package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.repbot.config.ConfigFile;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.data.GuildData;
import net.dv8tion.jda.api.Permission;

import javax.sql.DataSource;

public class Prefix extends SimpleCommand {
    private final GuildData data;
    private final Configuration configuration;
    private Localizer loc;

    public Prefix(DataSource dataSource, Configuration configuration, Localizer localizer) {
        super("prefix",
                null,
                "command.prefix.description",
                "",
                subCommandBuilder().add("set", "<prefix>", "command.prefix.sub.set")
                        .add("reset", null, "command.prefix.sub.reset")
                        .build(),
                Permission.ADMINISTRATOR);
        data = new GuildData(dataSource);
        this.configuration = configuration;
        loc = localizer;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        var optSubCmd = context.argString(0);
        if (optSubCmd.isPresent()) {
            var subCmd = optSubCmd.get();
            if ("set".equalsIgnoreCase(subCmd)) {
                return set(eventWrapper, context.subCommandcontext(subCmd));
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
        changePrefix(eventWrapper, configuration.get(ConfigFile::getDefaultPrefix));
        return true;
    }

    private void changePrefix(MessageEventWrapper wrapper, String prefix) {
        if (data.setPrefix(wrapper.getGuild(), prefix)) {
            wrapper.replyNonMention(loc.localize("command.prefix.changed", wrapper,
                    Replacement.create("PREFIX", prefix, Format.CODE))).queue();
        }
    }

    private boolean set(MessageEventWrapper eventWrapper, CommandContext subCommandcontext) {
        var optArg = subCommandcontext.argString(0);
        if (optArg.isEmpty()) return false;
        var prefix = optArg.get();

        if (prefix.length() > 3) {
            eventWrapper.replyNonMention(loc.localize("error.prefixTooLong", eventWrapper)).queue();
            return true;
        }
        changePrefix(eventWrapper, prefix);
        return true;
    }

    private boolean get(MessageEventWrapper eventWrapper) {
        var prefix = data.getPrefix(eventWrapper.getGuild()).orElse(configuration.get(ConfigFile::getDefaultPrefix));
        eventWrapper.replyNonMention(loc.localize("command.prefix.show", eventWrapper,
                Replacement.create("PREFIX", prefix, Format.CODE))).queue();
        return true;
    }


}
