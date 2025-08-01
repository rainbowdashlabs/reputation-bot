/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.prune.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.service.GdprService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Guild implements SlashHandler {
    private static final Logger log = getLogger(Guild.class);
    private final GdprService service;

    public Guild(GdprService service) {
        this.service = service;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        event.reply(context.localize("command.prune.guild.message.started")).complete();
        service.cleanupGuildUsers(event.getGuild())
               .thenAccept(amount -> event.getHook().editOriginal(context.localize("command.prune.guild.message.done",
                       Replacement.create("AMOUNT", amount))).complete());
    }
}
