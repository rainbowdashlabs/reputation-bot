/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.thankwords.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

/**
 * Handler for the list thank words command.
 */
public class List implements SlashHandler {
    private final Guilds guilds;

    /**
     * Constructs a new List handler with the specified Guilds provider.
     *
     * @param guilds the Guilds provider
     */
    public List(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles the slash command interaction event to list thank words.
     *
     * @param event the SlashCommandInteractionEvent
     * @param context the EventContext
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var pattern = getGuildPattern(event.getGuild());
        if (pattern == null) return;

        event.reply(context.localize("command.thankwords.list.message.list") + "\n" + pattern).queue();
    }

    /**
     * Retrieves the thank words pattern for the specified guild.
     *
     * @param guild the Guild to get the thank words pattern from
     * @return a string containing the thank words pattern, or null if not available
     */
    @Nullable
    private String getGuildPattern(Guild guild) {
        return guilds.guild(guild).settings().thanking().thankwords().words().stream()
                     .map(w -> StringUtils.wrap(w, "`"))
                     .collect(Collectors.joining(", "));
    }
}
