/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.log.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.ValueParser;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static de.chojo.repbot.commands.log.handler.LogFormatter.buildFields;
import static de.chojo.repbot.commands.log.handler.LogFormatter.mapMessageLogEntry;

public class Message implements SlashHandler {
    private final GuildRepository guildRepository;

    public Message(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        event.getOption("messageid");
        var optMessageId = ValueParser.parseLong(event.getOption("messageid").getAsString());
        if (optMessageId.isEmpty()) {
            event.reply(context.localize("error.invalidMessage"))
                    .setEphemeral(true)
                    .complete();
            return;
        }

        event.replyEmbeds(getMessageLog(
                        context, event.getGuild(), event.getOption("messageid").getAsLong()))
                .setEphemeral(true)
                .complete();
    }

    private MessageEmbed getMessageLog(EventContext context, Guild guild, long messageId) {
        var messageLog = guildRepository.guild(guild).reputation().log().messageLog(messageId, 50);

        var log = mapMessageLogEntry(context.guildLocalizer(), messageLog);

        var builder = new LocalizedEmbedBuilder(context.guildLocalizer())
                .setAuthor("command.log.message.message.log", Replacement.create("ID", messageId));
        buildFields(log, builder);
        return builder.build();
    }
}
