package de.chojo.repbot.dao.access.settings;

import de.chojo.sqlutil.base.QueryFactoryHolder;
import net.dv8tion.jda.api.entities.Message;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class AbuseSettings extends QueryFactoryHolder {
    private int cooldown;
    private int maxMessageAge;
    private int minMessages;
    private boolean donorContext;
    private boolean receiverContext;

    public AbuseSettings(DataSource dataSource, int cooldown, int maxMessageAge, int minMessages, boolean donorContext, boolean receiverContext) {
        super(dataSource);
        this.cooldown = cooldown;
        this.maxMessageAge = maxMessageAge;
        this.minMessages = minMessages;
        this.donorContext = donorContext;
        this.receiverContext = receiverContext;
    }

    public AbuseSettings(DataSource dataSource) {
        this(dataSource, 30, 30, 10, true, true);
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

    public void cooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public void maxMessageAge(int maxMessageAge) {
        this.maxMessageAge = maxMessageAge;
    }

    public void minMessages(int minMessages) {
        this.minMessages = minMessages;
    }

    public void donorContext(boolean donorContext) {
        this.donorContext = donorContext;
    }

    public void receiverContext(boolean receiverContext) {
        this.receiverContext = receiverContext;
    }

    public boolean isFreshMessage(Message message) {
        if (maxMessageAge == 0) return true;
        var until = message.getTimeCreated().toInstant().until(Instant.now(), ChronoUnit.MINUTES);
        return until < maxMessageAge();
    }

    public static AbuseSettings build(DataSource dataSource, ResultSet rs) throws SQLException {
        return new AbuseSettings(dataSource,
                rs.getInt("cooldown"),
                rs.getInt("max_message_age"),
                rs.getInt("min_messages"),
                rs.getBoolean("donor_context"),
                rs.getBoolean("receiver_context"));
    }

}
