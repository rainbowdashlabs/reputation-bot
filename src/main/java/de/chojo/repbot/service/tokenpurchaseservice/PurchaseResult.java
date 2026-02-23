/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.tokenpurchaseservice;

public class PurchaseResult {
    boolean success;
    FailureReason reason;

    public PurchaseResult(boolean success, FailureReason reason) {
        this.success = success;
        this.reason = reason;
    }

    public boolean success() {
        return success;
    }

    public FailureReason reason() {
        return reason;
    }

    public static final PurchaseResult SUCCESS = new PurchaseResult(true, null);

    public static PurchaseResult failed(FailureReason reason) {
        return new PurchaseResult(false, reason);
    }
}
