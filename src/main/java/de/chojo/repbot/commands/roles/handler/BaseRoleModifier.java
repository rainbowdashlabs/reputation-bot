/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.roles.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.menus.MenuAction;
import de.chojo.jdautil.menus.entries.ButtonEntry;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.function.Consumer;

/**
 * Abstract base class for handling role modification commands.
 */
public abstract class BaseRoleModifier implements SlashHandler {

    private final Refresh refresh;
    private final Guilds guilds;

    /**
     * Constructs a BaseRoleModifier with the specified refresh handler and guilds provider.
     *
     * @param refresh the refresh handler
     * @param guilds the guilds provider
     */
    public BaseRoleModifier(Refresh refresh, Guilds guilds) {
        this.refresh = refresh;
        this.guilds = guilds;
    }

    /**
     * Handles the slash command interaction event.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        Consumer<MessageEmbed> refreshConsumer = menu -> {
            context.registerMenu(MenuAction.forCallback(menu, event)
                    .addComponent(ButtonEntry.of(Button.of(ButtonStyle.DANGER, "refresh", "Refresh roles"),
                            ctx -> refresh.refresh(context, ctx.event().getGuild(), ctx.event()))).build());
        };
        modify(event, context, refreshConsumer);
    }

    /**
     * Abstract method to modify roles, to be implemented by subclasses.
     *
     * @param event the slash command interaction event
     * @param context the event context
     * @param refresh the consumer to handle refresh actions
     */
    public abstract void modify(SlashCommandInteractionEvent event, EventContext context, Consumer<MessageEmbed> refresh);

    /**
     * Gets the guilds provider.
     *
     * @return the guilds provider
     */
    public Guilds guilds() {
        return guilds;
    }
}
