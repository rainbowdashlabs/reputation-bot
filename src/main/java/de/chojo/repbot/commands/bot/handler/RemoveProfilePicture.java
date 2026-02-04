/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class RemoveProfilePicture implements SlashHandler {

    private final GuildRepository guildRepository;

    public RemoveProfilePicture(GuildRepository guildRepository) {
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

        var profile = guildRepository.guild(guild).settings().profile();
        boolean success = profile.profilePicture(null);

        if (success) {
            event.reply("Successfully removed profile picture for bot in guild: " + guild.getName()).setEphemeral(true).queue();
        } else {
            event.reply("Failed to remove profile picture. Bot may lack permissions or an error occurred.").setEphemeral(true).queue();
        }
    }
}
