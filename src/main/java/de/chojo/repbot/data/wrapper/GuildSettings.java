package de.chojo.repbot.data.wrapper;

import net.dv8tion.jda.api.entities.Guild;

public class GuildSettings {
    private final Guild guild;
    private final GeneralSettings generalSettings;
    private final MessageSettings messageSettings;
    private final AbuseSettings abuseSettings;
    private final ThankSettings thankSettings;
    private final AnnouncementSettings announcementSettings;


    public GuildSettings(Guild guild, GeneralSettings generalSettings, MessageSettings messageSettings, AbuseSettings abuseSettings, ThankSettings thankSettings, AnnouncementSettings announcementSettings) {
        this.guild = guild;
        this.generalSettings = generalSettings;
        this.messageSettings = messageSettings;
        this.abuseSettings = abuseSettings;
        this.thankSettings = thankSettings;
        this.announcementSettings = announcementSettings;
    }

    public Guild guild() {
        return guild;
    }

    public AbuseSettings abuseSettings() {
        return abuseSettings;
    }

    public GeneralSettings generalSettings() {
        return generalSettings;
    }

    public MessageSettings messageSettings() {
        return messageSettings;
    }

    public ThankSettings thankSettings() {
        return thankSettings;
    }

    public AnnouncementSettings announcementSettings() {
        return announcementSettings;
    }
}
