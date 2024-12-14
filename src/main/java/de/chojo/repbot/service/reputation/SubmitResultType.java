/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.reputation;

/**
 * Enum representing the different types of submission results.
 */
public enum SubmitResultType {
    /**
     * No donor role found.
     */
    NO_DONOR_ROLE("submitresult.nodonorrole"),

    /**
     * Channel is inactive.
     */
    CHANNEL_INACTIVE("submitresult.channelinactive"),

    /**
     * No targets found.
     */
    NO_TARGETS("submitresult.notargets"),

    /**
     * No recent members found.
     */
    NO_RECENT_MEMBERS("submitresult.norecentmembers"),

    /**
     * Cooldown is active.
     */
    COOLDOWN_ACTIVE("submitresult.cooldownactive"),

    /**
     * Embed send operation.
     */
    EMBED_SEND("submitresult.embedsend"),

    /**
     * Donor limit reached.
     */
    DONOR_LIMIT("submitresult.donorlimit"),

    /**
     * No receiver role found.
     */
    NO_RECEIVER_ROLE("submitresult.noreceiverrole"),

    /**
     * Thank type is disabled.
     */
    THANK_TYPE_DISABLED("submitresult.thanktypedisabled"),

    /**
     * Self vote detected.
     */
    SELF_VOTE("submitresult.selfvote"),

    /**
     * Target not in context.
     */
    TARGET_NOT_IN_CONTEXT("submitresult.targetnotincontext"),

    /**
     * Donor not in context.
     */
    DONOR_NOT_IN_CONTEXT("submitresult.donornotincontext"),

    /**
     * Outdated reference message.
     */
    OUTDATED_REFERENCE_MESSAGE("submitresult.outdatedreferencemessage"),

    /**
     * Outdated message.
     */
    OUTDATED_MESSAGE("submitresult.outdatedmessage"),

    /**
     * Receiver limit reached.
     */
    RECEIVER_LIMIT("submitresult.receiverlimit"),

    /**
     * Already present.
     */
    ALREADY_PRESENT("submitresult.alreadypresent"),

    /**
     * Submitting operation.
     */
    SUBMITTING("submitresult.submitting"),

    /**
     * All cooldowns are active.
     */
    ALL_COOLDOWN("submitresult.allcooldown");

    private final String localeKey;

    /**
     * Constructs a new SubmitResultType with the given locale key.
     *
     * @param localeKey the locale key associated with the submission result type
     */
    SubmitResultType(String localeKey) {
        this.localeKey = localeKey;
    }

    /**
     * Retrieves the locale key associated with the submission result type.
     *
     * @return the locale key
     */
    public String localeKey() {
        return localeKey;
    }
}
