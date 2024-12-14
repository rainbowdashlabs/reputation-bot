/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.core;

import de.chojo.jdautil.localization.Localizer;
import net.dv8tion.jda.api.interactions.DiscordLocale;

/**
 * Manages localization settings and provides a Localizer instance.
 */
public class Localization {
    private final Data data;
    private Localizer localizer;

    /**
     * Constructs a new Localization instance with the specified data.
     *
     * @param data the data provider
     */
    private Localization(Data data) {
        this.data = data;
    }

    /**
     * Creates and initializes a new Localization instance.
     *
     * @param data the data provider
     * @return a new Localization instance
     */
    public static Localization create(Data data) {
        var localization = new Localization(data);
        localization.init();
        return localization;
    }

    /**
     * Initializes the Localizer with supported languages and settings.
     */
    public void init() {
        localizer = Localizer.builder(DiscordLocale.ENGLISH_US)
                .addLanguage(DiscordLocale.GERMAN,
                        DiscordLocale.SPANISH,
                        DiscordLocale.FRENCH,
                        DiscordLocale.PORTUGUESE_BRAZILIAN,
                        DiscordLocale.RUSSIAN)
                .withLanguageProvider(guild -> data.guilds().guild(guild).settings().general().language())
                .withBundlePath("locale")
                .build();
    }

    /**
     * Returns the Localizer instance.
     *
     * @return the Localizer instance
     */
    public Localizer localizer() {
        return localizer;
    }
}
