/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.log.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.pagination.bag.PrivatePageBag;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.dao.snapshots.ReputationLogEntry;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static de.chojo.repbot.commands.log.handler.LogFormatter.PAGE_SIZE;
import static de.chojo.repbot.commands.log.handler.LogFormatter.mapUserLogEntry;
import static de.chojo.repbot.commands.log.handler.LogFormatter.userLogEmbed;

/**
 * Handles the received reputation log command.
 */
public class Received implements SlashHandler {
    private final Guilds guilds;

    /**
     * Constructs a Received handler with the specified guilds provider.
     *
     * @param guilds the guilds provider
     */
    public Received(Guilds guilds) {
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
        var user = event.getOption("user").getAsMember();
        send(event, user, guilds, context);
    }

    /**
     * Sends the received reputation log to the user.
     *
     * @param callback the reply callback
     * @param user the member whose log is being retrieved
     * @param guilds the guilds provider
     * @param context the event context
     */
    public static void send(IReplyCallback callback, Member user, Guilds guilds, EventContext context) {
        var logAccess = guilds.guild(callback.getGuild()).reputation().log().getUserReceivedLog(user.getUser(), PAGE_SIZE);
        context.registerPage(new PrivatePageBag(logAccess.pages(), callback.getUser().getIdLong()) {
            @Override
            public CompletableFuture<MessageEditData> buildPage() {
                return CompletableFuture.supplyAsync(() -> userLogEmbed(context, user, "command.log.received.message.log",
                        mapUserLogEntry(context, logAccess.page(current()), ReputationLogEntry::donorId)));
            }

            @Override
            public CompletableFuture<MessageEditData> buildEmptyPage() {
                return CompletableFuture.completedFuture(userLogEmbed(context, user, "command.log.received.message.log",
                        mapUserLogEntry(context, Collections.emptyList(), ReputationLogEntry::donorId)));
            }
        }, true);
    }
}
