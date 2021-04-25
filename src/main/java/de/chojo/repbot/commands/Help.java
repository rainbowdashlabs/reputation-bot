package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.listener.CommandHub;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Help extends SimpleCommand {
    private CommandHub<SimpleCommand> hub;
    private Localizer loc;

    public Help(CommandHub<SimpleCommand> hub, Localizer localizer) {
        super("help",
                null,
                "command.help.description",
                "[command]",
                null,
                Permission.UNKNOWN);
        this.hub = hub;
        this.loc = localizer;
    }


    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) {

            var commands = hub.getCommands().stream()
                    .filter(c -> eventWrapper.getMember().hasPermission(c.getPermission()))
                    .map(c -> "`" + c.getCommand() + "`")
                    .collect(Collectors.joining(", "));
            var message = new LocalizedEmbedBuilder(loc, eventWrapper)
                    .setTitle("command.help.list.title")
                    .setDescription(eventWrapper.localize("command.help.list.list",
                            Replacement.create("COMMAND", "help <command>", Format.CODE)))
                    .addField("", commands, false)
                    .build();
            eventWrapper.reply(message).queue();
            return true;
        }

        var cmd = context.argString(0).get();

        var command = hub.getCommand(cmd);
        if (command.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.commandNotFound"), 10);
            return true;
        }

        eventWrapper.reply(getcommandHelpEmbed(eventWrapper, command.get())).queue();
        return true;
    }

    private MessageEmbed getcommandHelpEmbed(MessageEventWrapper eventWrapper, SimpleCommand command) {
        var embedBuilder = new LocalizedEmbedBuilder(loc, eventWrapper)
                .setTitle(eventWrapper.localize("command.help.title",
                        Replacement.create("COMMAND", command.getCommand())))
                .setDescription(command.getDescription());

        if (command.getAlias().length > 0) {
            var aliases = Arrays.stream(command.getAlias())
                    .map(s -> StringUtils.wrap(s, "`"))
                    .collect(Collectors.joining(", "));
            embedBuilder.addField("command.help.alias", aliases, false);
        }

        if (command.getArgs() != null) {
            embedBuilder.addField("command.help.usage", command.getCommand() + " " + command.getArgs(), false);
        }

        List<String> subCommands = new ArrayList<>();
        for (var subCommand : command.getSubCommands()) {
            subCommands.add("`" + command.getCommand() + " "
                    + subCommand.getName() + (subCommand.getArgs() != null ? " " + subCommand.getArgs() : "")
                    + "` -> " + subCommand.getDescription());
        }
        if (!subCommands.isEmpty()) {
            embedBuilder.addField("command.help.subCommands", String.join("\n", subCommands), false);
        }
        return embedBuilder.build();
    }
}
