/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.core;

import de.chojo.jdautil.localization.Localizer;
import de.chojo.repbot.config.Configuration;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.Optional;

public class Localization {
    private final Configuration configuration;
    private final Data data;
    private Localizer localizer;

    private Localization(Data data, Configuration configuration) {
        this.data = data;
        this.configuration = configuration;
    }

    public static Localization create(Data data, Configuration configuration) {
        var localization = new Localization(data, configuration);
        localization.init();
        return localization;
    }

    public void init() {
        localizer = Localizer.builder(DiscordLocale.ENGLISH_US)
                             .addLanguage(DiscordLocale.GERMAN,
                                     DiscordLocale.SPANISH,
                                     DiscordLocale.FRENCH,
                                     DiscordLocale.PORTUGUESE_BRAZILIAN,
                                     DiscordLocale.RUSSIAN)
                             .withLanguageProvider(guild -> data.guilds().guild(guild).settings().general().language())
                             .withGuildLocaleCodeProvider((guild, code) -> {
                                 if ("words.reputation".equals(code)
                                         && !configuration.skus().features().localeOverrides().reputationNameOverride().isEntitled(data.guilds().guild(guild).subscriptions())) {
                                     return Optional.empty();
                                 }
                                 return data.guilds().guild(guild).localeOverrides().getOverride(code);
                             })
                             .withBundlePath("locale")
                             .build();
    }

    public Localizer localizer() {
        return localizer;
    }
}
