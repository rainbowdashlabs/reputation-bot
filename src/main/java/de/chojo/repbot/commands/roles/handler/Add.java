/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.roles.handler;

import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.function.Consumer;

/**
 * Handler for the "add role" slash command, which adds a role with a specified reputation.
 */
public class Add extends BaseRoleModifier {

    /**
     * Constructs an Add handler with the specified refresh function and guilds provider.
     *
     * @param refresh the refresh function
     * @param guilds the guilds provider
     */
    public Add(Refresh refresh, Guilds guilds) {
        super(refresh, guilds);
    }

    /**
     * Modifies the roles by adding a new role with the specified reputation.
     *
     * @param event the slash command interaction event
     * @param context the event context
     * @param refresh the consumer to refresh the message embed
     */
    @Override
    public void modify(SlashCommandInteractionEvent event, EventContext context, Consumer<MessageEmbed> refresh) {
        var role = event.getOption("role").getAsRole();
        var reputation = event.getOption("reputation").getAsLong();
        if (!event.getGuild().getSelfMember().canInteract(role)) {
            event.reply(context.localize("error.roleAccess",
                    Replacement.createMention(role))).setEphemeral(true).queue();
            return;
        }

        var ranks = guilds().guild(event.getGuild()).settings().ranks();
        ranks.add(role, reputation);
        var menu = new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.roles.add.title.added")
                .setDescription("command.roles.add.message.added", Replacement.createMention("ROLE", role), Replacement.create("POINTS", reputation))
                .build();
        refresh.accept(menu);
    }
}
