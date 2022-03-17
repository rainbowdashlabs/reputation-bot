package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.util.Colors;
import de.chojo.repbot.util.Guilds;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.sql.DataSource;

public class Debug extends SimpleCommand {
    private final GuildData data;
    private final Localizer localizer;

    public Debug(DataSource dataSource, Localizer localizer) {
        super("debug", null, "command.debug.description", argsBuilder().build(), Permission.ADMINISTRATOR);
        data = new GuildData(dataSource);
        this.localizer = localizer;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var guildSettings = data.getGuildSettings(event.getGuild());

        String thankWords = String.join(", ", guildSettings.thankSettings().thankwords());

        event.replyEmbeds(new LocalizedEmbedBuilder(localizer, event)
                .setTitle(localizer.localize(
                        "command.debug.title",
                        Replacement.create("GUILD", Guilds.prettyName(event.getGuild()))
                ))
                .addField("word.messageSettings", localizer.localize(
                                guildSettings.messageSettings().toLocalizedString(guildSettings), event),
                        false
                )
                .addField("word.thankWords", thankWords, true)
                .addField("command.debug.channelActive", String.valueOf(
                                guildSettings.thankSettings().isReputationChannel(event.getTextChannel())),
                        true
                )
                .setColor(Colors.Pastel.DARK_PINK)
                .build()).queue();
    }
}
