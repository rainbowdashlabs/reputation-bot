package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
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
    private final Localizer loc;

    public Roles(DataSource dataSource, Localizer localizer) {
        super("roles", new String[] {"role"},
                "command.roles.description",
                null,
                subCommandBuilder()
                        .add("add", "<role> <reputation>", "command.roles.sub.add")
                        .add("remove", "<role>", "command.roles.sub.remove")
                        .add("list", null, "command.roles.sub.list")
                        .build(),
                Permission.ADMINISTRATOR);
        data = new GuildData(dataSource);
        loc = localizer;
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
            return add(eventWrapper, context.subCommandcontext(subCmd));
        }

        if ("remove".equalsIgnoreCase(subCmd)) {
            return remove(eventWrapper, context.subCommandcontext(subCmd));
        }
        return false;
    }

    private boolean remove(MessageEventWrapper eventWrapper, CommandContext commandContext) {
        var role = commandContext.argString(0);
        if (commandContext.argsEmpty() || role.isEmpty()) return false;
        var roleById = DiscordResolver.getRole(eventWrapper.getGuild(), role.get());
        if (roleById.isEmpty()) {
            eventWrapper.replyErrorAndDelete(loc.localize("error.invalidRole", eventWrapper), 30);
            return true;
        }

        if (data.removeReputationRole(eventWrapper.getGuild(), roleById.get())) {
            eventWrapper.replyNonMention(loc.localize("command.roles.sub.remove.removed", eventWrapper,
                    Replacement.create("ROLE", roleById.get().getName(), Format.BOLD))).queue();
            return true;
        }
        eventWrapper.replyErrorAndDelete(loc.localize("command.roles.sub.remove.notARepRole", eventWrapper), 10);
        return true;
    }

    private boolean add(MessageEventWrapper eventWrapper, CommandContext commandContext) {
        var role = commandContext.argString(0);
        var reputation = commandContext.argLong(1);
        if (commandContext.argsEmpty() || role.isEmpty() || reputation.isEmpty()) return false;
        var roleById = DiscordResolver.getRole(eventWrapper.getGuild(), role.get());
        if (roleById.isEmpty()) {
            eventWrapper.replyErrorAndDelete(loc.localize("error.invalidRole", eventWrapper), 30);
            return true;
        }
        if (data.addReputationRole(eventWrapper.getGuild(), roleById.get(), reputation.get())) {
            eventWrapper.replyNonMention(loc.localize("command.roles.sub.add.added", eventWrapper,
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
                loc.localize("words.role", eventWrapper),
                "id",
                loc.localize("words.reputation", eventWrapper));

        for (var role : reputationRoles) {
            builder.setNextRow(role.getRole().getName(), String.valueOf(role.getRoleId()), String.valueOf(role.getReputation()));
        }
        eventWrapper.replyNonMention(builder.toString()).queue();
        return true;
    }
}
