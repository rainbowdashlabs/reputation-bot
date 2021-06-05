package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.text.TextFormatting;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.stream.Collectors;

public class Roles extends SimpleCommand {
    private final GuildData data;
    private final ILocalizer loc;

    public Roles(DataSource dataSource, ILocalizer loc) {
        super("roles", new String[]{"role"},
                "command.roles.description",
                subCommandBuilder()
                        .add("managerrole", "command.roles.sub.managerRole", argsBuilder()
                                .add(OptionType.ROLE, "role", "role")
                                .build()
                        )
                        .add("add", "command.roles.sub.add", argsBuilder()
                                .add(OptionType.ROLE, "role", "role", true)
                                .add(OptionType.INTEGER, "reputation", "reputation", true)
                                .build()
                        )
                        .add("remove", "command.roles.sub.remove", argsBuilder()
                                .add(OptionType.ROLE, "role", "role", true)
                                .build()
                        )
                        .add("list", "command.roles.sub.list")
                        .build(),
                Permission.MANAGE_SERVER);
        data = new GuildData(dataSource);
        this.loc = loc;
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

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        var subCmd = event.getSubcommandName();
        if ("list".equalsIgnoreCase(subCmd)) {
            list(event);
        }

        if ("add".equalsIgnoreCase(subCmd)) {
            add(event);
        }

        if ("remove".equalsIgnoreCase(subCmd)) {
            remove(event);
        }
        if ("managerRole".equalsIgnoreCase(subCmd)) {
            managerRole(event);
        }

    }

    private boolean managerRole(MessageEventWrapper eventWrapper, CommandContext subContext) {
        if (subContext.argsEmpty()) {
            var guildSettings = data.getGuildSettings(eventWrapper.getGuild());
            if (guildSettings.isEmpty()) return true;
            var settings = guildSettings.get();
            eventWrapper.reply(getManagerRoleMessage(eventWrapper.getGuild(), settings)).queue();
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

    private void managerRole(SlashCommandEvent event) {
        var loc = this.loc.getContextLocalizer(event.getGuild());
        if (event.getOptions().isEmpty()) {
            var guildSettings = data.getGuildSettings(event.getGuild());
            if (guildSettings.isEmpty()) return;

            var settings = guildSettings.get();
            event.reply(getManagerRoleMessage(event.getGuild(), settings)).allowedMentions(Collections.emptyList()).queue();
            return;
        }
        var role = event.getOption("role").getAsRole();
        if (data.setManagerRole(event.getGuild(), role)) {
            event.reply(loc.localize("command.roles.sub.managerRole.set",
                    Replacement.createMention(role)))
                    .allowedMentions(Collections.emptyList()).queue();
        }
    }

    private String getManagerRoleMessage(Guild guild, GuildSettings settings) {
        if (settings.getManagerRole().isPresent()) {
            var roleById = guild.getRoleById(settings.getManagerRole().getAsLong());
            if (roleById != null) {
                return loc.localize("command.roles.sub.managerRole.current", guild,
                        Replacement.createMention(roleById));
            }
        }
        return loc.localize("command.roles.sub.managerRole.noRole", guild);
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

    private void remove(SlashCommandEvent event) {
        var loc = this.loc.getContextLocalizer(event.getGuild());
        var role = event.getOption("role").getAsRole();

        if (data.removeReputationRole(event.getGuild(), role)) {
            event.reply(loc.localize("command.roles.sub.remove.removed",
                    Replacement.createMention("ROLE", role))).allowedMentions(Collections.emptyList()).queue();
            return;
        }
        event.reply(loc.localize("command.roles.sub.remove.notARepRole")).setEphemeral(true).queue();
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

    private boolean add(SlashCommandEvent event) {
        var role = event.getOption("role").getAsRole();
        var reputation = event.getOption("reputation").getAsLong();
        if (data.addReputationRole(event.getGuild(), role, reputation)) {
            event.reply(loc.localize("command.roles.sub.add.added", event.getGuild(),
                    Replacement.createMention("ROLE", role), Replacement.create("POINTS", reputation)))
                    .allowedMentions(Collections.emptyList()).queue();
        }
        return true;
    }

    private boolean list(MessageEventWrapper eventWrapper) {
        eventWrapper.reply(getRoleList(eventWrapper.getGuild())).queue();
        return true;
    }

    private void list(SlashCommandEvent event) {
        event.reply(getRoleList(event.getGuild())).allowedMentions(Collections.emptyList()).queue();
    }

    private String getRoleList(Guild guild) {
        return data.getReputationRoles(guild)
                .stream()
                .filter(role -> role.getRole(guild) != null)
                .map(role -> role.getReputation() + " ➜ " + role.getRole(guild).getAsMention())
                .collect(Collectors.joining("\n"));
    }
}
