package de.chojo.repbot.data.wrapper;

import de.chojo.repbot.dao.access.guild.settings.sub.General;
import de.chojo.repbot.dao.access.guild.settings.sub.Messages;
import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import net.dv8tion.jda.api.entities.Guild;

public class GuildSettings {
    private final Guild guild;
    private final General generalSettings;
    private final Messages messageSettings;
    private final AbuseSettings abuseSettings;
    private final Thanking thankSettings;


    public GuildSettings(Guild guild, General general, Messages messages, AbuseSettings abuseSettings, Thanking thanking) {
        this.guild = guild;
        this.generalSettings = general;
        this.messageSettings = messages;
        this.abuseSettings = abuseSettings;
        this.thankSettings = thanking;
    }


    public Guild guild() {
        return guild;
    }

    public AbuseSettings abuseSettings() {
        return abuseSettings;
    }

    public General generalSettings() {
        return generalSettings;
    }

    public Messages messageSettings() {
        return messageSettings;
    }

    public Thanking thankSettings() {
        return thankSettings;
    }
}
