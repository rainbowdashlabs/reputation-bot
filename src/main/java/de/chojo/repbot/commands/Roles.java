package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
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

    public Roles(DataSource dataSource, Localizer localizer) {
        super("roles", new String[] {"role"},
                "Manage reputation roles.",
                null,
                subCommandBuilder()
                        .add("add", "<role> <reputation>", "Add a reputation role.")
                        .add("remove", "<role>", "Remove a reputation role.")
                        .add("list", null, "List repuation roles.")
                        .build(),
                Permission.ADMINISTRATOR);
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
            return add(eventWrapper, context.subCommandcontext(subCmd));
        }

        if ("remove".equalsIgnoreCase(subCmd)) {
            return remove(eventWrapper, context.subCommandcontext(subCmd));
        }
        return false;
    }

    private boolean remove(MessageEventWrapper eventWrapper, CommandContext subCommandcontext) {
        var role = subCommandcontext.argString(0);
        if (subCommandcontext.argsEmpty() || role.isEmpty()) return false;
        var roleById = DiscordResolver.getRole(eventWrapper.getGuild(), role.get());
        if (roleById.isEmpty()) {
            eventWrapper.replyErrorAndDelete("Invalid role", 30);
            return true;
        }

        if (data.removeReputationRole(eventWrapper.getGuild(), roleById.get())) {
            eventWrapper.replyNonMention("Removed role **" + roleById.get().getName() + ".").queue();
            return true;
        }
        eventWrapper.replyNonMention("This role is not a reputation role.").queue();
        return true;
    }

    private boolean add(MessageEventWrapper eventWrapper, CommandContext subCommandcontext) {
        var role = subCommandcontext.argString(0);
        var reputation = subCommandcontext.argLong(1);
        if (subCommandcontext.argsEmpty() || role.isEmpty() || reputation.isEmpty()) return false;
        var roleById = DiscordResolver.getRole(eventWrapper.getGuild(), role.get());
        if (roleById.isEmpty()) {
            eventWrapper.replyErrorAndDelete("Invalid role", 30);
            return true;
        }
        if (data.addReputationRole(eventWrapper.getGuild(), roleById.get(), reputation.get())) {
            eventWrapper.replyNonMention("Added role **" + roleById.get().getName() + "** with reputation **" + reputation.get() + "**.").queue();
        }
        return true;
    }

    private boolean list(MessageEventWrapper eventWrapper) {
        var reputationRoles = data.getReputationRoles(eventWrapper.getGuild())
                .stream()
                .filter(role -> role.getRole(eventWrapper.getGuild()) != null)
                .collect(Collectors.toList());
        var builder = TextFormatting.getTableBuilder(reputationRoles, "role", "id", "reputation");

        for (var role : reputationRoles) {
            builder.setNextRow(role.getRole().getName(), String.valueOf(role.getRoleId()), String.valueOf(role.getReputation()));
        }
        eventWrapper.replyNonMention(builder.toString()).queue();
        return true;
    }
}
