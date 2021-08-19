package de.chojo.repbot.util;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.slf4j.Logger;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;

import static net.dv8tion.jda.api.entities.Message.MAX_CONTENT_LENGTH;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * This appender implements a discord webhook sink. Since webhooks are rate limited,
 * the appender tries tries to buffer bursts of log messages to make use of multiple
 * outputs within the same webhook call.
 */

//TODO: make flush interval configurable, maybe delay flush on receive or force flush below certain logging level
@Plugin(name = "DiscordWebhook", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public final class DiscordWebhookAppender extends AbstractAppender {
    private static final Logger log = getLogger(DiscordWebhookAppender.class);
    private static final int FLUSH_INTERVAL = 5 * 1000; // TODO: make configurable

    private static final int MAX_EMBED = 10; // discord allows up to 10 embeds per call
    private static final int MAX_EMBED_CHARS = 6000;
    private static final int MAX_FIELD_CHARS = 1024;
    // TODO: it's probably a bad idea to keep multiple clients lying around but since they are blocking,
    //  i have no better clue. ask me again for better solution
    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder().build();

    private static final Predicate<String> WEBHOOK_PREDICATE = WebhookClientBuilder.WEBHOOK_PATTERN.asMatchPredicate();

    /**
     * Shared timer is used to perform periodic flush of timers.
     */
    private static final Timer TIMER = new Timer("DiscordWebhookAppenderFlushTimer", true);

    private final WebhookClient webhookClient;
    private final List<WebhookEmbed> buffer = new ArrayList<>();
    private TimerTask flushTimer;

    private DiscordWebhookAppender(String name, Filter filter, boolean ignoreExceptions, WebhookClient webhookClient) {
        super(name, filter, null, ignoreExceptions, null);
        this.webhookClient = webhookClient;
    }

    @PluginFactory
    public static DiscordWebhookAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
            @PluginAttribute(value = "url", sensitive = true) String url,
            @PluginElement("Filters") Filter filter
    ) {
        if (name == null) {
            log.error("no name provided");
            return null;
        }

        if (!WEBHOOK_PREDICATE.test(url)) {
            log.error("invalid webhook url: {}", url);
            return null;
        }
        var client = new WebhookClientBuilder(url)
                .setDaemon(true) // ideally, we would never need any threads since we never use the callback
                .setHttpClient(OK_HTTP_CLIENT)
                .setWait(false)
                .build();


        return new DiscordWebhookAppender(name, filter, ignoreExceptions, client);
    }

    private static Color resolveColor(Level level) {
        if (level == Level.TRACE) {
            return Color.WHITE;
        }
        if (level == Level.DEBUG) {
            return Color.BLUE;
        }
        if (level == Level.INFO) {
            return Color.GREEN;
        }
        if (level == Level.WARN) {
            return Color.YELLOW;
        }
        if (level == Level.ERROR) {
            return Color.RED;
        }
        return Color.GRAY;
    }

    @Override
    public void start() {
        super.start();

        // timer task is perpetually called so needs initialisation
        // TODO: not too happy with code duplication and periodic timer, consider on-demand timer
        synchronized (buffer) { // synchronize might not be required but ensures proper synchronisation in case of changes to this class
            flushTimer = new TimerTask() {
                @Override
                public void run() {
                    flush();
                }
            };
            TIMER.schedule(flushTimer, FLUSH_INTERVAL);
        }
    }

    @Override
    public void stop() {
        // no need to shut down timer task as flush will clear buffer anyway and timer won't cause any harm when attempting to flush empty buffer
        flush();

        super.stop();
        webhookClient.close();
    }

    @Override
    public void append(LogEvent logEvent) {
        var currLength = 0;
        var source = logEvent.getSource();
        // TODO: this shit seems to cause recursive logging calls
        var title = logEvent.getLevel().name();
        currLength += title.length();
        var footer = source.getFileName() + "#" + source.getMethodName() + ":" + source.getLineNumber() + "@" + logEvent.getThreadName() + "-" + logEvent.getThreadId();
        currLength += footer.length();
        var eb = new WebhookEmbedBuilder()
                .setTitle(new WebhookEmbed.EmbedTitle(title, null))
                .setTimestamp(Instant.ofEpochMilli(logEvent.getInstant().getEpochMillisecond()))
                .setColor(resolveColor(logEvent.getLevel()).getRGB())
                .setFooter(new WebhookEmbed.EmbedFooter(footer, null));

        var descr = StringUtils.abbreviate(logEvent.getMessage().getFormattedMessage(), MAX_CONTENT_LENGTH);
        eb.setDescription(descr);
        currLength += descr.length();

        // append throwable if attached
        var throwable = logEvent.getThrown();
        if (throwable != null) {
            // the linebreaks and code blocks also require some characters (we hardcode this value)
            var lineChars = "\n\nst``````".length();
            var strippedException = StringUtils.strip(ExceptionUtils.getStackTrace(throwable)).replaceAll("^\s+?", "");
            // split exceptions on causes
            var chunks = strippedException.split("Caused by:");
            var chunkLength = chunks.length * (lineChars + "Caused by:".length() + 10);
            // calc the total exception length with line breaks
            var exceptionLength = strippedException.length() + chunkLength;
            var freeCharacters = MAX_EMBED_CHARS - currLength;

            freeCharacters -= throwable.getClass().getSimpleName().length();

            var abbreviate = exceptionLength >= freeCharacters;
            var abbreviateChars = MAX_FIELD_CHARS - 3;
            if (abbreviate) {
                abbreviateChars = Math.min(Math.abs(freeCharacters / chunkLength), MAX_FIELD_CHARS) - 3;
            }

            var first = true;
            for (var chunk : chunks) {
                var fieldTitle = first ? throwable.getClass().getSimpleName() : "Caused by";
                var text = StringUtils.abbreviate(chunk, abbreviateChars);
                text = String.format("```st%n%s%n```", text);
                eb.addField(new WebhookEmbed.EmbedField(false, fieldTitle, text));
                first = false;
            }
        }

        // add to buffer
        add(eb.build());
    }

    private void add(WebhookEmbed embed) {
        synchronized (buffer) {
            // each add flushes on full buffer, so we know the buffer has at least one space left
            buffer.add(embed);

            if (buffer.size() >= MAX_EMBED) {
                flush();
            }
        }
    }

    private void flush() {
        synchronized (buffer) {
            try {
                if (buffer.isEmpty()) {
                    return;
                }

                // no need to copy buffer since library will already perform full copy
                webhookClient.send(buffer);
                buffer.clear();

            } finally {
                // timer is always reset after flush, since we not know cause of flush, we always cancel timer
                flushTimer.cancel(); // timer can't flush empty buffer so no need for further synchronisation
                flushTimer = new TimerTask() {
                    @Override
                    public void run() {
                        flush();
                    }
                };
                TIMER.schedule(flushTimer, FLUSH_INTERVAL);
            }
        }
    }
}
