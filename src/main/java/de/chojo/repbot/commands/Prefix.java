package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
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

    public Prefix(DataSource dataSource, Configuration configuration) {
        super("prefix", null, "Manage prefix", "prefix [set <prefix>|reset]", Permission.ADMINISTRATOR);
        data = new GuildData(dataSource);
        this.configuration = configuration;
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
        if (data.setPrefix(wrapper.getGuild(), null)) {
            wrapper.replyNonMention("Prefix set to `" + prefix + "`.").queue();
        }
    }

    private boolean set(MessageEventWrapper eventWrapper, CommandContext subCommandcontext) {
        var optArg = subCommandcontext.argString(0);
        if (optArg.isEmpty()) return false;
        var prefix = optArg.get();

        if (prefix.length() > 3) {
            eventWrapper.replyNonMention("Prefix can be only 3 chars long.").queue();
            return true;
        }
        changePrefix(eventWrapper, prefix);
        return true;
    }

    private boolean get(MessageEventWrapper eventWrapper) {
        var prefix = data.getPrefix(eventWrapper.getGuild()).orElse(configuration.get(ConfigFile::getDefaultPrefix));
        eventWrapper.replyNonMention("The current prefix is `" + prefix + "`.").queue();
        return true;
    }


}
