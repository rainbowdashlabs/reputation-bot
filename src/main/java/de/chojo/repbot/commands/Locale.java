package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.command.dispatching.CommandHub;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.text.TextFormatting;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.GuildData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.sql.DataSource;

public class Locale extends SimpleCommand {
    private final GuildData data;
    private final Localizer loc;
    private CommandHub<SimpleCommand> commandHub;

    public Locale(DataSource dataSource, Localizer loc) {
        super("locale",
                null,
                "command.locale.description",
                subCommandBuilder()
                        .add("set", "command.locale.sub.set", argsBuilder()
                                .add(OptionType.STRING, "language", "language", true)
                                .build()
                        )
                        .add("list", "command.locale.sub.list")
                        .build(),
                Permission.MANAGE_SERVER);
        data = new GuildData(dataSource);
        this.loc = loc;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {
        var subCmd = event.getSubcommandName();
        if ("set".equalsIgnoreCase(subCmd)) {
            set(event);
        }
        if ("list".equalsIgnoreCase(subCmd)) {
            list(event);
        }
    }

    private void set(SlashCommandEvent event) {
        var loc = this.loc.getContextLocalizer(event.getGuild());
        var language = this.loc.getLanguage(event.getOption("language").getAsString());
        if (language.isEmpty()) {
            event.reply(loc.localize("command.locale.error.invalidLocale")).setEphemeral(true).queue();
            return;
        }
        if (data.setLanguage(event.getGuild(), language.get())) {
            event.reply(loc.localize("command.locale.sub.set.set",
                    Replacement.create("LOCALE", language.get().getLanguage(), Format.CODE))).queue();
            commandHub.refreshGuildCommands(event.getGuild());
        }
    }

    private void list(SlashCommandEvent event) {
        var loc = this.loc.getContextLocalizer(event.getGuild());
        var languages = this.loc.getLanguages();
        var builder = TextFormatting.getTableBuilder(languages,
                loc.localize("words.language"), loc.localize("words.code"));
        languages.forEach(l -> builder.setNextRow(l.getLanguage(), l.getCode()));
        event.reply(loc.localize("command.locale.sub.list.list") + "\n" + builder).queue();
    }

    public void addCommandHub(CommandHub<SimpleCommand> commandHub){
        this.commandHub = commandHub;
    }
}
