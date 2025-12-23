/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.listener;

import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.LocaleProvider;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.logutil.marker.LogNotify;
import de.chojo.repbot.analyzer.ContextResolver;
import de.chojo.repbot.analyzer.MessageAnalyzer;
import de.chojo.repbot.analyzer.results.empty.EmptyResultReason;
import de.chojo.repbot.analyzer.results.match.ThankType;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.listener.voting.ReputationVoteListener;
import de.chojo.repbot.service.RepBotCachePolicy;
import de.chojo.repbot.service.reputation.ReputationContext;
import de.chojo.repbot.service.reputation.ReputationService;
import de.chojo.repbot.service.reputation.SubmitResult;
import de.chojo.repbot.service.reputation.SubmitResultType;
import de.chojo.repbot.util.PermissionErrorHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class MessageListener extends ListenerAdapter {
    private static final Logger log = getLogger(MessageListener.class);
    private final ILocalizer localizer;
    private final Configuration configuration;
    private final GuildRepository guildRepository;
    private final RepBotCachePolicy repBotCachePolicy;
    private final ReputationVoteListener reputationVoteListener;
    private final ReputationService reputationService;
    private final ContextResolver contextResolver;
    private final MessageAnalyzer messageAnalyzer;

    public MessageListener(ILocalizer localizer, Configuration configuration, GuildRepository guildRepository, RepBotCachePolicy repBotCachePolicy,
                           ReputationVoteListener reputationVoteListener, ReputationService reputationService,
                           ContextResolver contextResolver, MessageAnalyzer messageAnalyzer) {
        this.localizer = localizer;
        this.guildRepository = guildRepository;
        this.configuration = configuration;
        this.repBotCachePolicy = repBotCachePolicy;
        this.reputationVoteListener = reputationVoteListener;
        this.reputationService = reputationService;
        this.contextResolver = contextResolver;
        this.messageAnalyzer = messageAnalyzer;
    }

    @Override
    public void onChannelCreate(@NotNull ChannelCreateEvent event) {
        if (event.getChannelType() == ChannelType.GUILD_PUBLIC_THREAD) {
            var thread = ((ThreadChannel) event.getChannel());
            var settings = guildRepository.guild(event.getGuild()).settings();
            if (settings.thanking().channels().isEnabled(thread)) {
                thread.join().complete();
            }
        }
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        reputationService.delete(event.getMessageIdLong(), event.getGuildChannel(), event.getGuild());
    }

    @Override
    public void onMessageBulkDelete(@NotNull MessageBulkDeleteEvent event) {
        reputationService.deleteBulk(event.getMessageIds().stream().map(Long::valueOf).toList(), event.getChannel(), event.getGuild());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.isWebhookMessage() || !event.isFromGuild()) return;
        var guild = event.getGuild();
        var repGuild = guildRepository.guild(guild);
        var settings = repGuild.settings();
        var thank = settings.thanking();

        var message = event.getMessage();
        var context = ReputationContext.fromMessage(message);
        if (message.getType() != MessageType.DEFAULT && message.getType() != MessageType.INLINE_REPLY) {
            return;
        }

        var analyzer = repGuild.reputation().analyzer();

        if (!thank.channels().isEnabled(event.getGuildChannel())) {
            analyzer.log(context, SubmitResult.of(SubmitResultType.CHANNEL_INACTIVE));
            return;
        }
        repBotCachePolicy.seen(event.getMember());

        if (!thank.donorRoles().hasRole(event.getMember())) {
            analyzer.log(context, SubmitResult.of(SubmitResultType.NO_DONOR_ROLE, Replacement.createMention(event.getMember())));
            return;
        }

        var result = messageAnalyzer.processMessage(
                thank.thankwords().thankwordPattern(), message, settings, true,
                settings.abuseProtection().maxMessageReputation());

        if (result.isEmpty()
                && (result.asEmpty().reason() == EmptyResultReason.NO_MATCH
                || result.asEmpty().reason() == EmptyResultReason.NO_PATTERN)) return;


        if (PermissionErrorHandler.assertAndHandle(event.getGuildChannel(), localizer.context(LocaleProvider.guild(event.getGuild())), configuration,
                Permission.MESSAGE_SEND, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS)) {
            log.debug("Permission error while analyzing on {} message {}", message.getGuild(), message.getIdLong());
            return;
        }

        log.trace("Found thankword in {}", message.getIdLong());

        if (settings.abuseProtection().isDonorLimit(event.getMember())) {
            analyzer.log(context, SubmitResult.of(SubmitResultType.DONOR_LIMIT));
            log.trace("Donor reached limit on {}", message.getIdLong());
            return;
        }

        if (result.isEmpty() && (settings.reputation().isEmbedActive() || settings.reputation().isDirectActive())) {
            resolveNoTarget(context, settings);
            return;
        }
        if (result.isEmpty()) {
            analyzer.log(context, SubmitResult.of(SubmitResultType.NO_TARGETS));
            return;
        }

        var match = result.asMatch();
        var resultType = match.thankType();

        var donator = match.donor();

        for (var receiver : match.receivers()) {
            switch (resultType) {
                case FUZZY -> {
                    if (!settings.reputation().isFuzzyActive()) continue;
                    reputationService.submitReputation(guild, donator, receiver, context, null, resultType);
                }
                case MENTION -> {
                    if (!settings.reputation().isMentionActive()) continue;
                    reputationService.submitReputation(guild, donator, receiver, context, null, resultType);
                }
                case ANSWER -> {
                    if (!settings.reputation().isAnswerActive()) continue;
                    reputationService.submitReputation(
                            guild, donator, receiver, context, match.asAnswer().referenceMessage(), resultType);
                }
                default -> log.error(LogNotify.NOTIFY_ADMIN, "Unknown thank type {}", resultType);
            }
        }
    }

    private void resolveNoTarget(ReputationContext context, Settings settings) {
        log.trace("Resolving missing target for {}", context.getIdLong());
        var recentMembers = new LinkedHashSet<>(contextResolver.getCombinedContext(context.asMessage(), settings).members());
        recentMembers.remove(context.asMessage().getMember());

        if (recentMembers.isEmpty()) {
            settings.repGuild().reputation().analyzer()
                    .log(context, SubmitResult.of(SubmitResultType.NO_RECENT_MEMBERS));
            log.trace("No recent members for {}", context.getIdLong());
            return;
        }

        var members = recentMembers.stream()
                                   .filter(receiver -> reputationService.checkCooldown(context, context.asMessage().getMember(), receiver, context.getGuild(), settings).type() == SubmitResultType.SUCCESS)
                                   .filter(receiver -> !settings.abuseProtection().isReceiverLimit(receiver))
                                   .limit(10)
                                   .collect(Collectors.toList());

        if (members.isEmpty()) {
            settings.repGuild().reputation().analyzer().log(context, SubmitResult.of(SubmitResultType.ALL_COOLDOWN));
            log.trace("None of the recent members can receive reputation {}", context.getIdLong());
            return;
        }

        if (members.size() == 1 && settings.reputation().isDirectActive()) {
            log.trace("Found single target on {}. Skipping embed", context.getIdLong());
            reputationService.submitReputation(context.getGuild(), context.asMessage().getMember(), members.get(0), context, null, ThankType.DIRECT);
            return;
        }

        if (settings.reputation().isEmbedActive()) {
            settings.repGuild().reputation().analyzer().log(context, SubmitResult.of(SubmitResultType.EMBED_SEND));
            reputationVoteListener.registerVote(context.asMessage(), members, settings);
        }
    }
}
