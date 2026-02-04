/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.reactions.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.util.Premium;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.util.WebPromo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Info implements SlashHandler {
    private final GuildRepository guildRepository;
    private final Configuration configuration;

    public Info(GuildRepository guildRepository, Configuration configuration) {
        this.guildRepository = guildRepository;
        this.configuration = configuration;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        event.replyEmbeds(WebPromo.promoEmbed(context), getInfoEmbed(event, guildRepository.guild(event.getGuild()).settings(), context))
             .setEphemeral(true)
             .complete();
    }

    private MessageEmbed getInfoEmbed(SlashCommandInteractionEvent event, Settings settings, EventContext context) {
        var reactions = settings.thanking().reactions();
        var mainEmote = reactions.reactionMention();
        var emotes = String.join(" ", reactions.getAdditionalReactionMentions());

        EmbedBuilder build = new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.reactions.info.message.title")
                .addField("command.reactions.info.message.main", mainEmote.orElse("words.unknown"), true)
                .addField("command.reactions.info.message.additional", emotes, true);
        if (Premium.isNotEntitled(context, configuration.skus().features().additionalEmojis().additionalEmojis())) {
            build.setFooter("command.reactions.info.message.nopremium");
        }
        return build.build();
    }

}
