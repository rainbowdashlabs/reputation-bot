/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.core;

import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.util.Premium;
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
                             .addLanguage(
                                     DiscordLocale.ENGLISH_US, // en-US
                                     DiscordLocale.GERMAN, // de
                                     DiscordLocale.SPANISH, // es-ES
                                     DiscordLocale.FRENCH, // fr
                                     DiscordLocale.PORTUGUESE_BRAZILIAN, // pt-BR
                                     DiscordLocale.RUSSIAN, // ru
                                     DiscordLocale.UKRAINIAN, // uk
                                     DiscordLocale.DUTCH, // nl
                                     DiscordLocale.ITALIAN, // it
                                     DiscordLocale.GREEK, // el
                                     DiscordLocale.TURKISH, // tr
                                     DiscordLocale.CHINESE_CHINA, // zh-CN
                                     DiscordLocale.CZECH, // cs
                                     DiscordLocale.POLISH, // pl
                                     DiscordLocale.KOREAN, // ko
                                     DiscordLocale.NORWEGIAN, // no
                                     DiscordLocale.FINNISH, // fi
                                     DiscordLocale.SWEDISH, // sv-SE
                                     DiscordLocale.JAPANESE // ja
                             )
                             .withLanguageProvider(guild -> data.guildRepository().guild(guild).settings().general().language())
                             .withGuildLocaleCodeProvider((guild, code) -> {
                                 if (!"words.reputation".equals(code)) return Optional.empty();
                                 if (Premium.isNotEntitled(data.guildRepository().guild(guild).subscriptions(), configuration.skus().features().localeOverrides().reputationNameOverride())) {
                                     return Optional.empty();
                                 }
                                 return data.guildRepository().guild(guild).localeOverrides().getOverride(code);
                             })
                             .withBundlePath("locale")
                             .build();
    }

    public Localizer localizer() {
        return localizer;
    }
}
