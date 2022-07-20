package de.chojo.repbot.dao.access.guild.settings.sub;

import de.chojo.jdautil.consumer.ThrowingConsumer;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.wrapper.ParamBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class AbuseProtection extends QueryFactoryHolder implements GuildHolder {
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

    public AbuseProtection(Settings settings, int cooldown, int maxMessageAge, int minMessages, boolean donorContext, boolean receiverContext,
                           int maxGiven, int maxGivenHours, int maxReceived, int maxReceivedHours) {
        super(settings);
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
    }

    public AbuseProtection(Settings settings) {
        this(settings, 30, 30, 10, true, true, 0, 1, 0, 1);
    }

    public static AbuseProtection build(Settings settings, ResultSet rs) throws SQLException {
        return new AbuseProtection(settings,
                rs.getInt("cooldown"),
                rs.getInt("max_message_age"),
                rs.getInt("min_messages"),
                rs.getBoolean("donor_context"),
                rs.getBoolean("receiver_context"),
                rs.getInt("max_given"),
                rs.getInt("max_given_hours"),
                rs.getInt("max_received"),
                rs.getInt("max_received_hours"));
    }

    public int cooldown() {
        return cooldown;
    }

    public int maxMessageAge() {
        return maxMessageAge;
    }

    public int minMessages() {
        return minMessages;
    }

    public boolean isDonorContext() {
        return donorContext;
    }

    public boolean isReceiverContext() {
        return receiverContext;
    }

    public int maxGiven() {
        return maxGiven;
    }

    public int maxGivenHours() {
        return maxGivenHours;
    }

    public int maxReceived() {
        return maxReceived;
    }

    public int maxReceivedHours() {
        return maxReceivedHours;
    }

    public int cooldown(int cooldown) {
        var result = set("cooldown", stmt -> stmt.setInt(cooldown));
        if (result) {
            this.cooldown = cooldown;
        }
        return this.cooldown;
    }

    public int maxMessageAge(int maxMessageAge) {
        var result = set("max_message_age", stmt -> stmt.setInt(maxMessageAge));
        if (result) {
            this.maxMessageAge = maxMessageAge;
        }
        return this.maxMessageAge;
    }

    public int minMessages(int minMessages) {
        var result = set("min_messages", stmt -> stmt.setInt(minMessages));
        if (result) {
            this.minMessages = minMessages;
        }
        return this.minMessages;
    }

    public boolean donorContext(boolean donorContext) {
        var result = set("donor_context", stmt -> stmt.setBoolean(donorContext));
        if (result) {
            this.donorContext = donorContext;
        }
        return this.donorContext;
    }

    public boolean receiverContext(boolean receiverContext) {
        var result = set("receiver_context", stmt -> stmt.setBoolean(receiverContext));
        if (result) {
            this.receiverContext = receiverContext;
        }
        return this.receiverContext;
    }

    public int maxGiven(int maxGiven) {
        var result = set("max_given", stmt -> stmt.setInt(Math.max(maxGiven, 0)));
        if (result) {
            this.maxGiven = maxGiven;
        }
        return this.maxGiven;
    }

    public int maxGivenHours(int maxGivenHours) {
        var result = set("max_given_hours", stmt -> stmt.setInt(Math.max(maxGivenHours, 1)));
        if (result) {
            this.maxGivenHours = maxGivenHours;
        }
        return this.maxGivenHours;
    }

    public int maxReceived(int maxReceived) {
        var result = set("max_received", stmt -> stmt.setInt(Math.max(maxReceived, 0)));
        if (result) {
            this.maxReceived = maxReceived;
        }
        return this.maxReceived;
    }

    public int maxReceivedHours(int maxReceivedHours) {
        var result = set("max_received_hours", stmt -> stmt.setInt(Math.max(maxReceivedHours, 1)));
        if (result) {
            this.maxReceivedHours = maxReceivedHours;
        }
        return this.maxReceivedHours;
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
        if (maxGiven == 0) return false;
        return settings.repGuild().reputation().user(member).countGiven() >= maxGiven;
    }

    /**
     * Checks if the member has reached the {@link #maxReceived} amount of reputation in the last {@link #maxReceivedHours}.
     *
     * @param member member to check
     * @return true if the limit is reached
     */
    public boolean isReceiverLimit(Member member) {
        if (maxGiven == 0) return false;
        return settings.repGuild().reputation().user(member).countReceived() >= maxReceived;
    }

    private boolean set(String parameter, ThrowingConsumer<ParamBuilder, SQLException> builder) {
        return builder()
                       .query("""
                               INSERT INTO abuse_protection(guild_id, %s) VALUES (?, ?)
                               ON CONFLICT(guild_id)
                                   DO UPDATE SET %s = excluded.%s;
                               """, parameter, parameter, parameter)
                       .paramsBuilder(stmts -> {
                           stmts.setLong(guildId());
                           builder.accept(stmts);
                       }).insert()
                       .executeSync() > 0;
    }

    @Override
    public Guild guild() {
        return settings.guild();
    }
}
