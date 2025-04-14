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
import de.chojo.repbot.dao.components.MemberHolder;
import de.chojo.repbot.dao.snapshots.RepProfile;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Represents a user with reputation in a guild.
 */
public class RepUser implements MemberHolder {
    private static final Logger log = getLogger(RepUser.class);
    private final Reputation reputation;
    private final Gdpr gdpr;
    private final User user;
    private Member member;

    /**
     * Constructs a RepUser with the specified reputation and member.
     *
     * @param reputation the reputation
     * @param member the member
     */
    public RepUser(Reputation reputation, Member member) {
        gdpr = new Gdpr(this);
        this.reputation = reputation;
        this.member = member;
        user = member.getUser();
    }

    /**
     * Constructs a RepUser with the specified reputation and user.
     *
     * @param reputation the reputation
     * @param user the user
     */
    public RepUser(Reputation reputation, User user) {
        gdpr = new Gdpr(this);
        this.reputation = reputation;
        this.user = user;
    }

    /**
     * Returns the GDPR settings for the user.
     *
     * @return the GDPR settings
     */
    public Gdpr gdpr() {
        return gdpr;
    }

    /**
     * Adds an amount of reputation to the user's reputation count.
     *
     * @param amount the amount to add, can be negative to subtract
     * @return true if the reputation was added
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
     * Removes an amount of reputation from the user's reputation count.
     *
     * @param amount the amount to remove
     * @return true if the reputation was removed
     */
    public boolean removeReputation(long amount) {
        return addReputation(-amount);
    }

    /**
     * Sets the reputation offset to a value which will result in the specified reputation amount for the user.
     *
     * @param amount the reputation amount the user should have
     * @return true if the reputation was set
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
     * Logs reputation for a user.
     *
     * @param donor the donor of the reputation
     * @param message the message to log
     * @param refMessage the reference message if available
     * @param type the type of reputation
     * @return true if the reputation was logged
     */
    public boolean addReputation(@Nullable Member donor, @NotNull Message message, @Nullable Message refMessage, ThankType type) {
        var success = query("""
                INSERT INTO
                reputation_log(guild_id, donor_id, receiver_id, message_id, ref_message_id, channel_id, cause) VALUES(?,?,?,?,?,?,?)
                    ON CONFLICT(guild_id, donor_id, receiver_id, message_id)
                        DO NOTHING;
                """)
                .single(call().bind(guildId()).bind(donor == null ? 0 : donor.getIdLong())
                              .bind(userId())
                              .bind(message.getIdLong())
                              .bind(refMessage == null ? null : refMessage.getIdLong())
                              .bind(message.getChannel().getIdLong()).bind(type.name()))
                .insert()
                .changed();
        if (success) {
            log.debug("{} received one reputation from {} on guild {} for message {}", userId(), donor != null ? donor.getIdLong() : "unknown", guildId(), message.getIdLong());
        }
        return success;
    }

    /**
     * Logs reputation for a user with a backdated received date.
     *
     * @param donor the donor of the reputation
     * @param message the message to log
     * @param refMessage the reference message if available
     * @param type the type of reputation
     * @return true if the reputation was logged
     */
    public boolean addOldReputation(@Nullable Member donor, @NotNull Message message, @Nullable Message refMessage, ThankType type) {
        var success = query("""
                INSERT INTO
                reputation_log(guild_id, donor_id, receiver_id, message_id, ref_message_id, channel_id, cause, received) VALUES(?,?,?,?,?,?,?,?)
                    ON CONFLICT(guild_id, donor_id, receiver_id, message_id)
                        DO NOTHING;
                """)
                .single(call().bind(guildId())
                              .bind(donor == null ? 0 : donor.getIdLong())
                              .bind(userId())
                              .bind(message.getIdLong())
                              .bind(refMessage == null ? null : refMessage.getIdLong())
                              .bind(message.getChannel().getIdLong())
                              .bind(type.name())
                              .bind(Timestamp.from(message.getTimeCreated().toInstant())))
                .insert()
                .changed();
        if (success) {
            log.debug("{} received one reputation from {} for message {}", user().getName(), donor != null ? donor.getEffectiveName() : "unknown", message.getIdLong());
        }
        return success;
    }

    /**
     * Gets the last time the user gave or received reputation from another user.
     *
     * @param other the other user
     * @return the last timestamp as an instant
     */
    public Optional<Instant> getLastReputation(Member other) {
        return
                query("""
                        SELECT
                            received
                        FROM
                            reputation_log
                        WHERE
                            guild_id = ?
                            AND ((donor_id = ? AND receiver_id = ?)
                                OR (donor_id = ? AND receiver_id = ?))
                        ORDER BY received DESC
                        LIMIT  1;
                        """)
                        .single(call().bind(reputation.guildId())
                                      .bind(userId())
                                      .bind(other.getIdLong())
                                      .bind(other.getIdLong())
                                      .bind(userId()))
                        .map(row -> row.getTimestamp("received").toInstant())
                        .first();
    }

    /**
     * Gets the time since the last reputation interaction with another user.
     *
     * @param other the other user
     * @return the duration since the last interaction or 1 year if no entry was found
     */
    public Duration getLastRatedDuration(Member other) {
        return getLastReputation(other).map(last -> Duration.between(last, Instant.now()))
                                       .orElseGet(() -> Duration.ofDays(365));
    }

    /**
     * Gets the reputation profile of the user.
     *
     * @return the reputation profile
     */
    public RepProfile profile() {
        var mode = reputation.repGuild().settings().general().reputationMode();
        // We probably don't want to cache the profile. There are just too many factors which can change the user reputation.
        @Language("postgresql")
        String query;
        if (mode.isSupportsOffset()) {
            query = """
                    SELECT rank, rank_donated, user_id, reputation, rep_offset, raw_reputation, donated
                    FROM %s
                    WHERE guild_id = ? AND user_id = ?;
                    """;
        } else {
            query = """
                    SELECT rank, rank_donated, user_id, reputation, 0 AS rep_offset, reputation AS raw_reputation, donated
                    FROM %s
                    WHERE guild_id = ? AND user_id = ?;
                    """;
        }

        return query(query, mode.tableName())
                .single(call().bind(guildId()).bind(userId()))
                .map(row -> RepProfile.buildProfile(this, row))
                .first()
                .orElseGet(() -> RepProfile.empty(this, user()));

    }

    /**
     * Gets the amount of received reputation based on the maximum received hours.
     *
     * @return the amount of received reputation
     */
    public int countReceived() {
        var hours = reputation().repGuild().settings().abuseProtection().maxReceivedHours();
        return query("SELECT COUNT(1) FROM reputation_log WHERE received > NOW() - ?::interval AND receiver_id = ?")
                .single(call().bind("%s hours".formatted(hours)).bind(memberId()))
                .map(rs -> rs.getInt(1))
                .first()
                .orElse(0);
    }

    /**
     * Gets the amount of given reputation based on the maximum given hours.
     *
     * @return the amount of given reputation
     */
    public int countGiven() {
        var hours = reputation().repGuild().settings().abuseProtection().maxGivenHours();
        return query("SELECT COUNT(1) FROM reputation_log WHERE received > NOW() - ?::interval AND donor_id = ?")
                .single(call().bind("%s hours".formatted(hours)).bind(memberId()))
                .map(rs -> rs.getInt(1))
                .first()
                .orElse(0);
    }

    /**
     * Returns the member associated with this RepUser.
     *
     * @return the member
     */
    @Override
    public Member member() {
        return member;
    }

    /**
     * Returns the user associated with this RepUser.
     *
     * @return the user
     */
    @Override
    public User user() {
        return user;
    }

    /**
     * Returns the guild associated with this RepUser.
     *
     * @return the guild
     */
    @Override
    public Guild guild() {
        return reputation.guild();
    }

    /**
     * Refreshes the member associated with this RepUser.
     *
     * @param member the member
     * @return the refreshed RepUser
     */
    public RepUser refresh(Member member) {
        this.member = member;
        return this;
    }

    /**
     * Returns the reputation associated with this RepUser.
     *
     * @return the reputation
     */
    public Reputation reputation() {
        return reputation;
    }

    /**
     * Returns the configuration associated with this RepUser.
     *
     * @return the configuration
     */
    public Configuration configuration() {
        return reputation.configuration();
    }
}
