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

public class Request implements SlashHandler {
    private final Gdpr gdpr;

    public Request(Gdpr gdpr) {
        this.gdpr = gdpr;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        event.deferReply(true).complete();
        var user = gdpr.request(event.getUser());
        var request = user.request();
        if (request) {
            if (user.sendData()) {
                user.requestSend();
                event.getHook().editOriginal(context.localize("command.gdpr.request.message.send")).complete();
            } else {
                user.requestSendFailed();
                event.getHook().editOriginal(context.localize("command.gdpr.request.message.failed")).complete();
            }
        } else {
            event.getHook().editOriginal(context.localize("command.gdpr.request.message.requested")).complete();
        }
    }
}
