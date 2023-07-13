/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements;

import de.chojo.jdautil.botlist.BotListConfig;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class Botlist {
    private boolean submit;
    private BotListConfig topGg = new BotListConfig("top.gg", "", "", 264445053596991498L, "", "");
    private BotListConfig discordBotsGg = new BotListConfig("discord.bots.gg", "", "", 110373943822540800L, "", "");
    private BotListConfig discordBotlistCom = new BotListConfig("discordbotlist.com", "", "", 450100127256936458L, "", "");
    private BotListConfig botListMe = new BotListConfig("discordbotlist.com", "", "", 698637043240009738L, "", "");

    public boolean isSubmit() {
        return submit;
    }

    public BotListConfig topGg() {
        return topGg;
    }

    public BotListConfig discordBotsGg() {
        return discordBotsGg;
    }

    public BotListConfig discordBotlistCom() {
        return discordBotlistCom;
    }

    public BotListConfig botListMe() {
        return discordBotlistCom;
    }

    public boolean isBotlistGuild(long id) {
        for (var botlist : botlists()) {
            if (botlist.guildId() == id) {
                return true;
            }
        }

        return false;
    }

    public BotListConfig[] botlists() {
        return new BotListConfig[]{topGg, discordBotsGg, discordBotlistCom, botListMe};
    }
}
