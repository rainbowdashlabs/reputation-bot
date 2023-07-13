/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.repadmin.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Profile implements SlashHandler {
    private final Guilds guilds;
    private final Configuration configuration;

    public Profile(Guilds guilds, Configuration configuration) {
        this.guilds = guilds;
        this.configuration = configuration;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var user = guilds.guild(event.getGuild()).reputation().user(event.getOption("user").getAsMember());
        var profile = user.profile().adminProfile(configuration, context.guildLocalizer());
        event.replyEmbeds(profile).setEphemeral(true).queue();
    }
}
