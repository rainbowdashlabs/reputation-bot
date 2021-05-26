package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.data.GuildData;
import net.dv8tion.jda.api.Permission;

import javax.sql.DataSource;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Prefix extends SimpleCommand {
    private final GuildData data;
    private final Configuration configuration;

    public Prefix(DataSource dataSource, Configuration configuration) {
        super("prefix",
                null,
                "command.prefix.description",
                "",
                subCommandBuilder().add("set", "<prefix>", "command.prefix.sub.set")
                        .add("reset", null, "command.prefix.sub.reset")
                        .build(),
                Permission.MANAGE_SERVER);
        data = new GuildData(dataSource);
        this.configuration = configuration;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        var optSubCmd = context.argString(0);
        if (optSubCmd.isPresent()) {
            var subCmd = optSubCmd.get();
            if ("set".equalsIgnoreCase(subCmd)) {
                return set(eventWrapper, context.subContext(subCmd));
            }

            if ("reset".equalsIgnoreCase(subCmd)) {
                return reset(eventWrapper);
            }
            return false;
        } else {
            return get(eventWrapper);
        }
    }

    private boolean reset(MessageEventWrapper eventWrapper) {
        changePrefix(eventWrapper, configuration.getDefaultPrefix());
        return true;
    }

    private void changePrefix(MessageEventWrapper eventWrapper, String prefix) {
        if (data.setPrefix(eventWrapper.getGuild(), prefix)) {
            eventWrapper.reply(eventWrapper.localize("command.prefix.changed",
                    Replacement.create("PREFIX", prefix, Format.CODE))).queue();
        }
    }

    private boolean set(MessageEventWrapper eventWrapper, CommandContext subContext) {
        var optArg = subContext.argString(0);
        if (optArg.isEmpty()) return false;
        var prefix = optArg.get();

        if (!prefix.startsWith("re:") && prefix.length() > 3) {
            eventWrapper.reply(eventWrapper.localize("error.prefixTooLong")).queue();
            return true;
        }
        if (prefix.startsWith("re:")) {
            if (prefix.equalsIgnoreCase("re:")) {
                eventWrapper.reply(eventWrapper.localize("error.invalidRegex")).queue();
                return true;
            }
            var substring = prefix.substring(3);
            if (!substring.startsWith("^")) {
                substring = "^" + substring;
            }
            try {
                Pattern.compile(substring);
            } catch (PatternSyntaxException e) {
                eventWrapper.reply(eventWrapper.localize("error.invalidRegex")).queue();
                return true;
            }
            prefix = "re:" + substring;
        }
        changePrefix(eventWrapper, prefix);
        return true;
    }

    private boolean get(MessageEventWrapper eventWrapper) {
        var prefix = data.getPrefix(eventWrapper.getGuild()).orElse(configuration.getDefaultPrefix());
        eventWrapper.reply(eventWrapper.localize("command.prefix.show",
                Replacement.create("PREFIX", prefix, Format.CODE))).queue();
        return true;
    }


}
