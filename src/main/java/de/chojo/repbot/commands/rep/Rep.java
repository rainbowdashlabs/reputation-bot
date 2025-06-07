/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.rep;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashProvider;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

public class Rep implements SlashHandler, SlashProvider<Slash> {
    private Command profile;

    public Rep() {
    }


    @Override
    public Slash slash() {
        return Slash.of("rep", "This command is now named profile")
                    .unlocalized()
                    .command(this)
                    .build();
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        if (profile == null) {
            event.getJDA().retrieveCommands().complete().stream()
                 .filter(cmd -> cmd.getName().equals("profile"))
                 .findFirst()
                 .ifPresent(command -> profile = command);
        }
        if (profile != null) {
            event.reply("This command is now named " + profile.getAsMention()).setEphemeral(true).queue();
            return;
        }
        event.reply("This command is now named profile").setEphemeral(true).queue();

    }
}
