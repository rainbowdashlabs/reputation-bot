/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.gdpr.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.Gdpr;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Handler for the GDPR request command.
 */
public class Request implements SlashHandler {
    private final Gdpr gdpr;

    /**
     * Constructs a new Request handler with the specified GDPR data access object.
     *
     * @param gdpr the GDPR data access object
     */
    public Request(Gdpr gdpr) {
        this.gdpr = gdpr;
    }

    /**
     * Handles the slash command interaction event to process a GDPR request.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        event.deferReply(true).queue();
        var user = gdpr.request(event.getUser());
        var request = user.request();
        if (request) {
            if (user.sendData()) {
                user.requestSend();
                event.getHook().editOriginal(context.localize("command.gdpr.request.message.send")).queue();
            } else {
                user.requestSendFailed();
                event.getHook().editOriginal(context.localize("command.gdpr.request.message.failed")).queue();
            }
        } else {
            event.getHook().editOriginal(context.localize("command.gdpr.request.message.requested")).queue();
        }
    }
}
