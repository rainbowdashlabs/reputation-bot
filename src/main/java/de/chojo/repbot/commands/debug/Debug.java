package de.chojo.repbot.commands.debug;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.util.Colors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.StringJoiner;

import static de.chojo.repbot.util.Guilds.prettyName;

public class Debug extends SlashCommand {

    public Debug(Guilds guilds) {
        super(Slash.of("debug", "command.debug.description")
                .adminCommand()
                .command(new Handler(guilds)));
    }
}
