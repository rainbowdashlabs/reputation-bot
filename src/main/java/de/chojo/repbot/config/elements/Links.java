/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements;

/**
 * Configuration class for the links.
 */
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})
public class Links {
    private String tos = "";
    private String invite = "https://discord.com/oauth2/authorize?client_id=834843896579489794&scope=bot&permissions=1342532672";
    private String support = "";
    private String website = "https://rainbowdashlabs.github.io/reputation-bot/";
    private String faq = "https://rainbowdashlabs.github.io/reputation-bot/faq";

    /**
     * Creates a new links configuration with default values.
     */
    public Links(){
    }

    /**
     * Retrieves the Terms of Service (ToS) link.
     *
     * @return the ToS link as a String
     */
    public String tos() {
        return tos;
    }

    /**
     * Retrieves the invite link for the bot.
     *
     * @return the invite link as a String
     */
    public String invite() {
        return invite;
    }

    /**
     * Retrieves the support link.
     *
     * @return the support link as a String
     */
    public String support() {
        return support;
    }

    /**
     * Retrieves the website link.
     *
     * @return the website link as a String
     */
    public String website() {
        return website;
    }

    /**
     * Retrieves the FAQ link.
     *
     * @return the FAQ link as a String
     */
    public String faq() {
        return faq;
    }
}
