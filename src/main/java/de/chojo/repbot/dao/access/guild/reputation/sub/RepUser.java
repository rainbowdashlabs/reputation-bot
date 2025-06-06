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
import de.chojo.repbot.dao.components.MemberHolder;
import de.chojo.repbot.dao.snapshots.ChannelStats;
import de.chojo.repbot.dao.snapshots.RepProfile;
import de.chojo.sadu.queries.converter.StandardValueConverter;
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
import java.util.List;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;
import static org.slf4j.LoggerFactory.getLogger;

public class RepUser implements MemberHolder {
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
     * @return true if the repuation was logged.
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
     * Get the time since the last reputation.
     *
     * @param other receiver
     * @return the time since the last vote in the requested time unit or 1 year if no entry was found.
     */
    public Duration getLastRatedDuration(Member other) {
        return getLastReputation(other).map(last -> Duration.between(last, Instant.now()))
                                       .orElseGet(() -> Duration.ofDays(365));
    }

    /**
     * Get the reputation user.
     *
     * @return the reputation user
     */
    public RepProfile profile() {
        var mode = reputation.repGuild().settings().general().reputationMode();
        // We probably don't want to cache the profile. There are just too many factors which can change the user reputation.
        @Language("postgresql")
        String query;
            query = """
                    WITH
                        rep_offset
                            AS (
                            SELECT
                                o.user_id,
                                sum(o.amount) AS reputation
                            FROM
                                reputation_offset o
                            WHERE o.added > :date_init
                              AND guild_id = :guild_id
                            GROUP BY o.user_id
                               ),
                        raw_log
                            AS (
                            SELECT
                                r.guild_id,
                                r.receiver_id,
                                r.donor_id
                            FROM
                                reputation_log r
                            WHERE r.received > :date_init
                              AND guild_id = :guild_id
                               ),
                        rep_count
                            AS (
                            SELECT
                                r.receiver_id,
                                count(1) AS reputation
                            FROM
                                raw_log r
                            GROUP BY r.receiver_id
                               ),
                        don_count
                            AS (
                            SELECT
                                r.donor_id,
                                count(1) AS donated
                            FROM
                                raw_log r
                            GROUP BY r.donor_id
                               ),
                        -- Build raw log with aggregated user reputation
                        full_log
                            AS (
                            SELECT
                                coalesce(rep.receiver_id, don.donor_id) AS user_id,
                                coalesce(rep.reputation, 0::BIGINT)     AS reputation,
                                coalesce(don.donated, 0::BIGINT)        AS donated
                            FROM
                                rep_count rep
                                    FULL JOIN don_count don
                                    ON rep.receiver_id = don.donor_id
                               ),
                        filtered_log
                            AS (
                            SELECT
                                user_id,
                                reputation,
                                donated
                            FROM
                                full_log
                            WHERE
                                -- Remove entries scheduled for cleanup
                                user_id NOT IN (
                                    SELECT
                                        1
                                    FROM
                                        repbot_schema.cleanup_schedule clean
                                    WHERE guild_id = :guild_id
                                               )
                               ),
                        offset_reputation
                            AS (
                            SELECT
                                coalesce(f.user_id, o.user_id)                        AS user_id,
                                -- apply offset to the normal reputation.
                                coalesce(f.reputation, 0) + coalesce(o.reputation, 0) AS reputation,
                                coalesce(o.reputation, 0)                             AS rep_offset,
                                -- save raw reputation without the offset.
                                coalesce(f.reputation, 0)                             AS raw_reputation,
                                coalesce(f.donated, 0)                                AS donated
                            FROM
                                filtered_log f
                                    FULL JOIN rep_offset o
                                    ON f.user_id = o.user_id
                               ),
                        ranked AS (
                            SELECT
                                rank() OVER (ORDER BY reputation DESC) AS rank,
                                rank() OVER (ORDER BY donated DESC)    AS rank_donated,
                                user_id,
                                raw_reputation                         AS raw_reputation,
                                donated,
                                rep_offset::BIGINT                     AS rep_offset,
                                reputation::BIGINT                     AS reputation
                            FROM
                                offset_reputation rank
                               )
                    SELECT
                        rank,
                        rank_donated,
                        user_id,
                        raw_reputation,
                        donated,
                        rep_offset,
                        reputation
                    FROM
                        ranked
                    WHERE user_id = :user_id
                    """;

        return query(query)
                .single(call().bind("guild_id", guildId()).bind("user_id",userId()).bind("date_init", mode.dateInit(), INSTANT_TIMESTAMP))
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

    public List<ChannelStats> mostReceivedChannel() {
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
                LIMIT 5;
                """)
                .single(call().bind(guildId())
                              .bind(memberId())
                              .bind(reputation().repGuild().settings().general().reputationMode().dateInit(), INSTANT_TIMESTAMP))
                .map(ChannelStats::build)
                .all();
    }

    public List<ChannelStats> mostGivenChannel() {
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
                LIMIT 5;
                """)
                .single(call().bind(guildId())
                              .bind(memberId())
                              .bind(reputation().repGuild().settings().general().reputationMode().dateInit(), INSTANT_TIMESTAMP))
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
