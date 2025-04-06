/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.actions.messages.log;

import de.chojo.jdautil.interactions.message.Message;
import de.chojo.jdautil.interactions.message.provider.MessageProvider;
import de.chojo.repbot.actions.messages.log.handler.MessageAnalyzer;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.InteractionContextType;

import java.util.Set;

public class MessageLog implements MessageProvider<Message> {
    private final Guilds guilds;

    public MessageLog(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public Message message() {
        return Message.of("Message Log")
                      .handler(new MessageAnalyzer(guilds))
                      .setContext(Set.of(InteractionContextType.GUILD))
                      .withPermission(Permission.MESSAGE_MANAGE)
                      .build();
    }
}
