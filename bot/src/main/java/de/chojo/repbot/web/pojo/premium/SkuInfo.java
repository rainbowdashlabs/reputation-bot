/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.premium;

/**
 * Information about a SKU that can unlock a premium feature.
 */
public class SkuInfo {
    private final String id;
    private final String name;

    public SkuInfo(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }
}
