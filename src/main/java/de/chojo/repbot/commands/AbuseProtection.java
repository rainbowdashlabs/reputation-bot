package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.command.SimpleSubCommand;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.Nullable;

public class AbuseProtection extends SimpleCommand {
    protected AbuseProtection(String command, @Nullable String[] alias, String description, @Nullable SimpleSubCommand[] subCommands, Permission permission) {
        super(command, alias, description, subCommands, permission);
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        return false;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {

    }
}
