package de.chojo.repbot.dao.access.guild.settings.sub;

import de.chojo.jdautil.consumer.ThrowingConsumer;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.wrapper.ParamBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class AbuseProtection extends QueryFactoryHolder implements GuildHolder {
    private Settings settings;
    private int cooldown;
    private int maxMessageAge;
    private int minMessages;
    private boolean donorContext;
    private boolean receiverContext;

    public AbuseProtection(Settings settings, int cooldown, int maxMessageAge, int minMessages, boolean donorContext, boolean receiverContext) {
        super(settings);
        this.settings = settings;
        this.cooldown = cooldown;
        this.maxMessageAge = maxMessageAge;
        this.minMessages = minMessages;
        this.donorContext = donorContext;
        this.receiverContext = receiverContext;
    }

    public AbuseProtection(Settings settings) {
        this(settings, 30, 30, 10, true, true);
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

    public boolean cooldown(int cooldown) {
        var result = set("cooldown", stmt -> stmt.setInt(cooldown));
        if (result) {
        this.cooldown = cooldown;
        }
        return result;
    }

    public boolean maxMessageAge(int maxMessageAge) {
        var result = set("max_message_age", stmt -> stmt.setInt(maxMessageAge));
        if (result) {
            this.maxMessageAge = maxMessageAge;
        }
        return result;
    }

    public boolean minMessages(int minMessages) {
        var result = set("min_messages", stmt -> stmt.setInt(minMessages));
        if (result) {
            this.minMessages = minMessages;
        }
        return result;
    }

    public boolean donorContext(boolean donorContext) {
        var result = set("donor_context", stmt -> stmt.setBoolean(donorContext));
        if (result) {
            this.donorContext = donorContext;
        }
        return result;
    }

    public boolean receiverContext(boolean receiverContext) {
        var result = set("receiver_context", stmt -> stmt.setBoolean(receiverContext));
        if (result) {
            this.receiverContext = receiverContext;
        }
        return result;
    }

    public boolean isFreshMessage(Message message) {
        if (maxMessageAge == 0) return true;
        var until = message.getTimeCreated().toInstant().until(Instant.now(), ChronoUnit.MINUTES);
        return until < maxMessageAge();
    }

    public static AbuseProtection build(Settings dataSource, ResultSet rs) throws SQLException {
        return new AbuseProtection(dataSource,
                rs.getInt("cooldown"),
                rs.getInt("max_message_age"),
                rs.getInt("min_messages"),
                rs.getBoolean("donor_context"),
                rs.getBoolean("receiver_context"));
    }

    private boolean set(String parameter, ThrowingConsumer<ParamBuilder, SQLException> builder) {
        return builder()
                       .query("""
                               INSERT INTO message_settings(guild_id, %s) VALUES (?, ?)
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
