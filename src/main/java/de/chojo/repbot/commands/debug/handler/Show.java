/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.debug.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.service.debugService.GeneralPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfMember;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.ArrayList;
import java.util.List;

public class Show implements SlashHandler {
    private final GuildRepository guildRepository;
    private static final List<Permission> REPUTATION_CHANNEL_PERMS = List.of();

    public Show(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        Guild guild = event.getGuild();
        SelfMember self = guild.getSelfMember();
        RepGuild repGuild = guildRepository.guild(guild);
        event.deferReply(true).queue();

        List<String> problems = new ArrayList<>();

        for (GeneralPermissions value : GeneralPermissions.values()) {
            if (!value.check(self::hasPermission)) {
                problems.add(value.problemMessage());
            }
        }

        long systemChannelId = repGuild.settings().general().systemChannel();
        TextChannel systemChannel = guild.getTextChannelById(systemChannelId);
        if (systemChannelId == 0) {
            problems.add("System channel is not set");
        } else if (systemChannel == null) {
            problems.add("System channel is invalid");
        } else {

        }

        if (problems.isEmpty()) {
            event.getHook().sendMessage("No problems found.").queue();
        } else {
            event.getHook()
                    .sendMessage("Found the following problems:\n- " + String.join("\n- ", problems))
                    .queue();
        }
    }
}
