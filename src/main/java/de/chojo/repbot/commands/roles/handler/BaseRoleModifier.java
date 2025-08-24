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
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.function.Consumer;

public abstract class BaseRoleModifier implements SlashHandler {

    private final Refresh refresh;
    private final GuildRepository guildRepository;

    public BaseRoleModifier(Refresh refresh, GuildRepository guildRepository) {
        this.refresh = refresh;
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        Consumer<MessageEmbed> refreshConsumer = menu -> {
            context.registerMenu(MenuAction.forCallback(menu, event)
                    .addComponent(ButtonEntry.of(Button.of(ButtonStyle.DANGER, "refresh", "Refresh roles"),
                            ctx -> refresh.refresh(context, ctx.event().getGuild(), ctx.event()))).build());
        };
        modify(event, context, refreshConsumer);
    }

    public abstract void modify(SlashCommandInteractionEvent event, EventContext context, Consumer<MessageEmbed> refresh);

    public GuildRepository guilds() {
        return guildRepository;
    }
}
