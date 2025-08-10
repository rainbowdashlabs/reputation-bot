/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.reputation;

public enum SubmitResultType {
    NO_DONOR_ROLE("submitresult.nodonorrole"),
    CHANNEL_INACTIVE("submitresult.channelinactive"),
    NO_TARGETS("submitresult.notargets"),
    NO_RECENT_MEMBERS("submitresult.norecentmembers"),
    // TODO: Switch to old locale key after a few days (10.08.2025)
    COOLDOWN_ACTIVE("submitresult.cooldownactive"),
    EMBED_SEND("submitresult.embedsend"),
    DONOR_LIMIT("submitresult.donorlimit"),
    NO_RECEIVER_ROLE("submitresult.noreceiverrole"),
    THANK_TYPE_DISABLED("submitresult.thanktypedisabled"),
    SELF_VOTE("submitresult.selfvote"),
    TARGET_NOT_IN_CONTEXT("submitresult.targetnotincontext"),
    DONOR_NOT_IN_CONTEXT("submitresult.donornotincontext"),
    OUTDATED_REFERENCE_MESSAGE("submitresult.outdatedreferencemessage"),
    OUTDATED_MESSAGE("submitresult.outdatedmessage"),
    RECEIVER_LIMIT("submitresult.receiverlimit"),
    ALREADY_PRESENT("submitresult.alreadypresent"),
    SUBMITTING("submitresult.submitting"),
    ALL_COOLDOWN("submitresult.allcooldown");

    private final String localeKey;

    SubmitResultType(String localeKey) {
        this.localeKey = localeKey;
    }

    public String localeKey() {
        return localeKey;
    }
}
