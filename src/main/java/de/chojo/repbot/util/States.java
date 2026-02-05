/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.util;

import de.chojo.jdautil.util.SysVar;

public final class States {
    public static final boolean TEST_MODE =
            Boolean.parseBoolean(SysVar.envOrProp("CJDA_INTERACTIONS_TESTMODE", "cjda.interactions.testmode", "false"));
    public static final boolean GRANT_ALL_SKU =
            Boolean.parseBoolean(SysVar.envOrProp("BOT_GRANT_ALL_SKU", "bot.grantallsku", "false"));
}
