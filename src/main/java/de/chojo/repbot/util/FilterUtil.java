package de.chojo.repbot.data.wrapper;

import de.chojo.repbot.data.GuildData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RepBotWrapper {

    private final Member repBot;
    private final Guild guild;
    private final GuildData guildData;

    private RepBotWrapper(Member repBot, GuildData guildData) {
        this.repBot = repBot;
        this.guild = repBot.getGuild();
        this.guildData = guildData;
    }


    public void addAllChannelsICanReadAndWrite() {
        addAllChannelsIHaveFollowingPermissions(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE);
    }

    public void addAllChannelsIHaveFollowingPermissions(Permission... permissions) {
        for (TextChannel textChannel : guild.getTextChannels()) {
            boolean hasAllRequiredPermissions = true;
            for (Permission permission : permissions) {
                if (!repBot.hasPermission(permission)) {
                    hasAllRequiredPermissions = false;
                    break;
                }
            }
            if (hasAllRequiredPermissions) {
                guildData.addChannel(guild, textChannel);
            }
        }
    }

    public static List<TextChannel> filterChannelByPermission(Guild guild, Permission... permissions) {
        var self = guild.getSelfMember();
        return guild.getTextChannels().stream().filter(textChannel -> self.hasPermission(textChannel, permissions)).collect(Collectors.toList());
    }

    public static RepBotWrapper of(Member repBot, GuildData guildData) {
        return new RepBotWrapper(repBot, guildData);
    }

    public static RepBotWrapper of(Guild guild, GuildData guildData) {
        return new RepBotWrapper(guild.getSelfMember(), guildData);
    }
}
