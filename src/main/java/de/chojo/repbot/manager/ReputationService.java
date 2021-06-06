package de.chojo.repbot.manager;

import de.chojo.jdautil.parsing.Verifier;
import de.chojo.repbot.analyzer.ThankType;
import de.chojo.repbot.config.elements.MagicImage;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import de.chojo.repbot.util.HistoryUtil;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static de.chojo.repbot.util.MessageUtil.markMessage;

public class ReputationService {
    private final ReputationData reputationData;
    private final GuildData guildData;
    private final RoleAssigner assigner;
    private final MagicImage magicImage;
    private Instant lastEasterEggSent = Instant.EPOCH;

    public ReputationService(DataSource dataSource, RoleAssigner assigner, MagicImage magicImage) {
        this.reputationData = new ReputationData(dataSource);
        this.guildData = new GuildData(dataSource);
        this.assigner = assigner;
        this.magicImage = magicImage;
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

        // Check if user was recently seen in this channel.
        var recentUsers = HistoryUtil.getRecentMembers(message, settings.maxMessageAge())
                .stream()
                .map(Member::getUser)
                .collect(Collectors.toSet());
        if (!recentUsers.contains(receiver)) return false;

        // block non vote channel
        if (!settings.isReputationChannel(message.getTextChannel())) return false;

        if (!canVote(donor, receiver, guild, settings)) return false;

        // block outdated ref message
        if (refMessage != null) {
            var until = refMessage.getTimeCreated().toInstant().until(Instant.now(), ChronoUnit.MINUTES);
            if (until > settings.maxMessageAge()) return false;
        }

        // block outdated message
        var until = message.getTimeCreated().toInstant().until(Instant.now(), ChronoUnit.MINUTES);
        if (until > settings.maxMessageAge()) return false;

        // block self vote
        if (Verifier.equalSnowflake(receiver, donor)) {
            if (lastEasterEggSent.until(Instant.now(), ChronoUnit.MINUTES) > magicImage.magicImageCooldown()
                    && ThreadLocalRandom.current().nextInt(magicImage.magicImagineChance()) == 0) {
                lastEasterEggSent = Instant.now();
                message.reply(new EmbedBuilder()
                        .setImage(magicImage.magicImageLink())
                        .setColor(Color.RED).build())
                        .queue(message1 -> message1.delete().queueAfter(
                                magicImage.magicImageDeleteSchedule(), TimeUnit.SECONDS));
            }
            return false;
        }

        // try to log reputation
        if (reputationData.logReputation(guild, donor, receiver, message, refMessage, type)) {
            // mark messages
            markMessage(message, refMessage, settings);
            // update role
            assigner.update(guild.getMember(receiver));
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
        if (lastRatedEachDuration < settings.cooldown()) return false;
        return true;
    }

    public boolean canVote(User donor, User receiver, Guild guild) {
        var optGuildSettings = guildData.getGuildSettings(guild);
        if (optGuildSettings.isEmpty()) return false;
        var settings = optGuildSettings.get();
        return canVote(donor, receiver, guild, settings);
    }
}
