/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.roles.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.util.Futures;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.service.RoleAccessException;
import de.chojo.repbot.service.RoleAssigner;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import static de.chojo.repbot.util.Guilds.prettyName;
import static org.slf4j.LoggerFactory.getLogger;

public class Refresh implements SlashHandler {
    private static final Logger log = getLogger(Refresh.class);
    private final RoleAssigner roleAssigner;
    private final Set<Long> running = new HashSet<>();

    public Refresh(RoleAssigner roleAssigner) {
        this.roleAssigner = roleAssigner;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        refresh(context, event.getGuild(), event);
    }

    public void refresh(EventContext context, Guild guild, IReplyCallback replyCallback) {
        if (!replyCallback.isAcknowledged()) {
            replyCallback.deferReply().complete();
        }
        if (running.contains(guild.getIdLong())) {
            replyCallback.getHook().editOriginal(context.localize("command.roles.refresh.message.running")).complete();
            return;
        }

        running.add(guild.getIdLong());

        var message = replyCallback.getHook().editOriginal(context.localize("command.roles.refresh.message.started")).complete();
        var start = Instant.now();

        roleAssigner
                .updateBatch(guild, context, message)
                .whenComplete(Futures.whenComplete(res -> {
                    var duration = DurationFormatUtils.formatDuration(start.until(Instant.now(), ChronoUnit.MILLIS), "mm:ss");
                    log.info("Update of roles on {} took {}. Checked {} Updated {}", prettyName(guild), duration, res.checked(), res.updated());
                    message.editMessage(context.localize("command.roles.refresh.message.finished",
                                    Replacement.create("CHECKED", res.checked()), Replacement.create("UPDATED", res.updated())))
                            .queue(RestAction.getDefaultSuccess(), ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    running.remove(guild.getIdLong());
                }, err -> {
                    log.warn("Update of role failed on guild {}", prettyName(guild), err);
                    if (err instanceof RoleAccessException roleException) {
                        message.editMessage(context.localize("error.roleAccess",
                                        Replacement.createMention("ROLE", roleException.role())))
                                .queue(RestAction.getDefaultSuccess(), ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    }
                    running.remove(guild.getIdLong());
                }));
    }

    public boolean refreshActive(Guild guild) {
        return running.contains(guild.getIdLong());
    }
}
