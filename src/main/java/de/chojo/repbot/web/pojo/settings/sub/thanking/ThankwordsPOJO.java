/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub.thanking;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Set;

public class ThankwordsPOJO {
    protected final Set<String> thankwords;

    /**
     * All values are required. A partial body is rejected instead of silently resetting the omitted settings, because
     * the settings are applied by difference.
     */
    @JsonCreator
    public ThankwordsPOJO(@JsonProperty(value = "thankwords", required = true) Set<String> thankwords) {
        this.thankwords = thankwords;
    }

    public Set<String> words() {
        return Collections.unmodifiableSet(thankwords);
    }
}
