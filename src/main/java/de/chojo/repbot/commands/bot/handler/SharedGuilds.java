/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.menus.MenuAction;
import de.chojo.jdautil.menus.MenuActionBuilder;
import de.chojo.jdautil.menus.entries.MenuEntry;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.util.Guilds;
import de.chojo.repbot.util.States;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static net.dv8tion.jda.api.Permission.ADMINISTRATOR;
import static net.dv8tion.jda.api.Permission.BAN_MEMBERS;
import static net.dv8tion.jda.api.Permission.KICK_MEMBERS;
import static net.dv8tion.jda.api.Permission.MANAGE_CHANNEL;
import static net.dv8tion.jda.api.Permission.MANAGE_ROLES;
import static net.dv8tion.jda.api.Permission.MANAGE_SERVER;
import static net.dv8tion.jda.api.Permission.MESSAGE_MANAGE;

public class SharedGuilds implements SlashHandler {
    private final Configuration configuration;
    private static final EnumSet<Permission> MODERATOR = EnumSet.of(MANAGE_SERVER, KICK_MEMBERS, BAN_MEMBERS, MESSAGE_MANAGE, MANAGE_CHANNEL, MANAGE_ROLES);
    private final GuildRepository guildRepository;

    public SharedGuilds(Configuration configuration, GuildRepository guildRepository) {
        this.configuration = configuration;
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var userOpt = event.getOption("user");
        var userIdOpt = event.getOption("user_id");
        var deepSearch = event.getOption("deep", () -> false, OptionMapping::getAsBoolean);
        if (userOpt == null && userIdOpt == null) {
            event.reply("Provide a user").setEphemeral(true).complete();
            return;
        }

        event.deferReply(true).complete();

        User user;

        if (userOpt != null) {
            user = userOpt.getAsUser();
        } else {
            try {
                user = event.getJDA().getShardManager().retrieveUserById(event.getIdLong()).complete();
            } catch (RuntimeException e) {
                event.getHook().editOriginal("Could not find this user.").complete();
                return;
            }
        }

        List<Guild> shared = sharedGuilds(user, deepSearch, configuration);
        MessageEmbed messageEmbed = buildEmbed(user, shared);
        MenuActionBuilder menuActionBuilder = MenuAction.forCallback(messageEmbed, event);
        for (Guild guild : shared) {
            menuActionBuilder.addComponent(MenuEntry.of(Button.primary(guild.getId(), guild.getName()), ctx -> {
                Debug.sendDebug(ctx.event(), context.interactionHub().pageServices(), guildRepository.guild(guild), null);
            }));
        }
        context.registerMenu(menuActionBuilder.build());
    }

    public static MessageEmbed sharedGuildsEmbed(User user, boolean deepSearch, Configuration configuration) {
        List<Guild> shared = sharedGuilds(user, deepSearch, configuration);
        return buildEmbed(user, shared);
    }

    public static List<Guild> sharedGuilds(User user, boolean deepSearch, Configuration configuration) {
        var mutualGuilds = new ArrayList<Guild>();

        for (var shard : user.getJDA().getShardManager().getShards()) {
            mutualGuilds.addAll(shard.getMutualGuilds(user));
        }

        if (mutualGuilds.isEmpty() && deepSearch) {
            for (var shard : user.getJDA().getShardManager().getShards()) {
                for (Guild guild : shard.getGuilds()) {
                    try {
                        Member member = guild.retrieveMemberById(user.getIdLong()).complete();
                        if (member != null) {
                            mutualGuilds.add(guild);
                        }
                    } catch (ErrorResponseException e) {
                        //pass
                    }
                }
            }
        }

        if (!States.TEST_MODE) {
            mutualGuilds.removeIf(g -> configuration.baseSettings().botGuild() == g.getIdLong());
        }
        return mutualGuilds;
    }

    public static MessageEmbed buildEmbed(User user, List<Guild> mutualGuilds) {
        var guilds = mutualGuilds.stream()
                                 .map(guild -> format(guild, user))
                                 .collect(Collectors.joining("\n"));

        return new EmbedBuilder().setTitle("Shared Guilds").setDescription(guilds).build();
    }

    public static String format(Guild guild, User user) {
        return "%s Status: %s Members: %d".formatted(Guilds.prettyName(guild), userStanding(user, guild), guild.getMemberCount());
    }

    private static String userStanding(User user, Guild guild) {
        if (guild.getOwnerIdLong() == user.getIdLong()) return "Owner";
        Member member = guild.retrieveMemberById(user.getIdLong()).complete();
        var permissions = member.getPermissions();
        if (member.hasPermission(ADMINISTRATOR)) return "Admin";
        if (MODERATOR.stream().anyMatch(permissions::contains)) return "Moderator";
        return "Member";
    }
}
