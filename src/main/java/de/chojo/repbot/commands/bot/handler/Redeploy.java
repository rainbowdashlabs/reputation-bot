/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.util.Guilds;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Redeploy implements SlashHandler {
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {

        var guild = event.getJDA().getShardManager().getGuildById(event.getOption("guild_id").getAsLong());

        if (guild == null) {
            event.reply("Guild not present.").setEphemeral(true).queue();
            return;
        }

        event.reply("Refreshing commands of guild " + Guilds.prettyName(guild)).setEphemeral(true).queue();
        context.interactionHub().refreshGuildCommands(guild);
    }
}
