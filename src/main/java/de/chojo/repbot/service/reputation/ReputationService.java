/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.reputation;

import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.LocalizationContext;
import de.chojo.jdautil.localization.util.LocaleProvider;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.jdautil.util.Premium;
import de.chojo.repbot.analyzer.ContextResolver;
import de.chojo.repbot.analyzer.MessageContext;
import de.chojo.repbot.analyzer.results.match.ThankType;
import de.chojo.repbot.commands.log.handler.LogFormatter;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.config.elements.MagicImage;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.access.guild.settings.sub.Reputation;
import de.chojo.repbot.dao.access.guild.settings.sub.integrationbypass.Bypass;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.dao.snapshots.ReputationLogEntry;
import de.chojo.repbot.service.RoleAssigner;
import de.chojo.repbot.util.Messages;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class ReputationService {
    private static final Logger log = getLogger(ReputationService.class);
    private final GuildRepository guildRepository;
    private final RoleAssigner assigner;
    private final Configuration configuration;
    private final ContextResolver contextResolver;
    private final ILocalizer localizer;
    private Instant lastEasterEggSent = Instant.EPOCH;

    public ReputationService(
            GuildRepository guildRepository,
            ContextResolver contextResolver,
            RoleAssigner assigner,
            Configuration configuration,
            ILocalizer localizer) {
        this.guildRepository = guildRepository;
        this.assigner = assigner;
        this.configuration = configuration;
        this.contextResolver = contextResolver;
        this.localizer = localizer;
    }

    /**
     * Submit a reputation.
     * <p>
     * This reputation will be checked by several factors based on the {@link de.chojo.repbot.dao.access.guild.settings.Settings}.
     *
     * @param guild      guild where the vote was given
     * @param donor      donor of the reputation
     * @param receiver   receiver of the reputation
     * @param context    triggered message
     * @param refMessage reference message if present
     * @param type       type of reputation source
     * @return {@link SubmitResult} indicating the success of the submission
     */
    public SubmitResult submitReputation(
            Guild guild,
            Member donor,
            @Nullable Member receiver,
            @NotNull ReputationContext context,
            @Nullable Message refMessage,
            ThankType type) {
        var repGuild = guildRepository.guild(guild);
        log.trace("Submitting reputation for message {} of type {}", context.getIdLong(), type);
        if (receiver == null) return SubmitResult.of(SubmitResultType.NO_RECEIVER);

        Optional<Bypass> optBypass = Optional.empty();

        // block bots
        if (donor.getUser().isBot()) {
            boolean notEntitled = Premium.isNotEntitled(
                    repGuild.subscriptions(),
                    configuration.skus().features().integrationBypass().allow());
            if (notEntitled) {
                log.trace("Author of {} is bot.", context.getIdLong());
                return SubmitResult.of(SubmitResultType.BLOCK_BOTS);
            }
            optBypass = repGuild.settings().integrationBypass().getBypass(donor.getIdLong());
            if (optBypass.isEmpty()) return SubmitResult.of(SubmitResultType.BLOCK_BOTS);
            if (!optBypass.get().isEnabled(type)) return SubmitResult.of(SubmitResultType.BLOCK_BOTS);
        }

        if(receiver.getUser().isBot()) {
            return SubmitResult.of(SubmitResultType.BLOCK_BOTS);
        }

        var settings = repGuild.settings();
        var messageSettings = settings.reputation();
        var thankSettings = settings.thanking();
        var analyzer = repGuild.reputation().analyzer();

        analyzer.log(
                context,
                SubmitResult.of(
                        SubmitResultType.SUBMITTING,
                        Replacement.create("type", "$%s$".formatted(type.nameLocaleKey())),
                        Replacement.createMention(donor)));

        // block non reputation channel
        if (type != ThankType.COMMAND && !thankSettings.channels().isEnabled(context.guildChannel())) {
            log.trace("Channel of message {} is not enabled", context.getIdLong());
            return analyzer.log(context, SubmitResult.of(SubmitResultType.CHANNEL_INACTIVE));
        }

        if (isTypeDisabled(type, messageSettings)) {
            log.trace("Thank type {} for message {} is disabled", type, context.getIdLong());
            return analyzer.log(
                    context,
                    SubmitResult.of(
                            SubmitResultType.THANK_TYPE_DISABLED,
                            Replacement.create("thanktype", "$%s$".formatted(type.nameLocaleKey()))));
        }

        var messageContext = getContext(donor, receiver, context, type, settings);

        if (isSelfVote(donor, receiver, context)) {
            log.trace("Detected self vote on {}", context.getIdLong());
            return analyzer.log(context, SubmitResult.of(SubmitResultType.SELF_VOTE));
        }

        var abuseResult = assertAbuseProtection(guild, donor, receiver, context, refMessage, messageContext, optBypass);
        if (abuseResult.type() != SubmitResultType.SUCCESS) return abuseResult;

        return log(guild, donor, receiver, context, refMessage, type, settings);
    }

    public void deleteBulk(List<Long> messages, GuildMessageChannelUnion channel, Guild guild) {
        var reputationLog = guildRepository.guild(guild).reputation().log();
        List<ReputationLogEntry> entries = messages.stream()
                .map(reputationLog::getLogEntries)
                .flatMap(Collection::stream)
                .toList();
        delete(entries, channel, guild);
    }

    public void delete(long messageId, GuildMessageChannelUnion channel, Guild guild) {
        List<ReputationLogEntry> logEntries =
                guildRepository.guild(guild).reputation().log().getLogEntries(messageId);
        delete(logEntries, channel, guild);
    }

    public void delete(List<ReputationLogEntry> entries, GuildMessageChannelUnion channel, Guild guild) {
        if (entries.isEmpty()) return;
        entries.forEach(ReputationLogEntry::deleteAll);
        LocalizationContext context = localizer.context(LocaleProvider.guild(guild));
        String deleted = entries.stream()
                .map(e -> LogFormatter.formatMessageLogEntrySimple(context, e))
                .collect(Collectors.joining("\n"));
        if (entries.size() > 1) {
            String title = localizer.localize(
                    "listener.reputation.log.bulkdelete", guild, Replacement.create("CHANNEL", channel.getAsMention()));
            deleted = title + "\n" + deleted;
        } else {
            deleted = LogFormatter.formatMessageLogEntrySimple(context, entries.getFirst()) + " **|** "
                    + channel.getAsMention();
        }

        logToChannel(guildRepository.guild(guild).settings(), ":red_circle: " + deleted);
    }

    public SubmitResult checkCooldown(
            ReputationContext context, Member donor, Member receiver, Guild guild, Settings settings) {
        var repGuild = settings.repGuild();
        var analyzer = repGuild.reputation().analyzer();
        // block cooldown
        var optRating = guildRepository.guild(guild).reputation().user(donor).getLastReputation(receiver);

        if (optRating.isPresent()) {
            var lastRating = optRating.get();

            if (settings.abuseProtection().cooldown() < 0) {
                return analyzer.log(context, SubmitResult.of(SubmitResultType.COOLDOWN_ONCE));
            }

            if (lastRating.tillNow().toMinutes() < settings.abuseProtection().cooldown()) {
                log.trace(
                        "The last rating is too recent. {}/{}",
                        lastRating.tillNow().toMinutes(),
                        settings.abuseProtection().cooldown());
                return analyzer.log(
                        context,
                        SubmitResult.of(
                                SubmitResultType.COOLDOWN_ACTIVE,
                                Replacement.create("TARGET", "$words.message$"),
                                Replacement.create("URL", lastRating.getMessageJumpLink()),
                                Replacement.create("ENTRY", lastRating.simpleString()),
                                Replacement.create("TIMESTAMP", lastRating.timestamp()),
                                Replacement.create(
                                        "REMAINING", lastRating.tillNow().toMinutes()),
                                Replacement.create(
                                        "TOTAL", settings.abuseProtection().cooldown())));
            }
        }

        if (!settings.thanking().receiverRoles().hasRole(receiver)) {
            log.trace("The receiver does not have a receiver role.");
            return analyzer.log(
                    context, SubmitResult.of(SubmitResultType.NO_RECEIVER_ROLE, Replacement.createMention(receiver)));
        }
        if (!settings.thanking().donorRoles().hasRole(donor)) {
            log.trace("The donor does not have a donor role.");
            return analyzer.log(
                    context, SubmitResult.of(SubmitResultType.NO_DONOR_ROLE, Replacement.createMention(donor)));
        }
        if (settings.thanking().denyReceiverRoles().hasRole(receiver)) {
            log.trace("The receiver does have a deny receiver role.");
            return analyzer.log(
                    context, SubmitResult.of(SubmitResultType.DENY_RECEIVER_ROLE, Replacement.createMention(receiver)));
        }
        if (settings.thanking().denyDonorRoles().hasRole(donor)) {
            log.trace("The donor does have a deny donor role.");
            return analyzer.log(
                    context, SubmitResult.of(SubmitResultType.DENY_DONOR_ROLE, Replacement.createMention(donor)));
        }

        return SubmitResult.of(SubmitResultType.SUCCESS);
    }

    private MessageContext getContext(
            Member donor, @NotNull Member receiver, ReputationContext context, ThankType type, Settings settings) {
        MessageContext messageContext;
        if (type == ThankType.REACTION) {
            // Check if user was recently seen in this channel.
            messageContext = contextResolver.getCombinedContext(donor, context.asMessage(), settings);
        } else {
            messageContext = context.getLastValidMessage()
                    .map(message -> contextResolver.getCombinedContext(message, settings))
                    .orElseGet(() -> contextResolver.getCombinedContext(receiver));
        }
        return messageContext;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private SubmitResult assertAbuseProtection(
            Guild guild,
            Member donor,
            Member receiver,
            ReputationContext context,
            @Nullable Message refMessage,
            MessageContext messageContext,
            Optional<Bypass> optBypass) {
        var contextId = context.getIdLong();
        var repGuild = guildRepository.guild(guild);
        var analyzer = repGuild.reputation().analyzer();
        var settings = repGuild.settings();
        var abuseSettings = settings.abuseProtection();

        // Abuse Protection: target context
        if (!messageContext.members().contains(receiver)
                && abuseSettings.isReceiverContext()
                && !optBypass.map(Bypass::ignoreContext).orElse(false)) {
            log.trace("Receiver is not in context of {}", contextId);
            return analyzer.log(
                    context,
                    SubmitResult.of(SubmitResultType.TARGET_NOT_IN_CONTEXT, Replacement.createMention(receiver)));
        }

        // Abuse Protection: donor context
        if (!messageContext.members().contains(donor)
                && abuseSettings.isDonorContext()
                && !optBypass.map(Bypass::ignoreContext).orElse(false)) {
            log.trace("Donor is not in context of {}", contextId);
            return analyzer.log(
                    context, SubmitResult.of(SubmitResultType.DONOR_NOT_IN_CONTEXT, Replacement.createMention(donor)));
        }

        // Abuse protection: Cooldown
        var canGiveReputation = checkCooldown(context, donor, receiver, guild, settings);
        if (canGiveReputation.type() != SubmitResultType.SUCCESS
                && !optBypass.map(Bypass::ignoreCooldown).orElse(false)) {
            log.trace("Cooldown active on {}", contextId);
            return canGiveReputation;
        }

        // block outdated ref message
        // Abuse protection: Message age
        if (refMessage != null) {
            if (abuseSettings.isOldMessage(refMessage)
                    && !messageContext
                            .latestMessages(abuseSettings.minMessages())
                            .contains(refMessage)) {
                log.trace("Reference message of {} is outdated", contextId);
                return analyzer.log(context, SubmitResult.of(SubmitResultType.OUTDATED_REFERENCE_MESSAGE));
            }
        }

        // block outdated message
        // Abuse protection: Message age
        if (abuseSettings.isOldMessage(context)) {
            log.trace("Message of {} is outdated", contextId);
            return analyzer.log(context, SubmitResult.of(SubmitResultType.OUTDATED_MESSAGE));
        }

        if (abuseSettings.isReceiverLimit(receiver)) {
            log.trace("Receiver limit is reached on {}", contextId);
            return analyzer.log(context, SubmitResult.of(SubmitResultType.RECEIVER_LIMIT));
        }

        if (abuseSettings.isDonorLimit(donor)
                && !optBypass.map(Bypass::ignoreLimit).orElse(false)) {
            log.trace("Donor limit is reached on {}", contextId);
            return analyzer.log(context, SubmitResult.of(SubmitResultType.DONOR_LIMIT));
        }

        return SubmitResult.of(SubmitResultType.SUCCESS);
    }

    private boolean isSelfVote(Member donor, Member receiver, ReputationContext context) {
        // block self vote
        if (Verifier.equalSnowflake(receiver, donor)) {
            MagicImage magicImage = configuration.magicImage();
            if (lastEasterEggSent.until(Instant.now(), ChronoUnit.MINUTES) > magicImage.magicImageCooldown()
                    && ThreadLocalRandom.current().nextInt(magicImage.magicImagineChance()) == 0
                    && context.isMessage()) {
                lastEasterEggSent = Instant.now();
                // TODO: Escape unknown channel 5
                context.asMessage()
                        .replyEmbeds(new EmbedBuilder()
                                .setImage(magicImage.magicImageLink())
                                .setColor(Color.RED)
                                .build())
                        .queue(msg -> msg.delete()
                                .queueAfter(
                                        magicImage.magicImageDeleteSchedule(),
                                        TimeUnit.SECONDS,
                                        RestAction.getDefaultSuccess(),
                                        ErrorResponseException.ignore(
                                                ErrorResponse.UNKNOWN_MESSAGE,
                                                ErrorResponse.UNKNOWN_CHANNEL,
                                                ErrorResponse.ILLEGAL_OPERATION_ARCHIVED_THREAD)));
            }
            return true;
        }
        return false;
    }

    private SubmitResult log(
            Guild guild,
            Member donor,
            Member receiver,
            ReputationContext context,
            @Nullable Message refMessage,
            ThankType type,
            Settings settings) {
        var repGuild = guildRepository.guild(guild);
        // try to log a reputation
        if (!repGuild.reputation()
                .user(receiver)
                .addReputation(
                        donor,
                        context,
                        refMessage,
                        type)) { // submit to database failed. Maybe this message was already voted by the user.
            log.trace(
                    "Could not log reputation for message {}. An equal entry was already present.",
                    context.getIdLong());
            return repGuild.reputation().analyzer().log(context, SubmitResult.of(SubmitResultType.ALREADY_PRESENT));
        }

        logReputationEntry(
                settings,
                guild,
                new ReputationLogEntry(
                        guild.getIdLong(),
                        context.getChannel().getIdLong(),
                        donor.getIdLong(),
                        receiver.getIdLong(),
                        context.getIdLong(),
                        refMessage == null ? 0 : refMessage.getIdLong(),
                        type,
                        Instant.now()));

        // mark messages
        if (context.isMessage()) {
            Messages.markMessage(context.asMessage(), refMessage, settings);
        }
        // update role
        var newRank = assigner.updateReporting(receiver, context.getChannel());

        // Send a level-up message
        newRank.ifPresent(rank -> {
            var announcements = repGuild.settings().announcements();
            if (!announcements.active()) return;
            var channel = context.getChannel();
            if (!announcements.sameChannel()) {
                channel = guild.getTextChannelById(announcements.channelId());
            }
            if (channel == null || rank.getRole(guild).isEmpty()) return;
            channel.sendMessage(localizer.localize(
                            "message.levelAnnouncement",
                            guild,
                            Replacement.createMention(receiver),
                            Replacement.createMention(rank.role().get())))
                    .setAllowedMentions(Collections.emptyList())
                    .complete();
        });
        return SubmitResult.of(SubmitResultType.SUCCESS);
    }

    private void logReputationEntry(Settings settings, Guild guild, ReputationLogEntry reputationLogEntry) {
        String message =
                LogFormatter.formatMessageLogEntry(localizer.context(LocaleProvider.guild(guild)), reputationLogEntry);
        logToChannel(settings, ":green_circle: " + message);
    }

    private void logToChannel(Settings settings, String string) {
        if (!settings.repGuild().settings().logChannel().active()) return;

        if (Premium.isNotEntitled(
                settings.repGuild().subscriptions(),
                configuration.skus().features().logChannel().logChannel())) return;

        TextChannel textChannelById = settings.guild()
                .getTextChannelById(settings.repGuild().settings().logChannel().channelId());
        if (textChannelById == null) return;

        textChannelById
                .sendMessage(string)
                .setAllowedMentions(Collections.emptyList())
                .complete();
    }

    private boolean isTypeDisabled(ThankType type, Reputation reputation) {
        // force settings
        switch (type) {
            case FUZZY -> {
                if (!reputation.isFuzzyActive()) return true;
            }
            case MENTION -> {
                if (!reputation.isMentionActive()) return true;
            }
            case ANSWER -> {
                if (!reputation.isAnswerActive()) return true;
            }
            case REACTION -> {
                if (!reputation.isReactionActive()) return true;
            }
            case EMBED -> {
                if (!reputation.isEmbedActive()) return true;
            }
            case DIRECT -> {
                if (!reputation.isDirectActive()) return true;
            }
            case COMMAND -> {
                if (!reputation.isCommandActive()) return true;
            }
            default -> throw new IllegalStateException("Unexpected value: " + type);
        }
        return false;
    }
}
