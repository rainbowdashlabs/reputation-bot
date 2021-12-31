package de.chojo.repbot.util;

import de.chojo.repbot.data.GuildData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Permissions {

    private Permissions() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static void buildGuildPriviledges(GuildData guildData, ShardManager shardManager) {
        for (JDA shard : shardManager.getShards()) {
            for (Guild guild : shard.getGuilds()) {
                var settings = guildData.getGuildSettings(guild);
                var optRole = settings.generalSettings().managerRole().map(guild::getRoleById);
                List<Role> roles;
                if (optRole.isEmpty()) {
                    roles = guild.getRoles().stream().filter(r -> r.hasPermission(Permission.ADMINISTRATOR)).limit(5).collect(Collectors.toList());
                } else {
                    roles = Collections.singletonList(optRole.get());
                }

                List<CommandPrivilege> privileges = new ArrayList<>();

                for (Role role : roles) {
                    privileges.add(CommandPrivilege.enable(role));
                }

                privileges.add(CommandPrivilege.enable(guild.getOwner().getUser()));

                var adminCommands = guild.retrieveCommands().complete().stream().filter(c -> !c.isDefaultEnabled()).collect(Collectors.toList());

                Map<String, Collection<? extends CommandPrivilege>> commandPrivileges = new HashMap<>();
                for (var adminCommand : adminCommands) {
                    commandPrivileges.put(adminCommand.getId(), privileges);
                }
                guild.updateCommandPrivileges(commandPrivileges).queue();
            }
        }
    }
}
