package de.chojo.repbot.listener;

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
import de.chojo.repbot.statistic.Statistic;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class MessageListener extends ListenerAdapter {
    private static final Logger log = getLogger(MessageListener.class);
    private final Configuration configuration;
    private final GuildData guildData;
    private final ReputationData reputationData;
    private final RepBotCachePolicy repBotCachePolicy;
    private final ReputationVoteListener reputationVoteListener;
    private final ReputationService reputationService;
    private final ContextResolver contextResolver;
    private final MessageAnalyzer messageAnalyzer;
    private final Statistic statistic;

    public MessageListener(DataSource dataSource, Configuration configuration, RepBotCachePolicy repBotCachePolicy, ReputationVoteListener reputationVoteListener, ReputationService reputationService, Statistic statistic) {
        guildData = new GuildData(dataSource);
        reputationData = new ReputationData(dataSource);
        this.configuration = configuration;
        this.repBotCachePolicy = repBotCachePolicy;
        this.reputationVoteListener = reputationVoteListener;
        this.reputationService = reputationService;
        this.contextResolver = new ContextResolver(dataSource);
        this.messageAnalyzer = new MessageAnalyzer(dataSource);
        this.statistic = statistic;
    }

    @Override
    public void onGuildMessageDelete(@NotNull GuildMessageDeleteEvent event) {
        reputationData.removeMessage(event.getMessageIdLong());
    }

    @Override
    public void onMessageBulkDelete(@NotNull MessageBulkDeleteEvent event) {
        event.getMessageIds().stream().map(Long::valueOf).forEach(reputationData::removeMessage);
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.isWebhookMessage()) return;
        repBotCachePolicy.seen(event.getMember());
        var guild = event.getGuild();
        var optGuildSettings = guildData.getGuildSettings(guild);
        if (optGuildSettings.isEmpty()) return;
        var settings = optGuildSettings.get();

        if (!settings.isReputationChannel(event.getChannel())) return;

        var thankwordPattern = settings.thankwordPattern();

        var message = event.getMessage();

        var prefix = settings.prefix().orElse(configuration.baseSettings().defaultPrefix());
        if (prefix.startsWith("re:")) {
            var compile = Pattern.compile(prefix.substring(3));
            if (compile.matcher(message.getContentRaw()).find()) return;
        } else {
            if (message.getContentRaw().startsWith(prefix)) return;
        }
        if (message.getContentRaw().startsWith(settings.prefix().orElse(configuration.baseSettings().defaultPrefix()))) {
            return;
        }

        var analyzerResult = messageAnalyzer.processMessage(thankwordPattern, message, settings, true, 0.85, 3);

        statistic.messageAnalyzed(event.getJDA());

        var donator = analyzerResult.donator();

        if (analyzerResult.type() == ThankType.NO_MATCH) return;

        var resultType = analyzerResult.type();
        var resolveNoTarget = true;
        for (var result : analyzerResult.receivers()) {
            var refMessage = analyzerResult.referenceMessage();
            switch (resultType) {
                case FUZZY -> {
                    if (!settings.isFuzzyActive()) return;
                    reputationService.submitReputation(guild, donator, result.getReference().getUser(), message, refMessage, resultType);
                    resolveNoTarget = false;
                }
                case MENTION -> {
                    if (!settings.isMentionActive()) return;
                    reputationService.submitReputation(guild, donator, result.getReference().getUser(), message, refMessage, resultType);
                    resolveNoTarget = false;
                }
                case ANSWER -> {
                    if (!settings.isAnswerActive()) return;
                    if (!settings.isFreshMessage(refMessage)) return;
                    reputationService.submitReputation(guild, donator, result.getReference().getUser(), message, refMessage, resultType);
                    resolveNoTarget = false;
                }
            }
        }
        if (resolveNoTarget) resolveNoTarget(message, settings);
    }

    private void resolveNoTarget(Message message, GuildSettings settings) {
        var recentMembers = contextResolver.getCombinedContext(message, settings);
        recentMembers.remove(message.getMember());
        if (recentMembers.isEmpty()) return;

        var members = recentMembers.stream()
                .filter(receiver -> reputationService.canVote(message.getAuthor(), receiver.getUser(), message.getGuild(), settings))
                .limit(10)
                .collect(Collectors.toList());

        if (members.isEmpty()) return;

        reputationVoteListener.registerVote(message, members);
    }
}
