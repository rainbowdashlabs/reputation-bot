/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.repsettings.handler.name;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.util.Premium;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class Set implements SlashHandler {
    private static final int MAX_NAME_LENGTH = 16;
    private final GuildRepository guildRepository;

    public Set(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext eventContext) {
        Premium.isNotEntitled()
        String name = event.getOption("name", OptionMapping::getAsString);
        guildRepository.guild(event.getGuild()).localeOverrides().setOverride("words.reputation", name);
        event.reply(eventContext.localize("command.repsettings.name.set.message.set")).setEphemeral(true).queue();

    }
}
