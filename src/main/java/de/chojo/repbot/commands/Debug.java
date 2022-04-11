package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.util.Colors;
import de.chojo.repbot.util.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.StringJoiner;

public class Debug extends SimpleCommand {
    private final GuildData data;

    public Debug(DataSource dataSource) {
        super(CommandMeta.builder("debug", "command.debug.description").withPermission());
        data = new GuildData(dataSource);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var guildSettings = data.getGuildSettings(event.getGuild());

        var joiner = new StringJoiner("`, `", "`", "`");
        Arrays.stream(guildSettings.thankSettings().thankwords()).forEach(joiner::add);

        event.replyEmbeds(new LocalizedEmbedBuilder(context.localizer())
                .setTitle("command.debug.title",
                        Replacement.create("GUILD", Guilds.prettyName(event.getGuild())))
                .addField("word.messageSettings", guildSettings.messageSettings().toLocalizedString(guildSettings), false)
                .addField("word.thankWords", joiner.setEmptyValue("none").toString(), true)
                .addField("command.debug.channelActive", String.valueOf(
                                guildSettings.thankSettings().isReputationChannel(event.getTextChannel())),
                        true
                )
                .setColor(Colors.Pastel.DARK_PINK)
                .build()).queue();
    }
}
