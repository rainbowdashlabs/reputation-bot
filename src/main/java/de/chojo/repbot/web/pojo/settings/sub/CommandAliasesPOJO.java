/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom command names of a guild, keyed by the name of the command they replace or extend.
 */
public class CommandAliasesPOJO {
    protected final Map<String, String> aliases = new HashMap<>();
}
