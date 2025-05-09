/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.profile.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.service.RoleAssigner;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Profile implements SlashHandler {
    private final GuildRepository guildRepository;
    private final Configuration configuration;
    private final RoleAssigner roleAssigner;

    public Profile(GuildRepository guildRepository, Configuration configuration, RoleAssigner roleAssigner) {
        this.guildRepository = guildRepository;
        this.configuration = configuration;
        this.roleAssigner = roleAssigner;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var userOption = event.getOption("user");
        var member = userOption != null ? userOption.getAsMember() : event.getMember();
        if (member == null) {
            event.reply(context.localize("error.userNotFound")).queue();
            return;
        }
        var reputation = guildRepository.guild(event.getGuild())
                                        .reputation()
                                        .user(member)
                                        .profile()
                                        .publicProfile(configuration, context.guildLocalizer());
        event.replyEmbeds(reputation).queue();
        roleAssigner.updateReporting(member, event.getGuildChannel());
    }
}
