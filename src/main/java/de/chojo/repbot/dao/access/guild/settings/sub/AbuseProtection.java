/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub;

import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

/**
 * Handles abuse protection settings for a guild.
 */
public class AbuseProtection implements GuildHolder {
    private final Settings settings;
    private int cooldown;
    private int maxMessageAge;
    private int minMessages;
    private boolean donorContext;
    private boolean receiverContext;
    private int maxGiven;
    private int maxGivenHours;
    private int maxReceived;
    private int maxReceivedHours;
    private int maxMessageReputation;

    /**
     * Constructs an AbuseProtection object with specified settings.
     *
     * @param settings the settings
     * @param cooldown the cooldown period
     * @param maxMessageAge the maximum age of messages
     * @param minMessages the minimum number of messages
     * @param donorContext whether donor context is enabled
     * @param receiverContext whether receiver context is enabled
     * @param maxGiven the maximum given reputation
     * @param maxGivenHours the hours for max given reputation
     * @param maxReceived the maximum received reputation
     * @param maxReceivedHours the hours for max received reputation
     * @param maxMessageReputation the maximum reputation per message
     */
    public AbuseProtection(Settings settings, int cooldown, int maxMessageAge, int minMessages, boolean donorContext, boolean receiverContext,
                           int maxGiven, int maxGivenHours, int maxReceived, int maxReceivedHours, int maxMessageReputation) {
        this.settings = settings;
        this.cooldown = cooldown;
        this.maxMessageAge = maxMessageAge;
        this.minMessages = minMessages;
        this.donorContext = donorContext;
        this.receiverContext = receiverContext;
        this.maxGiven = maxGiven;
        this.maxGivenHours = maxGivenHours;
        this.maxReceived = maxReceived;
        this.maxReceivedHours = maxReceivedHours;
        this.maxMessageReputation = maxMessageReputation;
    }

    /**
     * Constructs an AbuseProtection object with default settings.
     *
     * @param settings the settings
     */
    public AbuseProtection(Settings settings) {
        this(settings, 30, 30, 10, true, true, 0, 1, 0, 1, 3);
    }

    /**
     * Builds an AbuseProtection object from the database row.
     *
     * @param settings the settings
     * @param rs the database row
     * @return the AbuseProtection object
     * @throws SQLException if a database access error occurs
     */
    public static AbuseProtection build(Settings settings, Row rs) throws SQLException {
        return new AbuseProtection(settings,
                rs.getInt("cooldown"),
                rs.getInt("max_message_age"),
                rs.getInt("min_messages"),
                rs.getBoolean("donor_context"),
                rs.getBoolean("receiver_context"),
                rs.getInt("max_given"),
                rs.getInt("max_given_hours"),
                rs.getInt("max_received"),
                rs.getInt("max_received_hours"),
                rs.getInt("max_message_reputation"));
    }

    /**
     * Gets the cooldown period.
     *
     * @return the cooldown period
     */
    public int cooldown() {
        return cooldown;
    }

    /**
     * Gets the maximum age of messages.
     *
     * @return the maximum age of messages
     */
    public int maxMessageAge() {
        return maxMessageAge;
    }

    /**
     * Gets the minimum number of messages.
     *
     * @return the minimum number of messages
     */
    public int minMessages() {
        return minMessages;
    }

    /**
     * Gets the maximum reputation per message.
     *
     * @return the maximum reputation per message
     */
    public int maxMessageReputation() {
        return maxMessageReputation;
    }

    /**
     * Checks if donor context is enabled.
     *
     * @return true if donor context is enabled, false otherwise
     */
    public boolean isDonorContext() {
        return donorContext;
    }

    /**
     * Checks if receiver context is enabled.
     *
     * @return true if receiver context is enabled, false otherwise
     */
    public boolean isReceiverContext() {
        return receiverContext;
    }

    /**
     * Gets the maximum given reputation.
     *
     * @return the maximum given reputation
     */
    public int maxGiven() {
        return maxGiven;
    }

    /**
     * Gets the hours for max given reputation.
     *
     * @return the hours for max given reputation
     */
    public int maxGivenHours() {
        return maxGivenHours;
    }

    /**
     * Gets the maximum received reputation.
     *
     * @return the maximum received reputation
     */
    public int maxReceived() {
        return maxReceived;
    }

    /**
     * Gets the hours for max received reputation.
     *
     * @return the hours for max received reputation
     */
    public int maxReceivedHours() {
        return maxReceivedHours;
    }

    /**
     * Sets the cooldown period.
     *
     * @param cooldown the cooldown period
     * @return the updated cooldown period
     */
    public int cooldown(int cooldown) {
        if (set("cooldown", stmt -> stmt.bind(cooldown))) {
            this.cooldown = cooldown;
        }
        return this.cooldown;
    }

    /**
     * Sets the maximum age of messages.
     *
     * @param maxMessageAge the maximum age of messages
     * @return the updated maximum age of messages
     */
    public int maxMessageAge(int maxMessageAge) {
        if (set("max_message_age", stmt -> stmt.bind(maxMessageAge))) {
            this.maxMessageAge = maxMessageAge;
        }
        return this.maxMessageAge;
    }

    /**
     * Sets the minimum number of messages.
     *
     * @param minMessages the minimum number of messages
     * @return the updated minimum number of messages
     */
    public int minMessages(int minMessages) {
        if (set("min_messages", stmt -> stmt.bind(minMessages))) {
            this.minMessages = minMessages;
        }
        return this.minMessages;
    }

    /**
     * Sets whether donor context is enabled.
     *
     * @param donorContext the donor context status
     * @return the updated donor context status
     */
    public boolean donorContext(boolean donorContext) {
        if (set("donor_context", stmt -> stmt.bind(donorContext))) {
            this.donorContext = donorContext;
        }
        return this.donorContext;
    }

    /**
     * Sets whether receiver context is enabled.
     *
     * @param receiverContext the receiver context status
     * @return the updated receiver context status
     */
    public boolean receiverContext(boolean receiverContext) {
        if (set("receiver_context", stmt -> stmt.bind(receiverContext))) {
            this.receiverContext = receiverContext;
        }
        return this.receiverContext;
    }

    /**
     * Sets the maximum given reputation.
     *
     * @param maxGiven the maximum given reputation
     * @return the updated maximum given reputation
     */
    public int maxGiven(int maxGiven) {
        var result = set("max_given", stmt -> stmt.bind(Math.max(maxGiven, 0)));
        if (result) {
            this.maxGiven = Math.max(maxGiven, 0);
        }
        return this.maxGiven;
    }

    /**
     * Sets the hours for max given reputation.
     *
     * @param maxGivenHours the hours for max given reputation
     * @return the updated hours for max given reputation
     */
    public int maxGivenHours(int maxGivenHours) {
        var result = set("max_given_hours", stmt -> stmt.bind(Math.max(maxGivenHours, 1)));
        if (result) {
            this.maxGivenHours = Math.max(maxGivenHours, 1);
        }
        return this.maxGivenHours;
    }

    /**
     * Sets the maximum received reputation.
     *
     * @param maxReceived the maximum received reputation
     * @return the updated maximum received reputation
     */
    public int maxReceived(int maxReceived) {
        var result = set("max_received", stmt -> stmt.bind(Math.max(maxReceived, 0)));
        if (result) {
            this.maxReceived = Math.max(maxReceived, 0);
        }
        return this.maxReceived;
    }

    /**
     * Sets the hours for max received reputation.
     *
     * @param maxReceivedHours the hours for max received reputation
     * @return the updated hours for max received reputation
     */
    public int maxReceivedHours(int maxReceivedHours) {
        var result = set("max_received_hours", stmt -> stmt.bind(Math.max(maxReceivedHours, 1)));
        if (result) {
            this.maxReceivedHours = Math.max(maxReceivedHours, 1);
        }
        return this.maxReceivedHours;
    }

    /**
     * Sets the maximum reputation per message.
     *
     * @param maxMessageReputation the maximum reputation per message
     * @return the updated maximum reputation per message
     */
    public int maxMessageReputation(int maxMessageReputation) {
        if (set("max_message_reputation", stmt -> stmt.bind(maxMessageReputation))) {
            this.maxMessageReputation = maxMessageReputation;
        }
        return this.maxMessageReputation;
    }

    /**
     * Checks if a message is older than the maximum message age.
     *
     * @param message the message to check
     * @return true if the message is older than the maximum message age, false otherwise
     */
    public boolean isOldMessage(Message message) {
        if (maxMessageAge == 0) return false;
        var until = message.getTimeCreated().toInstant().until(Instant.now(), ChronoUnit.MINUTES);
        return until >= maxMessageAge();
    }

    /**
     * Checks if the member has reached the max given reputation in the last max given hours.
     *
     * @param member member to check
     * @return true if the limit is reached
     */
    public boolean isDonorLimit(Member member) {
        if (!isDonorLimit()) return false;
        return settings.repGuild().reputation().user(member).countGiven() >= maxGiven;
    }

    /**
     * Checks if the member has reached the max received reputation in the last max received hours.
     *
     * @param member member to check
     * @return true if the limit is reached
     */
    public boolean isReceiverLimit(Member member) {
        if (!isReceiverLimit()) return false;
        return settings.repGuild().reputation().user(member).countReceived() >= maxReceived;
    }

    /**
     * Checks if the donor limit is enabled.
     *
     * @return true if the donor limit is enabled, false otherwise
     */
    public boolean isDonorLimit() {
        return maxGiven != 0;
    }

    /**
     * Checks if the receiver limit is enabled.
     *
     * @return true if the receiver limit is enabled, false otherwise
     */
    public boolean isReceiverLimit() {
        return maxReceived != 0;
    }

    /**
     * Sets a parameter in the database.
     *
     * @param parameter the parameter to set
     * @param builder the function to build the call
     * @return true if the parameter was set successfully, false otherwise
     */
    private boolean set(String parameter, Function<Call, Call> builder) {
        return query("""
                       INSERT INTO abuse_protection(guild_id, %s) VALUES (?, ?)
                       ON CONFLICT(guild_id)
                           DO UPDATE SET %s = excluded.%s;
                       """, parameter, parameter, parameter)
                .single(builder.apply(call().bind(guildId())))
                .insert()
                .changed();
    }

    /**
     * Gets the guild associated with the settings.
     *
     * @return the guild
     */
    @Override
    public Guild guild() {
        return settings.guild();
    }

    /**
     * Gets the ID of the guild associated with the settings.
     *
     * @return the guild ID
     */
    @Override
    public long guildId() {
        return settings.guildId();
    }

    /**
     * Returns a pretty string representation of the abuse protection settings.
     *
     * @return the pretty string
     */
    public String prettyString() {
        return """
               **Context**
               Donor: %s
               Receiver: %s

               **Limits**
               Given: %s in %s hours
               Received: %s in %s hours
               Per Message: %s
               Cooldown: %s

               **Age**
               Max Age: %s minutes
               Min Messages: %s
               """.formatted(donorContext, receiverContext,
                maxGiven, maxGivenHours, maxReceived, maxReceivedHours, maxMessageReputation, cooldown,
                maxMessageAge, minMessages)
                .stripIndent();
    }
}
