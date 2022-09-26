package de.chojo.repbot.dao.access.guild.settings.sub;

import de.chojo.jdautil.consumer.ThrowingConsumer;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sadu.base.QueryFactory;
import de.chojo.sadu.wrapper.util.ParamBuilder;
import de.chojo.sadu.wrapper.util.Row;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class AbuseProtection extends QueryFactory implements GuildHolder {
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

    public AbuseProtection(Settings settings, int cooldown, int maxMessageAge, int minMessages, boolean donorContext, boolean receiverContext,
                           int maxGiven, int maxGivenHours, int maxReceived, int maxReceivedHours, int maxMessageReputation) {
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
        this.maxMessageReputation = maxMessageReputation;
    }

    public AbuseProtection(Settings settings) {
        this(settings, 30, 30, 10, true, true, 0, 1, 0, 1, 3);
    }

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

    public int cooldown() {
        return cooldown;
    }

    public int maxMessageAge() {
        return maxMessageAge;
    }

    public int minMessages() {
        return minMessages;
    }

    public int maxMessageReputation() {
        return maxMessageReputation;
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
        if (set("cooldown", stmt -> stmt.setInt(cooldown))) {
            this.cooldown = cooldown;
        }
        return this.cooldown;
    }

    public int maxMessageAge(int maxMessageAge) {
        if (set("max_message_age", stmt -> stmt.setInt(maxMessageAge))) {
            this.maxMessageAge = maxMessageAge;
        }
        return this.maxMessageAge;
    }

    public int minMessages(int minMessages) {
        if (set("min_messages", stmt -> stmt.setInt(minMessages))) {
            this.minMessages = minMessages;
        }
        return this.minMessages;
    }

    public boolean donorContext(boolean donorContext) {
        if (set("donor_context", stmt -> stmt.setBoolean(donorContext))) {
            this.donorContext = donorContext;
        }
        return this.donorContext;
    }

    public boolean receiverContext(boolean receiverContext) {
        if (set("receiver_context", stmt -> stmt.setBoolean(receiverContext))) {
            this.receiverContext = receiverContext;
        }
        return this.receiverContext;
    }

    public int maxGiven(int maxGiven) {
        var result = set("max_given", stmt -> stmt.setInt(Math.max(maxGiven, 0)));
        if (result) {
            this.maxGiven = Math.max(maxGiven, 0);
        }
        return this.maxGiven;
    }

    public int maxGivenHours(int maxGivenHours) {
        var result = set("max_given_hours", stmt -> stmt.setInt(Math.max(maxGivenHours, 1)));
        if (result) {
            this.maxGivenHours = Math.max(maxGivenHours, 1);
        }
        return this.maxGivenHours;
    }

    public int maxReceived(int maxReceived) {
        var result = set("max_received", stmt -> stmt.setInt(Math.max(maxReceived, 0)));
        if (result) {
            this.maxReceived = Math.max(maxReceived, 0);
        }
        return this.maxReceived;
    }

    public int maxReceivedHours(int maxReceivedHours) {
        var result = set("max_received_hours", stmt -> stmt.setInt(Math.max(maxReceivedHours, 1)));
        if (result) {
            this.maxReceivedHours = Math.max(maxReceivedHours, 1);
        }
        return this.maxReceivedHours;
    }

    public int maxMessageReputation(int maxMessageReputation) {
        if (set("max_message_reputation", stmt -> stmt.setInt(maxMessageReputation))) {
            this.maxMessageReputation = maxMessageReputation;
        }
        return this.maxMessageReputation;
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

    public boolean isDonorLimit() {
        return maxGiven != 0;
    }

    public boolean isReceiverLimit() {
        return maxReceived != 0;
    }

    private boolean set(String parameter, ThrowingConsumer<ParamBuilder, SQLException> builder) {
        return builder()
                .query("""
                       INSERT INTO abuse_protection(guild_id, %s) VALUES (?, ?)
                       ON CONFLICT(guild_id)
                           DO UPDATE SET %s = excluded.%s;
                       """, parameter, parameter, parameter)
                .parameter(stmts -> {
                    stmts.setLong(guildId());
                    builder.accept(stmts);
                }).insert()
                .sendSync()
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
