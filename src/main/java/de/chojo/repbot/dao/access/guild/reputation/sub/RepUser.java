package de.chojo.repbot.dao.access.guild.reputation.sub;

import de.chojo.repbot.analyzer.results.match.ThankType;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.reputation.Reputation;
import de.chojo.repbot.dao.access.guild.reputation.sub.user.Gdpr;
import de.chojo.repbot.dao.access.guild.settings.sub.AbuseProtection;
import de.chojo.repbot.dao.components.MemberHolder;
import de.chojo.repbot.dao.snapshots.RepProfile;
import de.chojo.sadu.base.QueryFactory;
import de.chojo.sadu.wrapper.stage.StatementStage;
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
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class RepUser extends QueryFactory implements MemberHolder {
    private static final Logger log = getLogger(RepUser.class);
    private final Reputation reputation;
    private final Gdpr gdpr;
    private final User user;
    private Member member;

    public RepUser(Reputation reputation, Member member) {
        super(reputation);
        gdpr = new Gdpr(this);
        this.reputation = reputation;
        this.member = member;
        user = member.getUser();
    }

    public RepUser(Reputation reputation, User user) {
        super(reputation);
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
        return builder()
                .query("""
                       INSERT INTO reputation_offset(guild_id, user_id, amount) VALUES (?,?,?)
                           ON CONFLICT(guild_id, user_id)
                               DO UPDATE SET amount = reputation_offset.amount + excluded.amount;
                       """)
                .parameter(stmt -> stmt.setLong(guildId()).setLong(userId()).setLong(amount))
                .insert()
                .sendSync()
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
        return builder()
                .query("""
                       INSERT INTO reputation_offset(guild_id, user_id, amount) VALUES (?,?,?)
                           ON CONFLICT(guild_id, user_id)
                               DO UPDATE SET amount = excluded.amount;
                       """)
                .parameter(stmt -> stmt.setLong(guildId()).setLong(userId()).setLong(offset))
                .insert()
                .sendSync()
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
        var success = builder()
                .query("""
                       INSERT INTO
                       reputation_log(guild_id, donor_id, receiver_id, message_id, ref_message_id, channel_id, cause) VALUES(?,?,?,?,?,?,?)
                           ON CONFLICT(guild_id, donor_id, receiver_id, message_id)
                               DO NOTHING;
                       """)
                .parameter(stmt -> stmt.setLong(guildId()).setLong(donor == null ? 0 : donor.getIdLong())
                                       .setLong(userId())
                                       .setLong(message.getIdLong())
                                       .setLong(refMessage == null ? null : refMessage.getIdLong())
                                       .setLong(message.getChannel().getIdLong()).setString(type.name()))
                .insert()
                .sendSync()
                .changed();
        if (success) {
            log.debug("{} received one reputation from {} for message {}", user().getName(), donor != null ? donor.getEffectiveName() : "unkown", message.getIdLong());
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
     * @return true if the repuation was logged.
     */
    public boolean addOldReputation(@Nullable Member donor, @NotNull Message message, @Nullable Message refMessage, ThankType type) {
        var success = builder()
                .query("""
                       INSERT INTO
                       reputation_log(guild_id, donor_id, receiver_id, message_id, ref_message_id, channel_id, cause, received) VALUES(?,?,?,?,?,?,?,?)
                           ON CONFLICT(guild_id, donor_id, receiver_id, message_id)
                               DO NOTHING;
                       """)
                .parameter(stmt -> stmt.setLong(guildId())
                                       .setLong(donor == null ? 0 : donor.getIdLong())
                                       .setLong(userId())
                                       .setLong(message.getIdLong())
                                       .setLong(refMessage == null ? null : refMessage.getIdLong())
                                       .setLong(message.getChannel().getIdLong())
                                       .setString(type.name())
                                       .setTimestamp(Timestamp.from(message.getTimeCreated().toInstant())))
                .insert()
                .sendSync()
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
        return builder(Instant.class).
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
                .parameter(stmt -> stmt.setLong(reputation.guildId())
                                       .setLong(userId())
                                       .setLong(other.getIdLong())
                                       .setLong(other.getIdLong())
                                       .setLong(userId()))
                .readRow(row -> row.getTimestamp("received").toInstant())
                .firstSync();
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
        var builder = builder(RepProfile.class);
        StatementStage<RepProfile> query;
        if (mode.isSupportsOffset()) {
            query = builder
                    .query("""
                           SELECT rank, rank_donated, user_id, reputation, rep_offset, raw_reputation, donated
                           FROM %s
                           WHERE guild_id = ? AND user_id = ?;
                           """, mode.tableName());
        } else {
            query = builder
                    .query("""
                           SELECT rank, rank_donated, user_id, reputation, 0 AS rep_offset, reputation AS raw_reputation, donated
                           FROM %s
                           WHERE guild_id = ? AND user_id = ?;
                           """, mode.tableName());
        }

        return query.parameter(stmt -> stmt.setLong(guildId()).setLong(userId()))
                    .readRow(row -> RepProfile.buildProfile(this, row))
                    .firstSync()
                    .orElseGet(() -> RepProfile.empty(this, user()));

    }

    /**
     * Get the amount of received reputation based on {@link AbuseProtection#maxReceivedHours()}
     *
     * @return amount of received reputation
     */
    public int countReceived() {
        var hours = reputation().repGuild().settings().abuseProtection().maxReceivedHours();
        return builder(Integer.class)
                .query("SELECT COUNT(1) FROM reputation_log WHERE received > NOW() - ?::interval AND receiver_id = ?")
                .parameter(stmt -> stmt.setString("%s hours".formatted(hours)).setLong(memberId()))
                .readRow(rs -> rs.getInt(1))
                .firstSync()
                .orElse(0);
    }

    /**
     * Get the amount of received reputation based on {@link AbuseProtection#maxGivenHours()}
     *
     * @return amount of given reputation
     */
    public int countGiven() {
        var hours = reputation().repGuild().settings().abuseProtection().maxGivenHours();
        return builder(Integer.class)
                .query("SELECT COUNT(1) FROM reputation_log WHERE received > NOW() - ?::interval AND donor_id = ?")
                .parameter(stmt -> stmt.setString("%s hours".formatted(hours)).setLong(memberId()))
                .readRow(rs -> rs.getInt(1))
                .firstSync()
                .orElse(0);
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
