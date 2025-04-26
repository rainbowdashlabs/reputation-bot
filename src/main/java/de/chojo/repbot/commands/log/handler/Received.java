/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.log.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.pagination.bag.PrivatePageBag;
import de.chojo.jdautil.util.Premium;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.dao.snapshots.ReputationLogEntry;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static de.chojo.repbot.commands.log.handler.LogFormatter.PAGE_SIZE;
import static de.chojo.repbot.commands.log.handler.LogFormatter.mapUserLogEntry;
import static de.chojo.repbot.commands.log.handler.LogFormatter.userLogEmbed;

public class Received implements SlashHandler {
    private final GuildRepository guildRepository;
    private final Configuration configuration;

    public Received(GuildRepository guildRepository, Configuration configuration) {
        this.guildRepository = guildRepository;
        this.configuration = configuration;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var user = event.getOption("user").getAsMember();
        send(event,user, guildRepository, context, configuration);
    }

    public static void send(IReplyCallback event, Member user, GuildRepository guildRepository, EventContext context, Configuration configuration) {
        var logAccess = guildRepository.guild(event.getGuild()).reputation().log().getUserReceivedLog(user.getUser(), PAGE_SIZE);
        context.registerPage(new PrivatePageBag(logAccess.pages(), event.getUser().getIdLong()) {
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

            @Override
            public int pages() {
                if (Premium.isNotEntitled(event, configuration.skus().features().reputationLog().extendedPages())) {
                    return Math.min(configuration.skus().features().reputationLog().defaultSize(), PAGE_SIZE);
                }
                return super.pages();
            }

        }, true);
        //TODO: Restrict
    }
}
