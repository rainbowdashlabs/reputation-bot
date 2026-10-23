/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.settings;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Arrays;
import java.util.List;

/**
 * Handler of a former settings command. Settings are changed in the dashboard, so the command changes nothing and
 * answers with links to the dashboard sections that hold its settings.
 */
public class DashboardLink implements SlashHandler {
    private final Configuration configuration;
    private final List<DashboardSection> sections;

    /**
     * Creates a handler that links to the given sections.
     *
     * @param configuration the bot configuration, for the dashboard address
     * @param sections      the sections to link, at most five
     */
    public DashboardLink(Configuration configuration, DashboardSection... sections) {
        if (sections.length == 0 || sections.length > 5) {
            throw new IllegalArgumentException("A dashboard link needs one to five sections.");
        }
        this.configuration = configuration;
        this.sections = Arrays.asList(sections);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        long guildId = event.getGuild().getIdLong();
        var buttons = sections.stream()
                .map(section -> Button.link(
                        configuration.api().pathUrl(guildId, section.path()), context.localize(section.localeKey())))
                .toList();
        event.reply(context.localize("command.settings.moved"))
                .addComponents(ActionRow.of(buttons))
                .setEphemeral(true)
                .complete();
    }
}
