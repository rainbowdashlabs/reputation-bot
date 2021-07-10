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

import static de.chojo.repbot.util.MessageUtil.markMessage;

public class ReputationService {
    private final ReputationData reputationData;
    private final GuildData guildData;
    private final RoleAssigner assigner;
    private final MagicImage magicImage;
    private final ContextResolver contextResolver;
    private final ILocalizer localizer;
    private Instant lastEasterEggSent = Instant.EPOCH;

    public ReputationService(DataSource dataSource, ContextResolver contextResolver, RoleAssigner assigner, MagicImage magicImage, ILocalizer localizer) {
        this.reputationData = new ReputationData(dataSource);
        this.guildData = new GuildData(dataSource);
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

        var optGuildSettings = guildData.getGuildSettings(guild);
        if (optGuildSettings.isEmpty()) return false;
        var settings = optGuildSettings.get();

        // block non reputation channel
        if (!settings.isReputationChannel(message.getTextChannel())) return false;

        if(!settings.hasDonorRole(guild.getMember(donor))) return false;
        if(!settings.hasReceiverRole(guild.getMember(receiver))) return false;

        // force settings
        switch (type) {
            case FUZZY -> {
                if (!settings.isFuzzyActive()) return false;
            }
            case MENTION -> {
                if (!settings.isMentionActive()) return false;
            }
            case ANSWER -> {
                if (!settings.isAnswerActive()) return false;

            }
            case REACTION -> {
                if (!settings.isReactionActive()) return false;
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

        if (!recentUser.contains(receiver)) {
            if (settings.isEmojiDebug()) message.addReaction(EmojiDebug.TARGET_NOT_IN_CONTEXT).queue();
            return false;
        }


        if (!canVote(donor, receiver, guild, settings)) {
            if (settings.isEmojiDebug()) message.addReaction(EmojiDebug.ONLY_COOLDOWN).queue();
            return false;
        }

        // block outdated ref message
        if (refMessage != null) {
            if (!settings.isFreshMessage(refMessage)) {
                if (settings.isEmojiDebug()) message.addReaction(EmojiDebug.TOO_OLD).queue();
                return false;
            }
        }

        // block outdated message
        if (!settings.isFreshMessage(message)) {
            if (settings.isEmojiDebug()) message.addReaction(EmojiDebug.TOO_OLD).queue();
            return false;
        }

        // block self vote
        if (Verifier.equalSnowflake(receiver, donor)) {
            if (lastEasterEggSent.until(Instant.now(), ChronoUnit.MINUTES) > magicImage.magicImageCooldown()
                    && ThreadLocalRandom.current().nextInt(magicImage.magicImagineChance()) == 0) {
                lastEasterEggSent = Instant.now();
                message.reply(new EmbedBuilder()
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
            markMessage(message, refMessage, settings);
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
        // block cooldown
        var lastRatedDuration = reputationData.getLastRatedDuration(guild, donor, receiver, ChronoUnit.MINUTES);
        if (lastRatedDuration < settings.cooldown()) return false;

        // block rep4rep
        var lastRatedEachDuration = reputationData.getLastRatedDuration(guild, receiver, donor, ChronoUnit.MINUTES);
        return lastRatedEachDuration >= settings.cooldown();
    }

    public boolean canVote(User donor, User receiver, Guild guild) {
        var optGuildSettings = guildData.getGuildSettings(guild);
        if (optGuildSettings.isEmpty()) return false;
        var settings = optGuildSettings.get();
        return canVote(donor, receiver, guild, settings);
    }
}
