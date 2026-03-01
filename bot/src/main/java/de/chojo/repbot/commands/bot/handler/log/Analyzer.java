/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot.handler.log;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.parsing.ValueParser;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.commands.log.handler.BaseAnalyzer;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Analyzer extends BaseAnalyzer implements SlashHandler {
    private final GuildRepository guildRepository;

    public Analyzer(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guild_id = ValueParser.parseLong(event.getOption("guild_id").getAsString());
        onSlashCommand(event, context, guildRepository.byId(guild_id.get()).reputation());
    }
}
