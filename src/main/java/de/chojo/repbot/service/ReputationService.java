package de.chojo.repbot.service;

import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.repbot.analyzer.ContextResolver;
import de.chojo.repbot.analyzer.ThankType;
import de.chojo.repbot.config.elements.MagicImage;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import de.chojo.repbot.util.EmojiDebug;
import de.chojo.repbot.util.Messages;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.awt.Color;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ReputationService {
    private final ReputationData reputationData;
    private final GuildData guildData;
    private final RoleAssigner assigner;
    private final MagicImage magicImage;
    private final ContextResolver contextResolver;
    private final ILocalizer localizer;
    private Instant lastEasterEggSent = Instant.EPOCH;

    public ReputationService(DataSource dataSource, ContextResolver contextResolver, RoleAssigner assigner, MagicImage magicImage, ILocalizer localizer) {
        reputationData = new ReputationData(dataSource);
        guildData = new GuildData(dataSource);
        this.assigner = assigner;
        this.magicImage = magicImage;
        this.contextResolver = contextResolver;
        this.localizer = localizer;
    }

    /**
     * Submit a reputation.
     * <p>
     * This reputation will be checked by several factors based on the {@link GuildSettings}.
     *
     * @param guild      guild where the vote was given
     * @param donor      donor of the reputation
     * @param receiver   receiver of the reputation
     * @param message    triggered message
     * @param refMessage reference message if present
     * @param type       type of reputation source
     * @return true if the reputation was counted and is valid
     */
    public boolean submitReputation(Guild guild, User donor, User receiver, Message message, @Nullable Message refMessage, ThankType type) {
        // block bots
        if (receiver.isBot()) return false;

        var settings = guildData.getGuildSettings(guild);
        var messageSettings = settings.messageSettings();
        var thankSettings = settings.thankSettings();
        var generalSettings = settings.generalSettings();
        var abuseSettings = settings.abuseSettings();

        // block non reputation channel
        if (!thankSettings.isReputationChannel(message.getChannel())) return false;

        if (!thankSettings.hasDonorRole(guild.getMember(donor))) return false;
        if (!thankSettings.hasReceiverRole(guild.getMember(receiver))) return false;

        // force settings
        switch (type) {
            case FUZZY -> {
                if (!messageSettings.isFuzzyActive()) return false;
            }
            case MENTION -> {
                if (!messageSettings.isMentionActive()) return false;
            }
            case ANSWER -> {
                if (!messageSettings.isAnswerActive()) return false;

            }
            case REACTION -> {
                if (!messageSettings.isReactionActive()) return false;
            }
            case EMBED -> {
                if (!messageSettings.isEmbedActive()) return false;
            }
            default -> throw new IllegalStateException("Unexpected value: " + type);
        }

        Set<Member> recentMember;
        if (type == ThankType.REACTION) {
            // Check if user was recently seen in this channel.
            recentMember = contextResolver.getCombinedContext(guild.getMember(donor), message, settings);
        } else {
            recentMember = contextResolver.getCombinedContext(message, settings);
        }

        var recentUser = recentMember.stream()
                .map(Member::getUser)
                .collect(Collectors.toSet());

        // Abuse Protection: target context
        if (!recentUser.contains(receiver) && abuseSettings.isReceiverContext()) {
            if (generalSettings.isEmojiDebug()) Messages.markMessage(message, EmojiDebug.TARGET_NOT_IN_CONTEXT);
            return false;
        }

        // Abuse Protection: donor context
        if (!recentUser.contains(donor) && abuseSettings.isDonorContext()) {
            if (generalSettings.isEmojiDebug()) Messages.markMessage(message, EmojiDebug.DONOR_NOT_IN_CONTEXT);
            return false;
        }

        // Abuse protection: Cooldown
        if (!canVote(donor, receiver, guild, settings)) {
            if (generalSettings.isEmojiDebug()) Messages.markMessage(message, EmojiDebug.ONLY_COOLDOWN);
            return false;
        }

        // block outdated ref message
        // Abuse protection: Message age
        if (refMessage != null) {
            if (!abuseSettings.isFreshMessage(refMessage)) {
                if (generalSettings.isEmojiDebug()) Messages.markMessage(message, EmojiDebug.TOO_OLD);
                return false;
            }
        }

        // block outdated message
        // Abuse protection: Message age
        if (!abuseSettings.isFreshMessage(message)) {
            if (generalSettings.isEmojiDebug()) Messages.markMessage(message, EmojiDebug.TOO_OLD);
            return false;
        }

        // block self vote
        if (Verifier.equalSnowflake(receiver, donor)) {
            if (lastEasterEggSent.until(Instant.now(), ChronoUnit.MINUTES) > magicImage.magicImageCooldown()
                && ThreadLocalRandom.current().nextInt(magicImage.magicImagineChance()) == 0) {
                lastEasterEggSent = Instant.now();
                message.replyEmbeds(new EmbedBuilder()
                                .setImage(magicImage.magicImageLink())
                                .setColor(Color.RED).build())
                        .queue(msg -> msg.delete().queueAfter(
                                magicImage.magicImageDeleteSchedule(), TimeUnit.SECONDS));
            }
            return false;
        }

        // try to log reputation
        if (reputationData.logReputation(guild, donor, receiver, message, refMessage, type)) {
            // mark messages
            Messages.markMessage(message, refMessage, settings);
            // update role
            try {
                assigner.update(guild.getMember(receiver));
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

    public boolean canVote(User donor, User receiver, Guild guild, GuildSettings settings) {
        var donorM = guild.getMember(donor);
        var receiverM = guild.getMember(receiver);

        if (donorM == null || receiverM == null) return false;

        // block cooldown
        var lastRated = reputationData.getLastRatedDuration(guild, donor, receiver, ChronoUnit.MINUTES);
        if (lastRated < settings.abuseSettings().cooldown()) return false;

        if (!settings.thankSettings().hasReceiverRole(receiverM)) return false;
        if (!settings.thankSettings().hasDonorRole(donorM)) return false;

        // block rep4rep
        lastRated = reputationData.getLastRatedDuration(guild, receiver, donor, ChronoUnit.MINUTES);
        return lastRated >= settings.abuseSettings().cooldown();
    }
}
