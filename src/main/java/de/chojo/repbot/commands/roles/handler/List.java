/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.roles.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

public class List implements SlashHandler {
    private final GuildRepository guildRepository;

    public List(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        event.replyEmbeds(getRoleList(context, event.getGuild()))
             .setAllowedMentions(Collections.emptyList())
             .setEphemeral(true)
             .complete();
    }

    private MessageEmbed getRoleList(EventContext context, Guild guild) {
        var settings = guildRepository.guild(guild).settings();
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
