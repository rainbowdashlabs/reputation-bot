/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.thankwords.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

public class List implements SlashHandler {
    private final GuildRepository guildRepository;

    public List(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var pattern = getGuildPattern(event.getGuild());
        if (pattern == null) return;

        event.reply(context.localize("command.thankwords.list.message.list") + "\n" + pattern)
             .setEphemeral(true)
             .complete();
    }

    @Nullable
    private String getGuildPattern(Guild guild) {
        return guildRepository.guild(guild).settings().thanking().thankwords().words().stream()
                              .map(w -> StringUtils.wrap(w, "`"))
                              .collect(Collectors.joining(", "));
    }
}
