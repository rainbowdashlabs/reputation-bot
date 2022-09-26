package de.chojo.repbot.analyzer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.access.guild.settings.sub.AbuseProtection;
import de.chojo.repbot.dao.provider.Voice;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class ContextResolver {
    private static final Logger log = getLogger(ContextResolver.class);
    private final Voice voiceData;
    private final Configuration configuration;
    private final Cache<Long, MessageContext> messageContextCache = CacheBuilder.newBuilder()
                                                                                .expireAfterAccess(5, TimeUnit.SECONDS)
                                                                                .expireAfterWrite(10, TimeUnit.SECONDS)
                                                                                .maximumSize(100000)
                                                                                .build();

    private final Cache<Long, MessageContext> voiceContextCache = CacheBuilder.newBuilder()
                                                                              .expireAfterAccess(5, TimeUnit.SECONDS)
                                                                              .expireAfterWrite(10, TimeUnit.SECONDS)
                                                                              .build();

    public ContextResolver(Voice voiceData, Configuration configuration) {
        this.voiceData = voiceData;
        this.configuration = configuration;
    }

    /**
     * Get members which have written in the channel of the message.
     * <p>
     * Only members which have written in the last 100 messages which are not older than the max history and are not
     * send before the first message of the message author are returned
     *
     * @param message  message to determine channel, author and start time
     * @param settings setting sof the guild.
     * @return list of members which have written in this channel
     */
    @NotNull
    public MessageContext getChannelContext(Member target, Message message, @Nullable Settings settings) {
        try {
            return messageContextCache.get(message.getIdLong(), () -> retrieveChannelContext(target, message, settings).resolve());
        } catch (ExecutionException e) {
            log.error("Could not conpute channel context.", e);
        }
        return MessageContext.byMessageAndMember(message, target);
    }

    private MessageContext retrieveChannelContext(Member target, Message message, Settings settings) {
        var history = message.getChannel().getHistoryBefore(message, configuration.analyzerSettings().historySize())
                             .complete();
        List<Message> retrievedHistory = new ArrayList<>();
        // add user message
        retrievedHistory.add(message);
        retrievedHistory.addAll(history.getRetrievedHistory());
        retrievedHistory = retrievedHistory.stream()
                                           // Remove all bot messages. we won't need them anyway.
                                           .filter(mes -> !mes.getAuthor().isBot())
                                           .collect(Collectors.toList());

        var context = MessageContext.byMessageAndMember(message, target);
        context.addRawMessages(retrievedHistory);

        addRecentAuthors(context, settings);
        addLatestAuthors(context, settings);
        return context;
    }

    /**
     * Add the latest authors.
     * <p>
     * Authors are considered latest when they have written a message in the last {@link AbuseProtection#minMessages()}
     *
     * @param context  context to add
     * @param settings settings
     */
    private void addLatestAuthors(MessageContext context, @Nullable Settings settings) {
        var maxAge = Instant.now().minus(configuration.analyzerSettings().latestMaxHours(), ChronoUnit.HOURS);

        var oldest = findOldestMessageByTarget(context, maxAge);

        var limit = settings == null ? configuration.analyzerSettings().historySize() : settings.abuseProtection()
                                                                                                .minMessages();
        // add users of the last recent messages
        addMembersAfter(context.latestMessages(limit), context, oldest);
    }

    /**
     * Add the recent authors.
     * <p>
     * Authors are considered recent when they have written a message in the {@link AbuseProtection#maxMessageAge()}
     *
     * @param context  context to add
     * @param settings settings
     */
    private void addRecentAuthors(MessageContext context, Settings settings) {
        var maxAge = Instant.now().minus(settings == null ? Long.MAX_VALUE : settings.abuseProtection()
                                                                                     .maxMessageAge(), ChronoUnit.MINUTES);
        // find the oldest message in the history written by the message author which is newer than the max message age.
        var oldest = findOldestMessageByTarget(context, maxAge);

        addMembersAfter(context.rawMessages(), context, oldest);
    }

    /**
     * Add the ids and messages which were newer than oldest to the context
     *
     * @param messages messages
     * @param context  context
     * @param oldest   oldest allowed message
     */
    private void addMembersAfter(Collection<Message> messages, MessageContext context, Instant oldest) {
        // filter message for only recent messages and after the first message of the user.
        var filtered = messages.stream()
                               .filter(mes -> mes.getTimeCreated().toInstant().isAfter(oldest))
                               .collect(Collectors.toCollection(LinkedHashSet::new));
        context.addContextMessages(filtered);
        var memberIds = filtered.stream()
                                .map(Message::getAuthor)
                                .distinct()
                                .map(u -> context.guild().retrieveMemberById(u.getIdLong()).onErrorMap(mes -> null)
                                                 .complete())
                                .filter(Objects::nonNull)
                                .map(Member::getIdLong)
                                .collect(Collectors.toSet());
        context.addIds(memberIds);
    }

    private Instant findOldestMessageByTarget(MessageContext context, Instant maxAge) {
        return context.rawMessages().stream()
                      .filter(mes -> Verifier.equalSnowflake(mes.getAuthor(), context.user()))
                      .map(mes -> mes.getTimeCreated().toInstant())
                      .filter(entry -> entry.isAfter(maxAge))
                      .min(Instant::compareTo)
                      .orElse(maxAge);
    }

    public MessageContext getVoiceContext(Member target, Message message, @Nullable Settings settings) {
        try {
            return voiceContextCache.get(message.getIdLong(), () -> retrieveVoiceContext(target, message, settings).resolve()
                                                                                                                   .resolve());
        } catch (ExecutionException e) {
            log.error("Could not compute voice cache", e);
        }
        return MessageContext.byMessageAndMember(message, target);
    }

    private MessageContext retrieveVoiceContext(Member target, Message message, @Nullable Settings settings) {
        var context = MessageContext.byMessageAndMember(message, target);
        var voiceState = target.getVoiceState();
        if (voiceState == null) return context;
        if (voiceState.inAudioChannel()) {
            var voice = voiceState.getChannel()
                                  .getMembers()
                                  .stream()
                                  .map(Member::getIdLong)
                                  .collect(Collectors.toSet());
            context.addIds(voice);
        }
        var pastUser = voiceData.getPastUser(target.getUser(), message.getGuild(),
                settings == null ? 0 : settings.abuseProtection().minMessages(), configuration.analyzerSettings()
                                                                                              .voiceMembers());
        context.addIds(pastUser.stream()
                               .map(id -> message.getGuild().retrieveMemberById(id).onErrorMap(throwable -> null)
                                                 .complete())
                               .map(Member::getIdLong)
                               .toList());
        return context;
    }

    public MessageContext getCombinedContext(Message message, @Nullable Settings settings) {
        return getCombinedContext(message.getMember(), message, settings);
    }

    public MessageContext getCombinedContext(Member target, Message message, @Nullable Settings settings) {
        return getChannelContext(target, message, settings)
                .combine(getVoiceContext(target, message, settings));
    }
}
