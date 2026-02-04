/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.modals.handler.ModalHandler;
import de.chojo.jdautil.modals.handler.TextInputHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

public class SendGuildMessage implements SlashHandler {
    private final GuildRepository guildRepository;

    public SendGuildMessage(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guildId = event.getOption("guild_id").getAsLong();
        var guild = event.getJDA().getShardManager().getGuildById(guildId);

        if (guild == null) {
            event.reply("Guild not found.").setEphemeral(true).queue();
            return;
        }

        RepGuild repGuild = guildRepository.guild(guild);
        long systemChannel = repGuild.settings().general().systemChannel();
        TextChannel textChannelById = guild.getTextChannelById(systemChannel);
        if (textChannelById == null) {
            event.reply("Custom channel is invalid").setEphemeral(true).complete();
            return;
        }

        ModalHandler modal = ModalHandler.builder("Message")
                                         .addInput(TextInputHandler.builder("message", "Message to send", TextInputStyle.PARAGRAPH))
                                         .withHandler((ctx) -> {
                                             ModalMapping message = ctx.getValue("message");
                                             textChannelById.sendMessage(message.getAsString()).complete();
                                         })
                                         .build();
        context.modalService().registerModal(event, modal);
    }
}
