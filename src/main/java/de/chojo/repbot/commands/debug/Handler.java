package de.chojo.repbot.commands.debug;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.util.Colors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.StringJoiner;

import static de.chojo.repbot.util.Guilds.prettyName;

public class Handler implements SlashHandler {
    private final Guilds guilds;

    public Handler(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var settings = guilds.guild(event.getGuild()).settings();

        var joiner = new StringJoiner("`, `", "`", "`");
        settings.thanking().thankwords().words().forEach(joiner::add);

        event.replyEmbeds(new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.debug.title",
                        Replacement.create("GUILD", prettyName(event.getGuild())))
                .addField("word.reputationSettings", settings.reputation().toLocalizedString(), false)
                .addField("word.thankWords", joiner.setEmptyValue("none").toString(), true)
                .addField("command.debug.channelActive", String.valueOf(
                                settings.thanking().channels().isEnabled(event.getChannel().asTextChannel())),
                        true
                )
                .setColor(Colors.Pastel.DARK_PINK)
                .build()).queue();
    }
}
