package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.listener.CommandHub;
import de.chojo.jdautil.localization.ContextLocalizer;
import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Help extends SimpleCommand {
    private static final String REQ = "<%s>";
    private static final String OPT = "[%s]";
    private final CommandHub<SimpleCommand> hub;
    private final Localizer loc;

    public Help(CommandHub<SimpleCommand> hub, Localizer localizer, boolean exclusiveHelp) {
        super("help",
                null,
                "command.help.description",
                argsBuilder()
                        .add(OptionType.STRING, "command", "command.help.arguments.command")
                        .build(),
                exclusiveHelp ? Permission.ADMINISTRATOR : Permission.UNKNOWN);
        this.hub = hub;
        this.loc = localizer;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) {
            var message = getAllCommandsEmbed(eventWrapper);
            eventWrapper.reply(message).queue();
            return true;
        }

        var cmd = context.argString(0).get();

        var command = hub.getCommand(cmd);
        if (command.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.commandNotFound"), 10);
            return true;
        }

        if (!eventWrapper.getMember().hasPermission(command.get().permission())) {
            return true;
        }

        eventWrapper.reply(getCommandHelpEmbed(eventWrapper, command.get())).queue();
        return true;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        var eventWrapper = MessageEventWrapper.create(event);

        if (event.getOptions().isEmpty()) {
            var message = getAllCommandsEmbed(eventWrapper);
            event.reply(wrap(message)).queue();
            return;
        }

        @SuppressWarnings("ConstantConditions")
        var cmd = event.getOption("command").getAsString();
        var command = hub.getCommand(cmd);
        if (command.isEmpty() ||
                (!eventWrapper.getMember().hasPermission(command.get().permission())
                        && command.get().permission() != Permission.UNKNOWN)) {
            event.reply(eventWrapper.localize("error.commandNotFound"))
                    .delay(Duration.ofSeconds(10))
                    .flatMap(InteractionHook::deleteOriginal)
                    .queue();
            return;
        }

        eventWrapper.reply(getCommandHelpEmbed(eventWrapper, command.get())).queue();
    }

    @NotNull
    private MessageEmbed getAllCommandsEmbed(MessageEventWrapper eventWrapper) {
        var commands = hub.getCommands().stream()
                .filter(c -> hub.canExecute(eventWrapper, c))
                .map(c -> "`" + c.command() + "`")
                .collect(Collectors.joining(", "));
        return new LocalizedEmbedBuilder(loc, eventWrapper)
                .setTitle("command.help.list.title")
                .setDescription(eventWrapper.localize("command.help.list.list",
                        Replacement.create("COMMAND", "help <command>", Format.CODE)))
                .addField("", commands, false)
                .build();
    }

    private MessageEmbed getCommandHelpEmbed(MessageEventWrapper eventWrapper, SimpleCommand command) {
        var embedBuilder = new LocalizedEmbedBuilder(loc, eventWrapper)
                .setTitle(eventWrapper.localize("command.help.title",
                        Replacement.create("COMMAND", command.command())))
                .setDescription(command.description());


        if (command.alias() != null) {
            embedBuilder.addField("command.help.usage", command.alias() + " " + command.args(), false);
        }

        List<String> subCommands = new ArrayList<>();
        for (var subCommand : command.getSubCommands()) {
            subCommands.add("`" + command.command() + " "
                    + subCommand.name() + (subCommand.args() != null ? " " + subCommand.args() : "")
                    + "` -> " + subCommand.description());
        }
        if (!subCommands.isEmpty()) {
            embedBuilder.addField("command.help.subCommands", String.join("\n", subCommands), false);
        }
        return embedBuilder.build();
    }

    public MessageEmbed getCommandHelp(SimpleCommand command, ILocalizer localizer, Guild guild) {
        var loc = localizer.getContextLocalizer(guild);

        var builder = new LocalizedEmbedBuilder(localizer, guild)
                .setTitle(loc.localize("command.help.title",
                        Replacement.create("COMMAND", command.command())))
                .setDescription(loc.localize(description()));

        if (command.alias().length > 0) {
            var aliases = Arrays.stream(command.alias())
                    .map(s -> StringUtils.wrap(s, "`"))
                    .collect(Collectors.joining(", "));
            builder.addField("command.help.alias", aliases, false);
        }

        if (subCommands() != null) {
            List<String> commands = new ArrayList<>();
            for (var simpleSubCommand : subCommands()) {
                commands.add(simpleSubCommand.name() + " " + argsAsString(simpleSubCommand.args(), loc));
            }
            builder.addField("command.help.subCommands", String.join("\n", commands), false);
        }

        if (args() != null) {
            builder.addField("command.help.usage", argsAsString(command.args(), loc), false);
        }

        return builder.build();
    }

    private String argsAsString(SimpleArgument[] args, ContextLocalizer localizer) {
        List<String> strArgs = new ArrayList<>();
        for (var arg : args) {
            strArgs.add(String.format(arg.isRequired() ? REQ : OPT, localizer.localize(arg.name())));
        }
        return String.join(" ", strArgs);
    }
}
