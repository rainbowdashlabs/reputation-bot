package de.chojo.repbot.web.pojo.settings;

import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.web.pojo.settings.sub.AbuseProtectionPOJO;
import de.chojo.repbot.web.pojo.settings.sub.AnnouncementsPOJO;
import de.chojo.repbot.web.pojo.settings.sub.AutopostPOJO;
import de.chojo.repbot.web.pojo.settings.sub.GeneralPOJO;
import de.chojo.repbot.web.pojo.settings.sub.LogChannelPOJO;
import de.chojo.repbot.web.pojo.settings.sub.MessagesPOJO;
import de.chojo.repbot.web.pojo.settings.sub.ProfilePOJO;
import de.chojo.repbot.web.pojo.settings.sub.ReputationPOJO;
import de.chojo.repbot.web.pojo.settings.sub.ThankingPOJO;
import net.dv8tion.jda.api.entities.Guild;

public class SettingsPOJO {
    AbuseProtectionPOJO abuseProtection;
    AnnouncementsPOJO announcements;
    AutopostPOJO autopost;
    GeneralPOJO general;
    LogChannelPOJO logChannel;
    MessagesPOJO messages;
    ProfilePOJO profile;
    ReputationPOJO reputation;
    ThankingPOJO thanking;

    public SettingsPOJO(AbuseProtectionPOJO abuseProtection, AnnouncementsPOJO announcements, AutopostPOJO autopost, GeneralPOJO general, LogChannelPOJO logChannel, MessagesPOJO messages, ProfilePOJO profile, ReputationPOJO reputation, ThankingPOJO thanking) {
        this.abuseProtection = abuseProtection;
        this.announcements = announcements;
        this.autopost = autopost;
        this.general = general;
        this.logChannel = logChannel;
        this.messages = messages;
        this.profile = profile;
        this.reputation = reputation;
        this.thanking = thanking;
    }

    public static SettingsPOJO generate(Guild guild, GuildRepository guildRepository) {
        RepGuild repGuild = guildRepository.guild(guild);
        Settings settings = repGuild.settings();
        Thanking thanking = settings.thanking();
        ThankingPOJO thankingPOJO = new ThankingPOJO(thanking.channels(), thanking.donorRoles(), thanking.receiverRoles(), thanking.reactions(), thanking.thankwords());
        return new SettingsPOJO(settings.abuseProtection(), settings.announcements(),
                settings.autopost(), settings.general(), settings.logChannel(), settings.messages(), settings.profile(), settings.reputation(), thankingPOJO);
    }
}
