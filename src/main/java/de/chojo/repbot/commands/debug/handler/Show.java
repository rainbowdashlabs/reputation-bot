/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.debug.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.util.Colors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.StringJoiner;

import static de.chojo.repbot.util.Guilds.prettyName;

/**
 * Handler for the debug show slash command.
 */
public class Show implements SlashHandler {
    private final Guilds guilds;

    /**
     * Constructs a new Show handler.
     *
     * @param guilds the guilds provider
     */
    public Show(Guilds guilds) {
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
        var settings = guilds.guild(event.getGuild()).settings();

        var joiner = new StringJoiner("`, `", "`", "`");
        settings.thanking().thankwords().words().forEach(joiner::add);

        event.replyEmbeds(new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.debug.message.title",
                        Replacement.create("GUILD", prettyName(event.getGuild())))
                .addField("word.reputationSettings", settings.reputation().toLocalizedString(), false)
                .addField("word.thankWords", joiner.setEmptyValue("none").toString(), true)
                .addField("command.debug.message.channelactive", String.valueOf(
                                settings.thanking().channels().isEnabled(event.getChannel().asTextChannel())),
                        true
                )
                .setColor(Colors.Pastel.DARK_PINK)
                .build()).queue();
    }
}
