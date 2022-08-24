package de.chojo.repbot.core;

import de.chojo.jdautil.localization.Localizer;
import net.dv8tion.jda.api.interactions.DiscordLocale;

public class Localization {
    private final Data data;
    private Localizer localizer;

    private Localization(Data data) {
        this.data = data;
    }

    public static Localization create(Data data) {
        var localization = new Localization(data);
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
                .withBundlePath("locale")
                .build();
    }

    public Localizer localizer() {
        return localizer;
    }
}
