/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub;

import de.chojo.repbot.dao.access.guild.settings.sub.CooldownDirection;

public class AbuseProtectionPOJO {
    protected int cooldown;
    protected CooldownDirection cooldownDirection;
    protected int maxMessageAge;
    protected int minMessages;
    protected boolean donorContext;
    protected boolean receiverContext;
    protected int maxGiven;
    protected int maxGivenHours;
    protected int maxReceived;
    protected int maxReceivedHours;
    protected int maxMessageReputation;

    public AbuseProtectionPOJO(int cooldown, CooldownDirection cooldownDirection, int maxMessageAge, int minMessages, boolean donorContext, boolean receiverContext, int maxGiven, int maxGivenHours, int maxReceived, int maxReceivedHours, int maxMessageReputation) {
        this.cooldown = cooldown;
        this.cooldownDirection = cooldownDirection;
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
     * Gets the cooldown in minutes.
     * A cooldown of 0 means no cooldown.
     * A negative cooldown means the cooldown is forever.
     *
     * @return cooldown in minutes
     */
    public int cooldown() {
        return cooldown;
    }

    /**
     * Gets the cooldown direction.
     * The direction defines if the cooldown is between both users, preventing backthanking or not.
     *
     * @return cooldown direction
     */
    public CooldownDirection cooldownDirection() {
        return cooldownDirection;
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

    public boolean isDonorLimit() {
        return maxGiven != 0;
    }

    public boolean isReceiverLimit() {
        return maxReceived != 0;
    }
}
