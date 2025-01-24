/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements;

import de.chojo.jdautil.botlist.BotListConfig;

/**
 * Configuration class for botlist settings.
 */
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class Botlist {
    /**
     * Indicates whether submissions are enabled.
     */
    private boolean submit;

    /**
     * Configuration for top.gg.
     */
    private BotListConfig topGg = new BotListConfig("top.gg", "", "", 264445053596991498L, "", "");

    /**
     * Configuration for discord.bots.gg.
     */
    private BotListConfig discordBotsGg = new BotListConfig("discord.bots.gg", "", "", 110373943822540800L, "", "");

    /**
     * Configuration for discordbotlist.com.
     */
    private BotListConfig discordBotlistCom = new BotListConfig("discordbotlist.com", "", "", 450100127256936458L, "", "");

    /**
     * Configuration for botlist.me.
     */
    private BotListConfig botListMe = new BotListConfig("discordbotlist.com", "", "", 698637043240009738L, "", "");

    /**
     * Creates a new botlist configuration with default values.
     */
    public Botlist(){
    }

    /**
     * Checks if submissions are enabled.
     *
     * @return true if submissions are enabled, false otherwise
     */
    public boolean isSubmit() {
        return submit;
    }

    /**
     * Gets the configuration for top.gg.
     *
     * @return the top.gg configuration
     */
    public BotListConfig topGg() {
        return topGg;
    }

    /**
     * Gets the configuration for discord.bots.gg.
     *
     * @return the discord.bots.gg configuration
     */
    public BotListConfig discordBotsGg() {
        return discordBotsGg;
    }

    /**
     * Gets the configuration for discordbotlist.com.
     *
     * @return the discordbotlist.com configuration
     */
    public BotListConfig discordBotlistCom() {
        return discordBotlistCom;
    }

    /**
     * Gets the configuration for botlist.me.
     *
     * @return the botlist.me configuration
     */
    public BotListConfig botListMe() {
        return discordBotlistCom;
    }

    /**
     * Checks if the given ID matches any botlist guild ID.
     *
     * @param id the ID to check
     * @return true if the ID matches a botlist guild ID, false otherwise
     */
    public boolean isBotlistGuild(long id) {
        for (var botlist : botlists()) {
            if (botlist.guildId() == id) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets an array of all botlist configurations.
     *
     * @return an array of botlist configurations
     */
    public BotListConfig[] botlists() {
        return new BotListConfig[]{topGg, discordBotsGg, discordBotlistCom, botListMe};
    }
}
