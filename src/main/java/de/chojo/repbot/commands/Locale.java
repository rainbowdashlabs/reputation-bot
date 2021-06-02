package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.text.TextFormatting;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.repbot.data.GuildData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.sql.DataSource;

public class Locale extends SimpleCommand {
    private final GuildData data;
    private final Localizer loc;

    public Locale(DataSource dataSource, Localizer loc) {
        super("locale",
                null,
                "command.locale.description",
                subCommandBuilder()
                        .add("set", "command.locale.sub.set", argsBuilder()
                                .add(OptionType.STRING, "locale", "locale")
                                .build()
                        )
                        .add("list", "command.locale.sub.list")
                        .build(),
                Permission.MANAGE_SERVER);
        this.data = new GuildData(dataSource);
        this.loc = loc;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) {
            var guildLocale = loc.getGuildLocale(eventWrapper.getGuild());
            eventWrapper.reply(eventWrapper.localize("command.locale.current",
                    Replacement.create("LOCALE", guildLocale.getLanguage()))).queue();
            return true;
        }

        var subCmd = context.argString(0).get();
        if ("set".equalsIgnoreCase(subCmd)) {
            return set(eventWrapper, context.subContext(subCmd));
        }
        if ("list".equalsIgnoreCase(subCmd)) {
            return list(eventWrapper);
        }

        return false;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        var subCmd = event.getSubcommandName();
        if ("set".equalsIgnoreCase(subCmd)) {
            set(event);
        }
        if ("list".equalsIgnoreCase(subCmd)) {
            list(event);
        }
    }

    private boolean set(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) return false;
        var language = loc.getLanguage(context.argString(0).get());
        if (language.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("command.locale.error.invalidLocale"), 10);
            return true;
        }
        data.setLanguage(eventWrapper.getGuild(), language.get());
        eventWrapper.reply(eventWrapper.localize("command.locale.sub.set.set",
                Replacement.create("LOCALE", language.get().getLanguage(), Format.CODE))).queue();
        return true;
    }

    private boolean list(MessageEventWrapper eventWrapper) {
        var languages = loc.getLanguages();
        var builder = TextFormatting.getTableBuilder(languages,
                eventWrapper.localize("words.language"), eventWrapper.localize("words.code"));
        languages.forEach(l -> builder.setNextRow(l.getLanguage(), l.getCode()));
        eventWrapper.reply(eventWrapper.localize("command.locale.sub.list.list") + "\n" + builder).queue();
        return true;

    }

    private boolean set(SlashCommandEvent event) {
        var loc = this.loc.getContextLocalizer(event.getGuild());
        var language = this.loc.getLanguage(event.getOption("language").getAsString());
        if (language.isEmpty()) {
            event.reply(loc.localize("command.locale.error.invalidLocale")).setEphemeral(true).queue();
            return true;
        }
        data.setLanguage(event.getGuild(), language.get());
        event.reply(loc.localize("command.locale.sub.set.set",
                Replacement.create("LOCALE", language.get().getLanguage(), Format.CODE))).queue();
        return true;
    }

    private boolean list(SlashCommandEvent event) {
        var loc = this.loc.getContextLocalizer(event.getGuild());
        var languages = this.loc.getLanguages();
        var builder = TextFormatting.getTableBuilder(languages,
                loc.localize("words.language"), loc.localize("words.code"));
        languages.forEach(l -> builder.setNextRow(l.getLanguage(), l.getCode()));
        event.reply(loc.localize("command.locale.sub.list.list") + "\n" + builder).queue();
        return true;
    }
}
