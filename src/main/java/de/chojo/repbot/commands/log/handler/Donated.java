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
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static de.chojo.repbot.commands.log.handler.LogFormatter.PAGE_SIZE;
import static de.chojo.repbot.commands.log.handler.LogFormatter.mapUserLogEntry;
import static de.chojo.repbot.commands.log.handler.LogFormatter.userLogEmbed;

public class Donated implements SlashHandler {
    private final Guilds guilds;

    public Donated(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var user = event.getOption("user").getAsMember();
        send(event, user, guilds, context);
    }

    public static void send(IReplyCallback event, Member user, Guilds guilds, EventContext context) {
        var logAccess = guilds.guild(event.getGuild()).reputation().log().userDonatedLog(user.getUser(), PAGE_SIZE);
        context.registerPage(new PrivatePageBag(logAccess.pages(), event.getUser().getIdLong()) {
            @Override
            public CompletableFuture<MessageEditData> buildPage() {
                return CompletableFuture.supplyAsync(() -> userLogEmbed(context, user, "command.log.donated.message.log",
                        mapUserLogEntry(context, logAccess.page(current()), ReputationLogEntry::receiverId)));
            }

            @Override
            public CompletableFuture<MessageEditData> buildEmptyPage() {
                return CompletableFuture.completedFuture(userLogEmbed(context, user, "command.log.donated.message.log",
                        mapUserLogEntry(context, Collections.emptyList(), ReputationLogEntry::receiverId)));
            }
        }, true);
    }
}
