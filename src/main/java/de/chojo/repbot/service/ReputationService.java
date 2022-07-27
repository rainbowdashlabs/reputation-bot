package de.chojo.repbot.service;

import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.repbot.analyzer.ContextResolver;
import de.chojo.repbot.analyzer.MessageContext;
import de.chojo.repbot.analyzer.ThankType;
import de.chojo.repbot.config.elements.MagicImage;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.access.guild.settings.sub.Reputation;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.util.EmojiDebug;
import de.chojo.repbot.util.Messages;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ReputationService {
    private final Guilds guilds;
    private final RoleAssigner assigner;
    private final MagicImage magicImage;
    private final ContextResolver contextResolver;
    private final ILocalizer localizer;
    private Instant lastEasterEggSent = Instant.EPOCH;

    public ReputationService(Guilds guilds, ContextResolver contextResolver, RoleAssigner assigner, MagicImage magicImage, ILocalizer localizer) {
        this.guilds = guilds;
        this.assigner = assigner;
        this.magicImage = magicImage;
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
     * @param message    triggered message
     * @param refMessage reference message if present
     * @param type       type of reputation source
     * @return true if the reputation was counted and is valid
     */
    public boolean submitReputation(Guild guild, Member donor, Member receiver, Message message, @Nullable Message refMessage, ThankType type) {
        // block bots
        if (receiver.getUser().isBot()) return false;

        var settings = guilds.guild(guild).settings();
        var messageSettings = settings.reputation();
        var thankSettings = settings.thanking();

        // block non reputation channel
        if (!thankSettings.channels().isEnabled(message.getGuildChannel())) return false;

        if (!thankSettings.donorRoles().hasRole(guild.getMember(donor))) return false;
        if (!thankSettings.receiverRoles().hasRole(guild.getMember(receiver))) return false;

        if (isTypeDisabled(type, messageSettings)) return false;

        var context = getContext(guild, donor, message, type, settings);

        if (isSelfVote(donor, receiver, message)) return false;

        if (assertAbuseProtection(guild, donor, receiver, message, refMessage, context)) return false;

        return log(guild, donor, receiver, message, refMessage, type, settings);
    }

    private MessageContext getContext(Guild guild, Member donor, Message message, ThankType type, Settings settings) {
        MessageContext context;
        if (type == ThankType.REACTION) {
            // Check if user was recently seen in this channel.
            context = contextResolver.getCombinedContext(guild.getMember(donor), message, settings);
        } else {
            context = contextResolver.getCombinedContext(message, settings);
        }
        return context;
    }

    private boolean assertAbuseProtection(Guild guild, Member donor, Member receiver, Message message, @Nullable Message refMessage, MessageContext context) {
        var settings = guilds.guild(guild).settings();
        var addEmoji = settings.general().isEmojiDebug();
        var abuseSettings = settings.abuseProtection();

        // Abuse Protection: target context
        if (!context.members().contains(receiver) && abuseSettings.isReceiverContext()) {
            if (addEmoji) Messages.markMessage(message, EmojiDebug.TARGET_NOT_IN_CONTEXT);
            return true;
        }

        // Abuse Protection: donor context
        if (!context.members().contains(donor) && abuseSettings.isDonorContext()) {
            if (addEmoji) Messages.markMessage(message, EmojiDebug.DONOR_NOT_IN_CONTEXT);
            return true;
        }

        // Abuse protection: Cooldown
        if (!canVote(donor, receiver, guild, settings)) {
            if (addEmoji) Messages.markMessage(message, EmojiDebug.ONLY_COOLDOWN);
            return true;
        }

        // block outdated ref message
        // Abuse protection: Message age
        if (refMessage != null) {
            if (abuseSettings.isOldMessage(refMessage) && !context.latestMessages(abuseSettings.minMessages()).contains(refMessage)) {
                if (addEmoji) Messages.markMessage(message, EmojiDebug.TOO_OLD);
                return true;
            }
        }

        // block outdated message
        // Abuse protection: Message age
        if (abuseSettings.isOldMessage(message)) {
            if (addEmoji) Messages.markMessage(message, EmojiDebug.TOO_OLD);
            return true;
        }


        if (abuseSettings.isReceiverLimit(receiver)) {
            if (addEmoji) Messages.markMessage(message, EmojiDebug.RECEIVER_LIMIT);
            return true;
        }

        if (abuseSettings.isDonorLimit(receiver)) {
            if (addEmoji) Messages.markMessage(message, EmojiDebug.DONOR_LIMIT);
            return true;
        }

        return false;
    }

    private boolean isSelfVote(Member donor, Member receiver, Message message) {
        // block self vote
        if (Verifier.equalSnowflake(receiver, donor)) {
            if (lastEasterEggSent.until(Instant.now(), ChronoUnit.MINUTES) > magicImage.magicImageCooldown()
                && ThreadLocalRandom.current().nextInt(magicImage.magicImagineChance()) == 0) {
                lastEasterEggSent = Instant.now();
                //TODO: Escape unknown channel 5
                message.replyEmbeds(new EmbedBuilder()
                                .setImage(magicImage.magicImageLink())
                                .setColor(Color.RED).build())
                        .queue(msg -> msg.delete().queueAfter(
                                magicImage.magicImageDeleteSchedule(), TimeUnit.SECONDS,
                                RestAction.getDefaultSuccess(),
                                ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE, ErrorResponse.UNKNOWN_CHANNEL))
                        );
            }
            return true;
        }
        return false;
    }

    private boolean log(Guild guild, Member donor, Member receiver, Message message, @Nullable Message refMessage, ThankType type, Settings settings) {
        // try to log reputation
        if (guilds.guild(guild).reputation().user(receiver).addReputation(donor, message, refMessage, type)) {
            // mark messages
            Messages.markMessage(message, refMessage, settings);
            // update role
            try {
                var newRank = assigner.update(guild.getMember(receiver));

                // Send level up message
                newRank.ifPresent(rank -> {
                    var announcements = guilds.guild(guild).settings().announcements();
                    if (!announcements.isActive()) return;
                    var channel = message.getChannel();
                    if (!announcements.isSameChannel()) {
                        channel = guild.getTextChannelById(announcements.channelId());
                    }
                    if (channel == null || rank.getRole(guild) == null) return;
                    channel.sendMessage(localizer.localize("message.levelAnnouncement", guild,
                                    Replacement.createMention(receiver), Replacement.createMention(rank.getRole(guild))))
                            .allowedMentions(Collections.emptyList())
                            .queue();
                });

            } catch (RoleAccessException e) {
                message.getChannel()
                        .sendMessage(localizer.localize("error.roleAccess", message.getGuild(),
                                Replacement.createMention("ROLE", e.role())))
                        .allowedMentions(Collections.emptyList())
                        .queue();
            }
            return true;
        }
        // submit to database failed. Maybe this message was already voted by the user.
        return false;
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
                if (!reputation.isSkipSingleEmbed()) return false;
            }
            default -> throw new IllegalStateException("Unexpected value: " + type);
        }
        return false;
    }

    public boolean canVote(Member donor, Member receiver, Guild guild, Settings settings) {
        var donorM = guild.getMember(donor);
        var receiverM = guild.getMember(receiver);

        if (donorM == null || receiverM == null) return false;

        // block cooldown
        var lastRated = guilds.guild(guild).reputation().user(donorM).getLastRatedDuration(receiver);
        if (lastRated.toMinutes() < settings.abuseProtection().cooldown()) return false;

        if (!settings.thanking().receiverRoles().hasRole(receiverM)) return false;
        if (!settings.thanking().donorRoles().hasRole(donorM)) return false;

        return lastRated.toMinutes() >= settings.abuseProtection().cooldown();
    }
}
