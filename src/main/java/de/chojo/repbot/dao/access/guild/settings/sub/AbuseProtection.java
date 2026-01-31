/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub;

import com.fasterxml.jackson.annotation.JsonSerializeAs;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.service.reputation.ReputationContext;
import de.chojo.repbot.web.pojo.settings.sub.AbuseProtectionPOJO;
import de.chojo.repbot.web.pojo.settings.sub.thanking.ThankwordsPOJO;
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

@JsonSerializeAs(AbuseProtectionPOJO.class)
public class AbuseProtection extends AbuseProtectionPOJO implements GuildHolder {
    private final Settings settings;

    public AbuseProtection(Settings settings, int cooldown, CooldownDirection cooldownDirection, int maxMessageAge, int minMessages, boolean donorContext, boolean receiverContext,
                           int maxGiven, int maxGivenHours, int maxReceived, int maxReceivedHours, int maxMessageReputation) {
        super(cooldown, cooldownDirection, maxMessageAge, minMessages, donorContext, receiverContext, maxGiven, maxGivenHours, maxReceived, maxReceivedHours, maxMessageReputation);
        this.settings = settings;
    }

    public AbuseProtection(Settings settings) {
        this(settings, 30, CooldownDirection.BIDIRECTIONAL, 30, 10, true, true, 0, 1, 0, 1, 3);
    }

    public static AbuseProtection build(Settings settings, Row rs) throws SQLException {
        return new AbuseProtection(settings,
                rs.getInt("cooldown"),
                rs.getEnum("cooldown_direction", CooldownDirection.class),
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

    public int cooldown(int cooldown) {
        if (set("cooldown", stmt -> stmt.bind(cooldown))) {
            this.cooldown = cooldown;
        }
        return this.cooldown;
    }

    public CooldownDirection cooldownDirection(CooldownDirection cooldownDirection) {
        if (set("cooldown_direction", stmt -> stmt.bind(cooldownDirection))) {
            this.cooldownDirection = cooldownDirection;
        }
        return this.cooldownDirection;
    }

    public int maxMessageAge(int maxMessageAge) {
        if (set("max_message_age", stmt -> stmt.bind(maxMessageAge))) {
            this.maxMessageAge = maxMessageAge;
        }
        return this.maxMessageAge;
    }

    public int minMessages(int minMessages) {
        if (set("min_messages", stmt -> stmt.bind(minMessages))) {
            this.minMessages = minMessages;
        }
        return this.minMessages;
    }

    public boolean donorContext(boolean donorContext) {
        if (set("donor_context", stmt -> stmt.bind(donorContext))) {
            this.donorContext = donorContext;
        }
        return this.donorContext;
    }

    public boolean receiverContext(boolean receiverContext) {
        if (set("receiver_context", stmt -> stmt.bind(receiverContext))) {
            this.receiverContext = receiverContext;
        }
        return this.receiverContext;
    }

    public int maxGiven(int maxGiven) {
        var result = set("max_given", stmt -> stmt.bind(Math.max(maxGiven, 0)));
        if (result) {
            this.maxGiven = Math.max(maxGiven, 0);
        }
        return this.maxGiven;
    }

    public int maxGivenHours(int maxGivenHours) {
        var result = set("max_given_hours", stmt -> stmt.bind(Math.max(maxGivenHours, 1)));
        if (result) {
            this.maxGivenHours = Math.max(maxGivenHours, 1);
        }
        return this.maxGivenHours;
    }

    public int maxReceived(int maxReceived) {
        var result = set("max_received", stmt -> stmt.bind(Math.max(maxReceived, 0)));
        if (result) {
            this.maxReceived = Math.max(maxReceived, 0);
        }
        return this.maxReceived;
    }

    public int maxReceivedHours(int maxReceivedHours) {
        var result = set("max_received_hours", stmt -> stmt.bind(Math.max(maxReceivedHours, 1)));
        if (result) {
            this.maxReceivedHours = Math.max(maxReceivedHours, 1);
        }
        return this.maxReceivedHours;
    }

    public int maxMessageReputation(int maxMessageReputation) {
        if (set("max_message_reputation", stmt -> stmt.bind(maxMessageReputation))) {
            this.maxMessageReputation = maxMessageReputation;
        }
        return this.maxMessageReputation;
    }

    public boolean isOldMessage(ReputationContext context) {
        if(context.isInteraction()) return false;
        return isOldMessage(context.asMessage());
    }

    public boolean isOldMessage(Message message) {
        if (maxMessageAge == 0) return false;
        var until = message.getTimeCreated().toInstant().until(Instant.now(), ChronoUnit.MINUTES);
        return until >= maxMessageAge();
    }

    /**
     * Checks if the member has reached the {@link #maxGiven} amount of reputation in the last {@link #maxGivenHours}.
     *
     * @param member member to check
     * @return true if the limit is reached
     */
    public boolean isDonorLimit(Member member) {
        if (!isDonorLimit()) return false;
        return settings.repGuild().reputation().user(member).countGiven() >= maxGiven;
    }

    /**
     * Checks if the member has reached the {@link #maxReceived} amount of reputation in the last {@link #maxReceivedHours}.
     *
     * @param member member to check
     * @return true if the limit is reached
     */
    public boolean isReceiverLimit(Member member) {
        if (!isReceiverLimit()) return false;
        return settings.repGuild().reputation().user(member).countReceived() >= maxReceived;
    }

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

    @Override
    public Guild guild() {
        return settings.guild();
    }

    @Override
    public long guildId() {
        return settings.guildId();
    }

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
