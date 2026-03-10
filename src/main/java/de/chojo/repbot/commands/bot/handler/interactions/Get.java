/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot.handler.interactions;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.dv8tion.jda.api.interactions.commands.Command.*;

public class Get implements SlashHandler {
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        Long guildId = event.getOption("guild_id", OptionMapping::getAsLong);
        Guild guildById = event.getJDA().getShardManager().getGuildById(guildId);
        List<Command> local = guildById.retrieveCommands().complete();
        List<Command> global = event.getJDA().retrieveCommands().complete();

        String localString = local.stream().map(this::format).collect(Collectors.joining("\n"));
        String globalString = global.stream().map(this::format).collect(Collectors.joining("\n"));

        StringBuilder builder = new StringBuilder();

        if (!globalString.isBlank()) {
            builder.append("Global\n```");
            builder.append(globalString);
            builder.append("\n```");
        }

        if (!localString.isBlank()) {
            builder.append("Local\n```");
            builder.append(localString);
            builder.append("\n```");
        }

        MessageEmbed build =
                new EmbedBuilder().setDescription(builder.toString()).build();

        event.replyEmbeds(build).setEphemeral(true).complete();
    }

    private String format(Command command) {
        List<String> commands = new ArrayList<>();
        commands.add(
                command.getType().name() + " " + command.getFullCommandName() + ": " + format(command.getOptions()));
        for (Subcommand subcommand : command.getSubcommands()) {
            commands.add("  " + format(subcommand));
        }
        for (SubcommandGroup subcommandGroup : command.getSubcommandGroups()) {
            commands.add("  " + subcommandGroup.getName());
            for (Subcommand subcommand : subcommandGroup.getSubcommands()) {
                commands.add("    " + format(subcommand));
            }
        }
        return String.join("\n", commands);
    }

    private String format(List<Option> options) {
        return options.stream().map(e -> "%s".formatted(e.getName())).collect(Collectors.joining(" "));
    }

    private String format(Subcommand subcommand) {
        return subcommand.getName() + ": " + format(subcommand.getOptions());
    }

    private String format(Type type) {
        return switch (type) {
            case UNKNOWN -> null;
            case SLASH -> "S";
            case USER -> "U";
            case MESSAGE -> "M";
        };
    }

    private String format(OptionType type) {
        return switch (type) {
            case UNKNOWN -> "X";
            case SUB_COMMAND -> "SC";
            case SUB_COMMAND_GROUP -> "SCG";
            case STRING -> "S";
            case INTEGER -> "I";
            case BOOLEAN -> "B";
            case USER -> "U";
            case CHANNEL -> "C";
            case ROLE -> "R";
            case MENTIONABLE -> "M";
            case NUMBER -> "N";
            case ATTACHMENT -> "A";
        };
    }
}
