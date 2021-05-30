package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.text.TextFormatting;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.repbot.data.GuildData;
import net.dv8tion.jda.api.Permission;

import javax.sql.DataSource;
import java.util.stream.Collectors;

public class Roles extends SimpleCommand {
    private final GuildData data;

    public Roles(DataSource dataSource) {
        super("roles", new String[]{"role"},
                "command.roles.description",
                null,
                subCommandBuilder()
                        .add("managerRole", "[role]", "command.roles.sub.managerRole")
                        .add("add", "<role> <reputation>", "command.roles.sub.add")
                        .add("remove", "<role>", "command.roles.sub.remove")
                        .add("list", null, "command.roles.sub.list")
                        .build(),
                Permission.MANAGE_SERVER);
        data = new GuildData(dataSource);
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) return false;

        var optArg = context.argString(0);
        if (optArg.isEmpty()) return false;
        var subCmd = optArg.get();

        if ("list".equalsIgnoreCase(subCmd)) {
            return list(eventWrapper);
        }

        if ("add".equalsIgnoreCase(subCmd)) {
            return add(eventWrapper, context.subContext(subCmd));
        }

        if ("remove".equalsIgnoreCase(subCmd)) {
            return remove(eventWrapper, context.subContext(subCmd));
        }
        if ("managerRole".equalsIgnoreCase(subCmd)) {
            return managerRole(eventWrapper, context.subContext(subCmd));
        }
        return false;
    }

    private boolean managerRole(MessageEventWrapper eventWrapper, CommandContext subContext) {
        if (subContext.argsEmpty()) {
            var guildSettings = data.getGuildSettings(eventWrapper.getGuild());
            if (guildSettings.isEmpty()) return true;
            var settings = guildSettings.get();
            settings.getManagerRole().ifPresentOrElse(r -> {
                var roleById = eventWrapper.getGuild().getRoleById(r);
                if (roleById != null) {
                    eventWrapper.reply(eventWrapper.localize("command.roles.sub.managerRole.current",
                            Replacement.createMention(roleById))).queue();
                    return;
                }
                eventWrapper.reply(eventWrapper.localize("command.roles.sub.managerRole.noRole")).queue();
            }, () -> eventWrapper.reply(eventWrapper.localize("command.roles.sub.managerRole.noRole")).queue());
            return true;
        }
        var role = DiscordResolver.getRole(eventWrapper.getGuild(), subContext.argString(0).get());
        if (role.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.invalidRole"), 10);
            return true;
        }
        if (data.setManagerRole(eventWrapper.getGuild(), role.get())) {
            eventWrapper.reply(eventWrapper.localize("command.roles.sub.managerRole.set",
                    Replacement.createMention(role.get()))).queue();
        }
        return true;
    }

    private boolean remove(MessageEventWrapper eventWrapper, CommandContext commandContext) {
        var role = commandContext.argString(0);
        if (commandContext.argsEmpty() || role.isEmpty()) return false;
        var roleById = DiscordResolver.getRole(eventWrapper.getGuild(), role.get());
        if (roleById.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.invalidRole"), 30);
            return true;
        }

        if (data.removeReputationRole(eventWrapper.getGuild(), roleById.get())) {
            eventWrapper.reply(eventWrapper.localize("command.roles.sub.remove.removed",
                    Replacement.create("ROLE", roleById.get().getName(), Format.BOLD))).queue();
            return true;
        }
        eventWrapper.replyErrorAndDelete(eventWrapper.localize("command.roles.sub.remove.notARepRole"), 10);
        return true;
    }

    private boolean add(MessageEventWrapper eventWrapper, CommandContext commandContext) {
        var role = commandContext.argString(0);
        var reputation = commandContext.argLong(1);
        if (commandContext.argsEmpty() || role.isEmpty() || reputation.isEmpty()) return false;
        var roleById = DiscordResolver.getRole(eventWrapper.getGuild(), role.get());
        if (roleById.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.invalidRole"), 30);
            return true;
        }
        if (data.addReputationRole(eventWrapper.getGuild(), roleById.get(), reputation.get())) {
            eventWrapper.reply(eventWrapper.localize("command.roles.sub.add.added",
                    Replacement.create("ROLE", roleById.get().getName(), Format.BOLD), Replacement.create("POINTS", reputation.get()))).queue();
        }
        return true;
    }

    private boolean list(MessageEventWrapper eventWrapper) {
        var reputationRoles = data.getReputationRoles(eventWrapper.getGuild())
                .stream()
                .filter(role -> role.getRole(eventWrapper.getGuild()) != null)
                .collect(Collectors.toList());
        var builder = TextFormatting.getTableBuilder(reputationRoles,
                eventWrapper.localize("words.role"),
                "id",
                eventWrapper.localize("words.reputation"));

        for (var role : reputationRoles) {
            builder.setNextRow(role.getRole().getName(), String.valueOf(role.getRoleId()), String.valueOf(role.getReputation()));
        }
        eventWrapper.reply(builder.toString()).queue();
        return true;
    }
}
