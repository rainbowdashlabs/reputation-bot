package de.chojo.repbot.util;

import de.chojo.repbot.data.GuildData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public final class Permissions {

    private static final Logger log = getLogger(Permissions.class);

    private Permissions() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static void buildGuildPriviledges(GuildData guildData, ShardManager shardManager) {
        for (JDA shard : shardManager.getShards()) {
            for (Guild guild : shard.getGuilds()) {
                buildGuildPriviledges(guildData, guild);
            }
        }
    }

    public static void buildGuildPriviledges(GuildData guildData, Guild guild){
        var settings = guildData.getGuildSettings(guild);
        var optRole = settings.generalSettings().managerRole().map(guild::getRoleById);
        List<Role> roles;
        log.debug("Refreshing command priviledges for guild {}", Guilds.prettyName(guild));
        if (optRole.isEmpty()) {
            log.debug("No manager role defined on guild {}. Using admin roles.", guild.getIdLong());
            roles = guild.getRoles().stream().filter(r -> r.hasPermission(Permission.ADMINISTRATOR)).limit(5).collect(Collectors.toList());
        } else {
            log.debug("Using manager role on {}", Guilds.prettyName(guild));
            roles = Collections.singletonList(optRole.get());
        }

        List<CommandPrivilege> privileges = new ArrayList<>();

        for (Role role : roles) {
            privileges.add(CommandPrivilege.enable(role));
        }

        privileges.add(CommandPrivilege.enable(guild.retrieveOwner().complete().getUser()));

        var adminCommands = guild.retrieveCommands().complete().stream().filter(c -> !c.isDefaultEnabled()).collect(Collectors.toList());

        Map<String, Collection<? extends CommandPrivilege>> commandPrivileges = new HashMap<>();
        for (var adminCommand : adminCommands) {
            commandPrivileges.put(adminCommand.getId(), privileges);
        }

        log.debug("Update done. Set restricted commands to {} priviledges", privileges.size());
        guild.updateCommandPrivileges(commandPrivileges).queue();
    }
}
