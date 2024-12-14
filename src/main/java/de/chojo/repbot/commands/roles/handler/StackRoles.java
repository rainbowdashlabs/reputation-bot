/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.roles.handler;

import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.util.Text;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.function.Consumer;

/**
 * Handler for the "stack roles" slash command.
 * This command toggles the stacking of roles for the guild.
 */
public class StackRoles extends BaseRoleModifier {

    /**
     * Constructs a new StackRoles handler.
     *
     * @param refresh the refresh consumer
     * @param guilds the guilds provider
     */
    public StackRoles(Refresh refresh, Guilds guilds) {
        super(refresh, guilds);
    }

    /**
     * Modifies the stack roles setting for the guild.
     *
     * @param event the slash command interaction event
     * @param context the event context
     * @param refresh the refresh consumer
     */
    @Override
    public void modify(SlashCommandInteractionEvent event, EventContext context, Consumer<MessageEmbed> refresh) {
        var settings = guilds().guild(event.getGuild()).settings();
        if (event.getOptions().isEmpty()) {
            event.reply(Text.getBooleanMessage(context, settings.general().isStackRoles(),
                    "command.roles.stackroles.message.stacked", "command.roles.stackroles.message.notStacked")).queue();
            return;
        }
        var state = event.getOption("stack").getAsBoolean();

        if (settings.general().stackRoles(state)) {
            var menu = new LocalizedEmbedBuilder(context.guildLocalizer())
                    .setTitle(Text.getBooleanMessage(context, state,
                            "command.roles.stackroles.message.stacked", "command.roles.stackroles.message.notStacked"))
                    .build();
            refresh.accept(menu);
        }
    }
}
