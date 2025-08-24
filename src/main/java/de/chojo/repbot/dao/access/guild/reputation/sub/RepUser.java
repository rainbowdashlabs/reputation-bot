/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.reputation.sub;

import de.chojo.repbot.analyzer.results.match.ThankType;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.reputation.Reputation;
import de.chojo.repbot.dao.access.guild.reputation.sub.user.Gdpr;
import de.chojo.repbot.dao.access.guild.settings.sub.AbuseProtection;
import de.chojo.repbot.dao.access.guild.settings.sub.CooldownDirection;
import de.chojo.repbot.dao.components.MemberHolder;
import de.chojo.repbot.dao.snapshots.ChannelStats;
import de.chojo.repbot.dao.snapshots.RepProfile;
import de.chojo.repbot.dao.snapshots.ReputationLogEntry;
import de.chojo.repbot.util.QueryLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;
import static org.slf4j.LoggerFactory.getLogger;

public class RepUser implements MemberHolder {
    private static final String PROFILE = QueryLoader.loadQuery("repuser", "profile");
    private static final Logger log = getLogger(RepUser.class);
    private final Reputation reputation;
    private final Gdpr gdpr;
    private final User user;
    private Member member;

    public RepUser(Reputation reputation, Member member) {
        gdpr = new Gdpr(this);
        this.reputation = reputation;
        this.member = member;
        user = member.getUser();
    }

    public RepUser(Reputation reputation, User user) {
        gdpr = new Gdpr(this);
        this.reputation = reputation;
        this.user = user;
    }

    public Gdpr gdpr() {
        return gdpr;
    }

    /**
     * Add an amount of reputation to the reputation count of the user
     *
     * @param amount amount to add. Can be negative to subtract.
     * @return true if added
     */
    public boolean addReputation(long amount) {
        return query("""
                INSERT INTO reputation_offset(guild_id, user_id, amount) VALUES (?,?,?)
                """)
                .single(call().bind(guildId()).bind(userId()).bind(amount))
                .insert()
                .changed();
    }

    /**
     * Removes an amount of reputation from the reputation count of the user.
     *
     * @param amount amount to remove
     * @return true if changed
     */
    public boolean removeReputation(long amount) {
        return addReputation(-amount);
    }

    /**
     * Set the reputation offset to a value which will let the reputation of the user result in the entered amount.
     *
     * @param amount the reputation amount the user should have
     * @return true if changed.
     */
    public boolean setReputation(long amount) {
        var offset = amount - profile().reputation();
        return query("""
                INSERT INTO reputation_offset(guild_id, user_id, amount) VALUES (?,?,?)
                """)
                .single(call().bind(guildId()).bind(userId()).bind(offset))
                .insert()
                .changed();
    }

    /**
     * Log reputation for a user.
     *
     * @param donor      donator of the reputation
     * @param message    message to log
     * @param refMessage reference message if available
     * @param type       type of reputation
     * @return true if the reputation was logged.
     */
    public boolean addReputation(@Nullable Member donor, @NotNull Message message, @Nullable Message refMessage, ThankType type) {
        var success = query("""
                INSERT INTO
                reputation_log(guild_id, donor_id, receiver_id, message_id, ref_message_id, channel_id, cause) VALUES(?,?,?,?,?,?,?)
                    ON CONFLICT(guild_id, donor_id, receiver_id, message_id)
                        DO NOTHING;
                """)
                .single(call().bind(guildId()).bind(donor == null ? null : donor.getIdLong())
                              .bind(userId())
                              .bind(message.getIdLong())
                              .bind(refMessage == null ? null : refMessage.getIdLong())
                              .bind(message.getChannel().getIdLong()).bind(type.name()))
                .insert()
                .changed();
        if (success) {
            log.debug("{} received one reputation from {} on guild {} for message {}", userId(), donor != null ? donor.getIdLong() : "unkown", guildId(), message.getIdLong());
        }
        return success;
    }

    /**
     * Log reputation for a user.
     * <p>
     * The received date will be dated back to {@link Message#getTimeCreated()}.
     *
     * @param donor      donator of the reputation
     * @param message    message to log
     * @param refMessage reference message if available
     * @param type       type of reputation
     * @return true if the reputation was logged.
     */
    public boolean addOldReputation(@Nullable Member donor, @NotNull Message message, @Nullable Message refMessage, ThankType type) {
        var success = query("""
                INSERT INTO
                reputation_log(guild_id, donor_id, receiver_id, message_id, ref_message_id, channel_id, cause, received) VALUES(?,?,?,?,?,?,?,?)
                    ON CONFLICT(guild_id, donor_id, receiver_id, message_id)
                        DO NOTHING;
                """)
                .single(call().bind(guildId())
                              .bind(donor == null ? null : donor.getIdLong())
                              .bind(userId())
                              .bind(message.getIdLong())
                              .bind(refMessage == null ? null : refMessage.getIdLong())
                              .bind(message.getChannel().getIdLong())
                              .bind(type.name())
                              .bind(Timestamp.from(message.getTimeCreated().toInstant())))
                .insert()
                .changed();
        if (success) {
            log.debug("{} received one reputation from {} for message {}", user().getName(), donor != null ? donor.getEffectiveName() : "unkown", message.getIdLong());
        }
        return success;
    }


    /**
     * Get the last time the user gave reputation to the user or received reputation from this user
     *
     * @param other the other user
     * @return last timestamp as instant
     */
    public Optional<ReputationLogEntry> getLastReputation(Member other) {
        CooldownDirection cooldownDirection = reputation.repGuild().settings().abuseProtection().cooldownDirection();
        switch (cooldownDirection) {
            case BIDIRECTIONAL -> {
                return query("""
                        SELECT
                            guild_id, donor_id, receiver_id, message_id, received, ref_message_id, channel_id, cause
                        FROM
                            reputation_log
                        WHERE
                            guild_id = ?
                            AND ((donor_id = :this AND receiver_id = :other)
                                OR (donor_id = :other AND receiver_id = :this))
                        ORDER BY received DESC
                        LIMIT  1;
                        """)
                        .single(call().bind(reputation.guildId())
                                      .bind("other", other.getIdLong())
                                      .bind("this", userId()))
                        .map(ReputationLogEntry::build)
                        .first();
            }
            case UNIDIRECTIONAL -> {
                return query("""
                        SELECT
                            guild_id, donor_id, receiver_id, message_id, received, ref_message_id, channel_id, cause
                        FROM
                            reputation_log
                        WHERE
                            guild_id = ?
                            AND (donor_id = :this AND receiver_id = :other)
                        ORDER BY received DESC
                        LIMIT  1;
                        """)
                        .single(call().bind(reputation.guildId())
                                      .bind("other", other.getIdLong())
                                      .bind("this", userId()))
                        .map(ReputationLogEntry::build)
                        .first();

            }
            case null, default -> {
                throw new IllegalStateException("Unknown cooldown direction: " + cooldownDirection);
            }
        }
    }

    /**
     * Get the time since the last reputation.
     *
     * @param other receiver
     * @return the time since the last vote in the requested time unit or 1 year if no entry was found.
     */
    public Optional<Duration> getLastRatedDuration(Member other) {
        return getLastReputation(other).map(last -> Duration.between(last.received(), Instant.now()));
    }

    /**
     * Get the reputation user.
     *
     * @return the reputation user
     */
    public RepProfile profile() {
        var mode = reputation.repGuild().settings().general().reputationMode();
        LocalDate resetDate = reputation.repGuild().settings().general().resetDate();
        // We probably don't want to cache the profile. There are just too many factors which can change the user reputation.

        return query(PROFILE)
                .single(call().bind("guild_id", guildId())
                              .bind("user_id", userId())
                              .bind("date_init", mode.dateInit(), INSTANT_TIMESTAMP)
                              .bind("reset_date", resetDate))
                .map(row -> RepProfile.buildProfile(this, row))
                .first()
                .orElseGet(() -> RepProfile.empty(this, user()));

    }

    /**
     * Get the amount of the received reputation based on {@link AbuseProtection#maxReceivedHours()}
     *
     * @return amount of the received reputation
     */
    public int countReceived() {
        var hours = reputation().repGuild().settings().abuseProtection().maxReceivedHours();
        return query("SELECT count(1) FROM reputation_log WHERE received > now() - ?::INTERVAL AND receiver_id = ?")
                .single(call().bind("%s hours".formatted(hours)).bind(memberId()))
                .map(rs -> rs.getInt(1))
                .first()
                .orElse(0);
    }

    /**
     * Get the amount of received reputation based on {@link AbuseProtection#maxGivenHours()}
     *
     * @return amount of given reputation
     */
    public int countGiven() {
        var hours = reputation().repGuild().settings().abuseProtection().maxGivenHours();
        return query("SELECT count(1) FROM reputation_log WHERE received > now() - ?::INTERVAL AND donor_id = ?")
                .single(call().bind("%s hours".formatted(hours)).bind(memberId()))
                .map(rs -> rs.getInt(1))
                .first()
                .orElse(0);
    }

    public List<ChannelStats> mostReceivedChannel(int count) {
        return query("""
                SELECT
                    channel_id,
                    count(1) AS count
                FROM
                    reputation_log
                WHERE guild_id = ?
                  AND receiver_id = ?
                  AND received > ?
                GROUP BY channel_id
                ORDER BY count DESC
                LIMIT ?;
                """)
                .single(call().bind(guildId())
                              .bind(memberId())
                              .bind(reputation().repGuild().settings().general().reputationMode().dateInit(), INSTANT_TIMESTAMP)
                              .bind(count))
                .map(ChannelStats::build)
                .all();
    }

    public List<ChannelStats> mostGivenChannel(int count) {
        return query("""
                SELECT
                    channel_id,
                    count(1) AS count
                FROM
                    reputation_log
                WHERE guild_id = ?
                  AND donor_id = ?
                  AND received > ?
                GROUP BY channel_id
                ORDER BY count DESC
                LIMIT ?;
                """)
                .single(call().bind(guildId())
                              .bind(memberId())
                              .bind(reputation().repGuild().settings().general().reputationMode().dateInit(), INSTANT_TIMESTAMP)
                              .bind(count))
                .map(ChannelStats::build)
                .all();
    }

    @Override
    public Member member() {
        return member;
    }

    @Override
    public User user() {
        return user;
    }

    @Override
    public Guild guild() {
        return reputation.guild();
    }

    public RepUser refresh(Member member) {
        this.member = member;
        return this;
    }

    public Reputation reputation() {
        return reputation;
    }

    public Configuration configuration() {
        return reputation.configuration();
    }
}
