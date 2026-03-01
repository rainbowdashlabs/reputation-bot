/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements;

import de.chojo.jdautil.util.SysVar;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class DiscordOAuth {
    private String clientId = "";
    private String clientSecret = "";
    private String redirectUri = "";

    public String clientId() {
        return SysVar.envOrProp("BOT_DISCORD_CLIENT_ID", "bot.discord.client.id", clientId);
    }

    public String clientSecret() {
        return SysVar.envOrProp("BOT_DISCORD_CLIENT_SECRET", "bot.discord.client.secret", clientSecret);
    }

    public String redirectUri() {
        return SysVar.envOrProp("BOT_DISCORD_REDIRECT_URI", "bot.discord.redirect.uri", redirectUri);
    }
}
