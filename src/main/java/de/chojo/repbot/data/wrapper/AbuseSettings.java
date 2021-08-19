package de.chojo.repbot.data.wrapper;

import net.dv8tion.jda.api.entities.Message;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class AbuseSettings {
    private int cooldown;
    private int maxMessageAge;
    private int minMessages;
    private boolean donorContext;
    private boolean receiverContext;

    public AbuseSettings(int cooldown, int maxMessageAge, int minMessages, boolean donorContext, boolean receiverContext) {
        this.cooldown = cooldown;
        this.maxMessageAge = maxMessageAge;
        this.minMessages = minMessages;
        this.donorContext = donorContext;
        this.receiverContext = receiverContext;
    }

    public AbuseSettings() {
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
}
