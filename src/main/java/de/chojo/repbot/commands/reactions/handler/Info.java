/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.reactions.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Info implements SlashHandler {
    private final Guilds guilds;

    public Info(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        event.replyEmbeds(getInfoEmbed(guilds.guild(event.getGuild()).settings(), context)).queue();
    }

    private MessageEmbed getInfoEmbed(Settings settings, EventContext context) {
        var reactions = settings.thanking().reactions();
        var mainEmote = reactions.reactionMention();
        var emotes = String.join(" ", reactions.getAdditionalReactionMentions());

        return new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.reactions.info.message.title")
                .addField("command.reactions.info.message.main", mainEmote.orElse("words.unknown"), true)
                .addField("command.reactions.info.message.additional", emotes, true)
                .build();
    }

}
