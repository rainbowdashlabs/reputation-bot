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

/**
 * Provides a message log functionality for the bot.
 */
public class MessageLog implements MessageProvider<Message> {
    private final Guilds guilds;

    /**
     * Constructs a MessageLog with the specified guilds provider.
     *
     * @param guilds the guilds provider
     */
    public MessageLog(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Returns a configured message for logging.
     *
     * @return the configured message
     */
    @Override
    public Message message() {
        return Message.of("Message Log")
                      .handler(new MessageAnalyzer(guilds))
                      .setGuildOnly(true)
                      .withPermission(Permission.MESSAGE_MANAGE)
                      .build();
    }
}
