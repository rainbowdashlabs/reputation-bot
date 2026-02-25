/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.services.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DiscordGuild(
        String id,
        String name,
        String icon,
        String permissions,
        @JsonProperty("permissions_new") String permissionsNew,
        boolean owner) {}
