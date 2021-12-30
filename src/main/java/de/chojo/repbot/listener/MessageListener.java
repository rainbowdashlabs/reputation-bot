package de.chojo.repbot.listener;

import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.repbot.analyzer.ContextResolver;
import de.chojo.repbot.analyzer.MessageAnalyzer;
import de.chojo.repbot.analyzer.ThankType;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import de.chojo.repbot.listener.voting.ReputationVoteListener;
import de.chojo.repbot.service.RepBotCachePolicy;
import de.chojo.repbot.service.ReputationService;
import de.chojo.repbot.util.Colors;
import de.chojo.repbot.util.EmojiDebug;
import de.chojo.repbot.util.Messages;
import de.chojo.repbot.util.PermissionErrorHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class MessageListener extends ListenerAdapter {
    private static final Logger log = getLogger(MessageListener.class);
    private final ILocalizer localizer;
    private final Configuration configuration;
    private final GuildData guildData;
    private final ReputationData reputationData;
    private final RepBotCachePolicy repBotCachePolicy;
    private final ReputationVoteListener reputationVoteListener;
    private final ReputationService reputationService;
    private final ContextResolver contextResolver;
    private final MessageAnalyzer messageAnalyzer;

    public MessageListener(ILocalizer localizer, DataSource dataSource, Configuration configuration, RepBotCachePolicy repBotCachePolicy,
                           ReputationVoteListener reputationVoteListener, ReputationService reputationService,
                           ContextResolver contextResolver, MessageAnalyzer messageAnalyzer) {
        this.localizer = localizer;
        guildData = new GuildData(dataSource);
        reputationData = new ReputationData(dataSource);
        this.configuration = configuration;
        this.repBotCachePolicy = repBotCachePolicy;
        this.reputationVoteListener = reputationVoteListener;
        this.reputationService = reputationService;
        this.contextResolver = contextResolver;
        this.messageAnalyzer = messageAnalyzer;
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        reputationData.removeMessage(event.getMessageIdLong());
    }

    @Override
    public void onMessageBulkDelete(@NotNull MessageBulkDeleteEvent event) {
        event.getMessageIds().stream().map(Long::valueOf).forEach(reputationData::removeMessage);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.isWebhookMessage()) return;
        var guild = event.getGuild();
        var settings = guildData.getGuildSettings(guild);

        if (!settings.thankSettings().isReputationChannel(event.getTextChannel())) return;
        repBotCachePolicy.seen(event.getMember());

        if (!settings.thankSettings().hasDonorRole(event.getMember())) return;

        var message = event.getMessage();

        var prefix = settings.generalSettings().prefix().orElse(configuration.baseSettings().defaultPrefix());
        if (prefix.startsWith("re:")) {
            var compile = Pattern.compile(prefix.substring(3));
            if (compile.matcher(message.getContentRaw()).find()) return;
        } else {
            if (message.getContentRaw().startsWith(prefix)) return;
        }
        if (message.getContentRaw().startsWith(settings.generalSettings().prefix().orElse(configuration.baseSettings().defaultPrefix()))) {
            return;
        }

        var analyzerResult = messageAnalyzer.processMessage(settings.thankSettings().thankwordPattern(), message, settings, true, 3);

        if (analyzerResult.type() == ThankType.NO_MATCH) return;

        if (PermissionErrorHandler.assertAndHandle(event.getTextChannel(), localizer, configuration,
                Permission.MESSAGE_SEND, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS)) {
            return;
        }

        if (configuration.migration().isActive()) {
            var activeMigrations = guildData.getActiveMigrations(configuration.migration().maxMigrationsPeriod());
            if (activeMigrations < configuration.migration().maxMigrations()) {
                guildData.promptMigration(event.getGuild());
            }
            if (guildData.migrationActive(event.getGuild())) {
                var embed = new EmbedBuilder()
                        .setTitle("⚠ Please migrate to the new version ⚠", configuration.links().invite())
                        .setDescription(configuration.migration().migrationMessage())
                        .setAuthor("→ Click here to invite the new bot instance ←", configuration.links().invite())
                        .setColor(Colors.Strong.RED)
                        .build();
                event.getChannel().sendMessageEmbeds(embed).queue();
                return;
            }
        }

        if (settings.generalSettings().isEmojiDebug()) {
            Messages.markMessage(event.getMessage(), EmojiDebug.FOUND_THANKWORD);
        }

        var resultType = analyzerResult.type();
        var resolveNoTarget = true;

        var donator = analyzerResult.donator();

        for (var result : analyzerResult.receivers()) {
            var refMessage = analyzerResult.referenceMessage();
            switch (resultType) {
                case FUZZY -> {
                    if (!settings.messageSettings().isFuzzyActive()) continue;
                    reputationService.submitReputation(guild, donator, result.getReference().getUser(), message, refMessage, resultType);
                    resolveNoTarget = false;
                }
                case MENTION -> {
                    if (!settings.messageSettings().isMentionActive()) continue;
                    reputationService.submitReputation(guild, donator, result.getReference().getUser(), message, refMessage, resultType);
                    resolveNoTarget = false;
                }
                case ANSWER -> {
                    if (!settings.messageSettings().isAnswerActive()) continue;
                    reputationService.submitReputation(guild, donator, result.getReference().getUser(), message, refMessage, resultType);
                    resolveNoTarget = false;
                }
            }
        }
        if (resolveNoTarget && settings.messageSettings().isEmbedActive()) resolveNoTarget(message, settings);
    }

    private void resolveNoTarget(Message message, GuildSettings settings) {
        var recentMembers = contextResolver.getCombinedContext(message, settings);
        recentMembers.remove(message.getMember());
        if (recentMembers.isEmpty()) {
            if (settings.generalSettings().isEmojiDebug()) Messages.markMessage(message, EmojiDebug.EMPTY_CONTEXT);
            return;
        }

        var members = recentMembers.stream()
                .filter(receiver -> reputationService.canVote(message.getAuthor(), receiver.getUser(), message.getGuild(), settings))
                .limit(10)
                .collect(Collectors.toList());

        if (members.isEmpty()) {
            if (settings.generalSettings().isEmojiDebug()) Messages.markMessage(message, EmojiDebug.ONLY_COOLDOWN);
            return;
        }

        reputationVoteListener.registerVote(message, members, settings);
    }
}
