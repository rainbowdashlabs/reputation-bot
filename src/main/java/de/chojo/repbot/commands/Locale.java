package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Language;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.text.TextFormatting;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.repbot.data.GuildData;
import net.dv8tion.jda.api.Permission;

import javax.sql.DataSource;

public class Locale extends SimpleCommand {
    private final GuildData data;
    private final Localizer loc;

    public Locale(DataSource dataSource, Localizer loc) {
        super("locale",
                null,
                "command.locale.description",
                "", subCommandBuilder()
                        .add("set", "<locale>", "command.locale.sub.set")
                        .add("list", null, "command.locale.sub.list")
                        .build(),
                Permission.ADMINISTRATOR);
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
            return list(eventWrapper, context.subContext(subCmd));
        }

        return false;
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

    private boolean list(MessageEventWrapper eventWrapper, CommandContext context) {
        var languages = loc.getLanguages();
        var builder = TextFormatting.getTableBuilder(languages,
                eventWrapper.localize("words.language"), eventWrapper.localize("words.code"));
        languages.forEach(l -> builder.setNextRow(l.getLanguage(), l.getCode()));
        eventWrapper.reply(eventWrapper.localize("command.locale.sub.list.list") + "\n" + builder).queue();
        return true;
    }
}
