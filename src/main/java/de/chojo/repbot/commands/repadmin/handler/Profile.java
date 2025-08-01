/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.repadmin.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Profile implements SlashHandler {
    private final GuildRepository guildRepository;
    private final Configuration configuration;

    public Profile(GuildRepository guildRepository, Configuration configuration) {
        this.guildRepository = guildRepository;
        this.configuration = configuration;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        event.deferReply(true).complete();
        var user = guildRepository.guild(event.getGuild()).reputation().user(event.getOption("user").getAsMember());
        var profile = user.profile().adminProfile(configuration, context.guildLocalizer());
        event.getHook().editOriginalEmbeds(profile).complete();
    }
}
