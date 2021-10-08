package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import de.chojo.repbot.service.RoleAccessException;
import de.chojo.repbot.service.RoleAssigner;
import de.chojo.repbot.util.StringUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Roles extends SimpleCommand {
    private final GuildData guildData;
    private final ILocalizer loc;
    private final RoleAssigner roleAssigner;
    private final Set<Long> running = new HashSet<>();

    public Roles(DataSource dataSource, ILocalizer loc, RoleAssigner roleAssigner) {
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
                        .add("adddonor", "command.roles.sub.addDonor", argsBuilder()
                                .add(OptionType.ROLE, "role", "role", true)
                                .build()
                        )
                        .add("addreceiver", "command.roles.sub.addReceiver", argsBuilder()
                                .add(OptionType.ROLE, "role", "role", true)
                                .build()
                        )
                        .add("removedonor", "command.roles.sub.removeDonor", argsBuilder()
                                .add(OptionType.ROLE, "role", "role", true)
                                .build()
                        )
                        .add("removereceiver", "command.roles.sub.removeReceiver", argsBuilder()
                                .add(OptionType.ROLE, "role", "role", true)
                                .build()
                        )
                        .add("refresh", "command.roles.sub.refresh", argsBuilder()
                                .build()
                        )
                        .add("list", "command.roles.sub.list")
                        .add("stackroles", "command.roles.sub.stackRoles")
                        .build(),
                Permission.MANAGE_SERVER);
        guildData = new GuildData(dataSource);
        this.loc = loc;
        this.roleAssigner = roleAssigner;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) return false;

        var subCmd = context.argString(0).get();

        if ("list".equalsIgnoreCase(subCmd)) {
            return list(eventWrapper);
        }

        if ("managerRole".equalsIgnoreCase(subCmd)) {
            return managerRole(eventWrapper, context.subContext(subCmd));
        }

        if ("stackRoles".equalsIgnoreCase(subCmd)) {
            return stackRoles(eventWrapper, context.subContext(subCmd));
        }

        if ("refresh".equalsIgnoreCase(subCmd)) {
            return refresh(eventWrapper);
        }

        if (StringUtil.contains(subCmd, "add", "remove", "addDonor", "addReceiver", "removeDonor", "removeReceiver")) {
            context.parseQuoted();
            var roleString = context.argString(0);
            if (context.argsEmpty() || roleString.isEmpty()) return false;

            var role = DiscordResolver.getRole(eventWrapper.getGuild(), roleString.get());
            if (role.isEmpty()) {
                eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.invalidRole"), 10);
                return true;
            }

            if ("add".equalsIgnoreCase(subCmd)) {
                return add(eventWrapper, context.subContext(subCmd), role.get());
            }

            if ("remove".equalsIgnoreCase(subCmd)) {
                return remove(eventWrapper, role.get());
            }
            if ("addDonor".equalsIgnoreCase(subCmd)) {
                return addDonor(eventWrapper, role.get());
            }

            if ("addReceiver".equalsIgnoreCase(subCmd)) {
                return addReceiver(eventWrapper, role.get());
            }
            if ("removeDonor".equalsIgnoreCase(subCmd)) {
                return removeDonor(eventWrapper, role.get());
            }

            if ("removeReceiver".equalsIgnoreCase(subCmd)) {
                return removeReceiver(eventWrapper, role.get());
            }
        }

        return false;
    }

    private boolean refresh(MessageEventWrapper event) {
        if (running.contains(event.getGuild().getIdLong())) {
            event.reply(loc.localize("command.roles.sub.refresh.running")).queue();
            return true;
        }

        running.add(event.getGuild().getIdLong());

        event.reply(loc.localize("command.roles.sub.refresh.started", event.getGuild())).queue();
        roleAssigner
                .updateBatch(event.getGuild())
                .thenRun(() -> event.reply(loc.localize("command.roles.sub.refresh.finished", event.getGuild()))
                        .queue())
                .exceptionally(r -> {
                    if (r instanceof RoleAccessException) {
                        event.reply(loc.localize("error.roleAccess", event.getGuild(),
                                        Replacement.createMention("ROLE", ((RoleAccessException) r).role())))
                                .queue();
                    }
                    return null;
                }).thenRun(() -> {
                    running.remove(event.getGuild().getIdLong());
                });
        return true;
    }

    private boolean stackRoles(MessageEventWrapper eventWrapper, CommandContext context) {
        var settings = guildData.getGuildSettings(eventWrapper.getGuild());
        if (context.argsEmpty()) {
            eventWrapper.reply(getBooleanMessage(eventWrapper.getGuild(), settings.generalSettings().isStackRoles(),
                    "command.roles.sub.stackRoles.stacked", "command.roles.sub.stackRoles.notStacked")).queue();
            return true;
        }
        var state = context.argBoolean(0);
        if (state.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.notABoolean",
                    Replacement.create("input", context.argString(0).get())), 10);
            return true;
        }

        if (guildData.setRoleStacking(eventWrapper.getGuild(), state.get())) {
            eventWrapper.reply(getBooleanMessage(eventWrapper.getGuild(), state.get(),
                    "command.roles.sub.stackRoles.stacked", "command.roles.sub.stackRoles.notStacked")).queue();
        }
        return true;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {
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

        if ("addDonor".equalsIgnoreCase(subCmd)) {
            addDonor(event, event.getOption("role").getAsRole());
        }

        if ("addReceiver".equalsIgnoreCase(subCmd)) {
            addReceiver(event, event.getOption("role").getAsRole());
        }

        if ("removeDonor".equalsIgnoreCase(subCmd)) {
            removeDonor(event, event.getOption("role").getAsRole());
        }

        if ("removeReceiver".equalsIgnoreCase(subCmd)) {
            removeReceiver(event, event.getOption("role").getAsRole());
        }

        if ("refresh".equalsIgnoreCase(subCmd)) {
            refresh(event);
        }

        if ("stackRoles".equalsIgnoreCase(subCmd)) {
            stackRoles(event);
        }
    }

    private void refresh(SlashCommandEvent event) {
        if (running.contains(event.getGuild().getIdLong())) {
            event.reply(loc.localize("command.roles.sub.refresh.running")).queue();
            return;
        }

        running.add(event.getGuild().getIdLong());

        event.reply(loc.localize("command.roles.sub.refresh.started", event.getGuild())).queue();
        roleAssigner
                .updateBatch(event.getGuild())
                .thenRun(() -> event.getHook()
                        .editOriginal(loc.localize("command.roles.sub.refresh.finished", event.getGuild()))
                        .queue())
                .exceptionally(r -> {
                    if (r instanceof RoleAccessException) {
                        event.getHook()
                                .editOriginal(loc.localize("error.roleAccess", event.getGuild(),
                                        Replacement.createMention("ROLE", ((RoleAccessException) r).role())))
                                .queue();
                    }
                    return null;
                }).thenRun(() -> {
                    running.remove(event.getGuild().getIdLong());
                });
    }

    private void stackRoles(SlashCommandEvent event) {
        var settings = guildData.getGuildSettings(event.getGuild());
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(event.getGuild(), settings.generalSettings().isStackRoles(),
                    "command.roles.sub.stackRoles.stacked", "command.roles.sub.stackRoles.notStacked")).queue();
            return;
        }
        var state = event.getOption("state").getAsBoolean();

        if (guildData.setRoleStacking(event.getGuild(), state)) {
            event.reply(getBooleanMessage(event.getGuild(), state,
                    "command.roles.sub.stackRoles.stacked", "command.roles.sub.stackRoles.notStacked")).queue();
        }
    }

    private boolean managerRole(MessageEventWrapper eventWrapper, CommandContext subContext) {
        if (subContext.argsEmpty()) {
            var settings = guildData.getGuildSettings(eventWrapper.getGuild());
            eventWrapper.reply(getManagerRoleMessage(eventWrapper.getGuild(), settings)).queue();
            return true;
        }
        var role = DiscordResolver.getRole(eventWrapper.getGuild(), subContext.argString(0).get());
        if (role.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.invalidRole"), 10);
            return true;
        }
        if (guildData.setManagerRole(eventWrapper.getGuild(), role.get())) {
            eventWrapper.reply(eventWrapper.localize("command.roles.sub.managerRole.set",
                    Replacement.createMention(role.get()))).queue();
        }
        return true;
    }

    private void managerRole(SlashCommandEvent event) {
        var loc = this.loc.getContextLocalizer(event.getGuild());
        if (event.getOptions().isEmpty()) {
            var settings = guildData.getGuildSettings(event.getGuild());
            event.reply(getManagerRoleMessage(event.getGuild(), settings)).allowedMentions(Collections.emptyList()).queue();
            return;
        }
        var role = event.getOption("role").getAsRole();
        if (guildData.setManagerRole(event.getGuild(), role)) {
            event.reply(loc.localize("command.roles.sub.managerRole.set",
                            Replacement.createMention(role)))
                    .allowedMentions(Collections.emptyList()).queue();
        }
    }

    private String getManagerRoleMessage(Guild guild, GuildSettings settings) {
        if (settings.generalSettings().managerRole().isPresent()) {
            var roleById = guild.getRoleById(settings.generalSettings().managerRole().getAsLong());
            if (roleById != null) {
                return loc.localize("command.roles.sub.managerRole.current", guild,
                        Replacement.createMention(roleById));
            }
        }
        return loc.localize("command.roles.sub.managerRole.noRole", guild);
    }

    private boolean remove(MessageEventWrapper eventWrapper, Role role) {
        if (guildData.removeReputationRole(eventWrapper.getGuild(), role)) {
            eventWrapper.reply(eventWrapper.localize("command.roles.sub.remove.removed",
                    Replacement.create("ROLE", role.getName(), Format.BOLD))).queue();
            return true;
        }
        eventWrapper.replyErrorAndDelete(eventWrapper.localize("command.roles.sub.remove.notARepRole"), 10);
        return true;
    }

    private void remove(SlashCommandEvent event) {
        var loc = this.loc.getContextLocalizer(event.getGuild());
        var role = event.getOption("role").getAsRole();

        if (guildData.removeReputationRole(event.getGuild(), role)) {
            event.reply(loc.localize("command.roles.sub.remove.removed",
                    Replacement.createMention("ROLE", role))).allowedMentions(Collections.emptyList()).queue();
            return;
        }
        event.reply(loc.localize("command.roles.sub.remove.notARepRole")).setEphemeral(true).queue();
    }

    private boolean add(MessageEventWrapper eventWrapper, CommandContext commandContext, Role role) {
        var reputation = commandContext.argLong(1);
        if (commandContext.argsEmpty() || reputation.isEmpty()) return false;
        if (!eventWrapper.getGuild().getSelfMember().canInteract(role)) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.roleAccess", Replacement.createMention(role)), 15);
            return true;
        }
        if (guildData.addReputationRole(eventWrapper.getGuild(), role, reputation.get())) {
            eventWrapper.reply(eventWrapper.localize("command.roles.sub.add.added",
                    Replacement.create("ROLE", role.getName(), Format.BOLD), Replacement.create("POINTS", reputation.get()))).queue();
        }
        return true;
    }

    private void add(SlashCommandEvent event) {
        var role = event.getOption("role").getAsRole();
        var reputation = event.getOption("reputation").getAsLong();
        if (!event.getGuild().getSelfMember().canInteract(role)) {
            event.reply(loc.localize("error.roleAccess", event.getGuild(),
                    Replacement.createMention(role))).setEphemeral(true).queue();
            return;
        }

        if (guildData.addReputationRole(event.getGuild(), role, reputation)) {
            event.reply(loc.localize("command.roles.sub.add.added", event.getGuild(),
                            Replacement.createMention("ROLE", role), Replacement.create("POINTS", reputation)))
                    .allowedMentions(Collections.emptyList()).queue();
        }
    }

    private boolean list(MessageEventWrapper eventWrapper) {
        eventWrapper.reply(getRoleList(eventWrapper.getGuild())).queue();
        return true;
    }

    private void list(SlashCommandEvent event) {
        event.replyEmbeds(getRoleList(event.getGuild())).allowedMentions(Collections.emptyList()).queue();
    }

    private MessageEmbed getRoleList(Guild guild) {
        var reputationRoles = guildData.getReputationRoles(guild).stream()
                .filter(role -> role.getRole(guild) != null)
                .map(role -> role.reputation() + " ➜ " + role.getRole(guild).getAsMention())
                .collect(Collectors.joining("\n"));
        var guildSettings = guildData.getGuildSettings(guild);

        var builder = new LocalizedEmbedBuilder(loc, guild)
                .setTitle("Role Info");

        builder.addField("Reputation Roles", reputationRoles, true);

        var thankSettings = guildSettings.thankSettings();

        if (!thankSettings.donorRoles().isEmpty()) {
            var donorRoles = thankSettings.donorRoles()
                    .stream()
                    .map(guild::getRoleById)
                    .filter(Objects::nonNull)
                    .map(IMentionable::getAsMention)
                    .collect(Collectors.joining("\n"));

            builder.addField("Donor Roles", donorRoles, true);
        }
        if (!thankSettings.receiverRoles().isEmpty()) {
            var receiverRoles = thankSettings.receiverRoles()
                    .stream()
                    .map(guild::getRoleById)
                    .filter(Objects::nonNull)
                    .map(IMentionable::getAsMention)
                    .collect(Collectors.joining("\n"));

            builder.addField("Receiver Roles", receiverRoles, true);
        }
        return builder.build();
    }

    private boolean addDonor(MessageEventWrapper eventWrapper, Role role) {
        guildData.addDonorRole(eventWrapper.getGuild(), role);
        eventWrapper.reply(eventWrapper.localize("command.roles.sub.addDonor.add",
                Replacement.createMention(role))).queue();
        return true;
    }

    private boolean addReceiver(MessageEventWrapper eventWrapper, Role role) {
        guildData.addReceiverRole(eventWrapper.getGuild(), role);
        eventWrapper.reply(eventWrapper.localize("command.roles.sub.addReceiver.add",
                Replacement.createMention(role))).queue();
        return false;
    }

    private boolean removeDonor(MessageEventWrapper eventWrapper, Role role) {
        guildData.removeDonorRole(eventWrapper.getGuild(), role);
        eventWrapper.reply(eventWrapper.localize("command.roles.sub.removeDonor.remove",
                Replacement.createMention(role))).queue();
        return false;
    }

    private boolean removeReceiver(MessageEventWrapper eventWrapper, Role role) {
        guildData.removeReceiverRole(eventWrapper.getGuild(), role);
        eventWrapper.reply(eventWrapper.localize("command.roles.sub.removeReceiver.remove",
                Replacement.createMention(role))).queue();
        return false;
    }

    private void addDonor(SlashCommandEvent event, Role role) {
        guildData.addDonorRole(event.getGuild(), role);
        event.reply(loc.localize("command.roles.sub.addDonor.add", event.getGuild(),
                Replacement.createMention(role))).allowedMentions(Collections.emptyList()).queue();
    }

    private void addReceiver(SlashCommandEvent event, Role role) {
        guildData.addReceiverRole(event.getGuild(), role);
        event.reply(loc.localize("command.roles.sub.addReceiver.add", event.getGuild(),
                Replacement.createMention(role))).allowedMentions(Collections.emptyList()).queue();
    }

    private void removeDonor(SlashCommandEvent event, Role role) {
        guildData.removeDonorRole(event.getGuild(), role);
        event.reply(loc.localize("command.roles.sub.removeDonor.remove", event.getGuild(),
                Replacement.createMention(role))).allowedMentions(Collections.emptyList()).queue();
    }

    private void removeReceiver(SlashCommandEvent event, Role role) {
        guildData.removeReceiverRole(event.getGuild(), role);
        event.reply(loc.localize("command.roles.sub.removeReceiver.remove", event.getGuild(),
                Replacement.createMention(role))).allowedMentions(Collections.emptyList()).queue();
    }

    private String getBooleanMessage(Guild guild, boolean value, String whenTrue, String whenFalse) {
        return loc.localize(value ? whenTrue : whenFalse, guild);
    }
}
