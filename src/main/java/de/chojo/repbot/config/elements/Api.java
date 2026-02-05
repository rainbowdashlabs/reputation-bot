/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements;

import de.chojo.jdautil.util.SysVar;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "MismatchedReadAndWriteOfArray"})
public class Api {
    private String host = "0.0.0.0";
    private int port = 8888;
    private String url = "https://repbot.chojo.de";
    private int tokenValidHours = 720; // 30 Days

    public String host() {
        return SysVar.envOrProp("BOT_API_HOST", "bot.api.host", host);
    }

    public int port() {
        return Integer.parseInt(SysVar.envOrProp("BOT_API_PORT", "bot.api.port", String.valueOf(port)));
    }

    public String url() {
        return SysVar.envOrProp("BOT_API_URL", "bot.api.url", url);
    }

    public int tokenValidHours() {
        return tokenValidHours;
    }
}
