package de.chojo.repbot.web.pojo;

import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.web.pojo.guild.GuildPOJO;
import de.chojo.repbot.web.pojo.settings.SettingsPOJO;
import net.dv8tion.jda.api.entities.Guild;

public class GuildSessionPOJO {
    SettingsPOJO settings;
    GuildPOJO guild;

    public GuildSessionPOJO(SettingsPOJO settings, GuildPOJO guild) {
        this.settings = settings;
        this.guild = guild;
    }

    public static GuildSessionPOJO generate(Guild guild, GuildRepository guildRepository) {
        return new GuildSessionPOJO(SettingsPOJO.generate(guild, guildRepository), GuildPOJO.generate(guild));
    }
}
