package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.listener.CommandHub;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Help extends SimpleCommand {
    private CommandHub<SimpleCommand> hub;

    public Help(CommandHub<SimpleCommand> hub) {
        super("help",
                null,
                "Get help!",
                "[command]",
                null,
                Permission.UNKNOWN);
        this.hub = hub;
    }


    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) {

            var commands = hub.getCommands().stream()
                    .filter(c -> eventWrapper.getMember().hasPermission(c.getPermission()))
                    .map(c -> "`" + c.getCommand() + "`")
                    .collect(Collectors.joining(", "));
            var message = new EmbedBuilder()
                    .setTitle("Reputation Bot Help")
                    .setDescription("Here is a list of all commands.\nUse `help <command>` for more information:\n")
                    .addField("", commands, false)
                    .build();
            eventWrapper.replyNonMention(message).queue();
            return true;
        }

        var cmd = context.argString(0).get();

        var command = hub.getCommand(cmd);
        if (command.isEmpty()) {
            eventWrapper.replyErrorAndDelete("Command not found.", 10);
            return true;
        }

        eventWrapper.replyNonMention(getcommandHelpEmbed(command.get())).queue();
        return true;
    }

    private MessageEmbed getcommandHelpEmbed(SimpleCommand command) {
        var embedBuilder = new EmbedBuilder()
                .setTitle(command.getCommand() + " - Help")
                .setDescription(command.getDescription());

        if (command.getAlias().length > 0) {
            var aliases = Arrays.stream(command.getAlias())
                    .map(s -> StringUtils.wrap(s, "`"))
                    .collect(Collectors.joining(", "));
            embedBuilder.addField("Aliases", aliases, false);
        }

        if (command.getArgs() != null) {
            embedBuilder.addField("Usage", command.getCommand() + " " + command.getArgs(), false);
        }

        List<String> subCommands = new ArrayList<>();
        for (var subCommand : command.getSubCommands()) {
            subCommands.add( "`" + command.getCommand() + " "
                    + subCommand.getName() + (subCommand.getArgs() != null ? " " + subCommand.getArgs() : "")
                    + "` -> " + subCommand.getDescription());
        }
        if (!subCommands.isEmpty()) {
            embedBuilder.addField("Subcommands", String.join("\n", subCommands), false);
        }
        return embedBuilder.build();
    }
}
