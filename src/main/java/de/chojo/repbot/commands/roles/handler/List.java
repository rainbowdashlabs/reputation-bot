/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.roles.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Handler for the list roles command.
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
     * Handles the slash command interaction event to list roles.
     *
     * @param event the SlashCommandInteractionEvent
     * @param context the EventContext
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        event.replyEmbeds(getRoleList(context, event.getGuild())).setAllowedMentions(Collections.emptyList()).queue();
    }

    /**
     * Generates a MessageEmbed containing the list of roles for the specified guild.
     *
     * @param context the EventContext for localization
     * @param guild the Guild to get the role list from
     * @return a MessageEmbed containing the role list
     */
    private MessageEmbed getRoleList(EventContext context, Guild guild) {
        var settings = guilds.guild(guild).settings();
        var ranks = settings.ranks();

        var reputationRoles = ranks.ranks()
                                   .stream()
                                   .sorted(Comparator.reverseOrder())
                                   .filter(role -> role.getRole(guild).isPresent())
                                   .map(role -> role.reputation() + " ➜ " + role.getRole(guild).get().getAsMention())
                                   .collect(Collectors.joining("\n"));

        var builder = new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.roles.info.message.roleinfo");

        builder.addField("command.roles.info.message.reputationrole", reputationRoles, true);

        var thankSettings = settings.thanking();

        if (!thankSettings.donorRoles().roles().isEmpty()) {
            var donorRoles = thankSettings.donorRoles()
                                          .roles()
                                          .stream()
                                          .map(IMentionable::getAsMention)
                                          .collect(Collectors.joining("\n"));

            builder.addField("command.roles.info.message.donorroles", donorRoles, true);
        }
        if (!thankSettings.receiverRoles().roles().isEmpty()) {
            var receiverRoles = thankSettings.receiverRoles()
                                             .roles()
                                             .stream()
                                             .map(IMentionable::getAsMention)
                                             .collect(Collectors.joining("\n"));

            builder.addField("command.roles.info.message.receiverroles", receiverRoles, true);
        }
        return builder.build();
    }
}
