/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.reputation.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.service.RoleAssigner;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Handles the profile command for reputation.
 */
public class Profile implements SlashHandler {
    private final Guilds guilds;
    private final Configuration configuration;
    private final RoleAssigner roleAssigner;

    /**
     * Constructs a Profile handler.
     *
     * @param guilds the guilds provider
     * @param configuration the configuration
     * @param roleAssigner the role assigner service
     */
    public Profile(Guilds guilds, Configuration configuration, RoleAssigner roleAssigner) {
        this.guilds = guilds;
        this.configuration = configuration;
        this.roleAssigner = roleAssigner;
    }

    /**
     * Handles the slash command interaction event.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var userOption = event.getOption("user");
        var member = userOption != null ? userOption.getAsMember() : event.getMember();
        if (member == null) {
            event.reply(context.localize("error.userNotFound")).queue();
            return;
        }
        var reputation = guilds.guild(event.getGuild())
                               .reputation()
                               .user(member)
                               .profile()
                               .publicProfile(configuration, context.guildLocalizer());
        event.replyEmbeds(reputation).queue();
        roleAssigner.updateReporting(member, event.getGuildChannel());
    }
}
