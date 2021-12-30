package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.command.dispatching.CommandHub;
import de.chojo.jdautil.localization.ContextLocalizer;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.Permission;
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
        loc = localizer;
    }

    public static MessageEmbed getCommandHelp(SimpleCommand command, ContextLocalizer loc) {
        var builder = new LocalizedEmbedBuilder(loc)
                .setTitle(loc.localize("command.help.title",
                        Replacement.create("COMMAND", command.command())))
                .setDescription(loc.localize(command.description()));

        if (command.alias().length > 0) {
            var aliases = Arrays.stream(command.alias())
                    .map(s -> StringUtils.wrap(s, "`"))
                    .collect(Collectors.joining(", "));
            builder.addField("command.help.alias", aliases, false);
        }

        if (command.subCommands() != null) {
            List<String> commands = new ArrayList<>();
            for (var simpleSubCommand : command.subCommands()) {
                commands.add("`" + simpleSubCommand.name() + " " + argsAsString(simpleSubCommand.args(), loc) + "` ➜ " + loc.localize(simpleSubCommand.description()));
            }
            builder.addField("command.help.subCommands", String.join("\n", commands), false);
        }

        if (command.args() != null) {
            builder.addField("command.help.usage", argsAsString(command.args(), loc), false);
        }

        return builder.build();
    }

    private static String argsAsString(SimpleArgument[] args, ContextLocalizer localizer) {
        if (args == null) return "";
        List<String> strArgs = new ArrayList<>();
        for (var arg : args) {
            strArgs.add(String.format(arg.isRequired() ? REQ : OPT, localizer.localize(arg.name())));
        }
        return String.join(" ", strArgs);
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {
        if (event.getOptions().isEmpty()) {
            var message = getAllCommandsEmbed(event);
            event.replyEmbeds(message).queue();
            return;
        }

        @SuppressWarnings("ConstantConditions")
        var cmd = event.getOption("command").getAsString();
        var command = hub.getCommand(cmd);
        if (command.isEmpty() ||
            (!event.getMember().hasPermission(command.get().permission())
             && command.get().permission() != Permission.UNKNOWN)) {
            event.reply(loc.localize("error.commandNotFound", event))
                    .delay(Duration.ofSeconds(10))
                    .flatMap(InteractionHook::deleteOriginal)
                    .queue();
            return;
        }

        event.reply(wrap(getCommandHelp(command.get(), loc.getContextLocalizer(event.getGuild())))).queue();
    }

    @NotNull
    private MessageEmbed getAllCommandsEmbed(SlashCommandEvent event) {
        var commands = hub.getCommands().stream()
                .filter(c -> hub.canExecute(event, c))
                .map(c -> "`" + c.command() + "`")
                .collect(Collectors.joining(", "));
        return new LocalizedEmbedBuilder(loc, event)
                .setTitle("command.help.list.title")
                .setDescription(loc.localize("command.help.list.list", event,
                        Replacement.create("COMMAND", "help <command>", Format.CODE)))
                .addField("", commands, false)
                .build();
    }
}
