package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.util.Colors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.StringJoiner;

import static de.chojo.repbot.util.Guilds.prettyName;

public class Debug extends SimpleCommand {
    private final Guilds guilds;

    public Debug(Guilds guilds) {
        super(CommandMeta.builder("debug", "command.debug.description").adminCommand());
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var settings = guilds.guild(event.getGuild()).settings();

        var joiner = new StringJoiner("`, `", "`", "`");
        settings.thanking().thankwords().words().forEach(joiner::add);

        event.replyEmbeds(new LocalizedEmbedBuilder(context.localizer())
                .setTitle("command.debug.title",
                        Replacement.create("GUILD", prettyName(event.getGuild())))
                .addField("word.reputationSettings", settings.reputation().toLocalizedString(), false)
                .addField("word.thankWords", joiner.setEmptyValue("none").toString(), true)
                .addField("command.debug.channelActive", String.valueOf(
                                settings.thanking().channels().isEnabled(event.getTextChannel())),
                        true
                )
                .setColor(Colors.Pastel.DARK_PINK)
                .build()).queue();
    }
}
