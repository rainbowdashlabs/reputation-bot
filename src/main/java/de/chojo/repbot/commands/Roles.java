package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import de.chojo.repbot.service.RoleAccessException;
import de.chojo.repbot.service.RoleAssigner;
import de.chojo.repbot.util.Permissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.sharding.ShardManager;

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
    private final ShardManager shardManager;
    private final Set<Long> running = new HashSet<>();

    public Roles(DataSource dataSource, ILocalizer loc, RoleAssigner roleAssigner, ShardManager shardManager) {
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
                        .add("stackroles", "command.roles.sub.stackRoles", argsBuilder()
                                .add(OptionType.BOOLEAN, "stack", "stack")
                                .build())
                        .build(),
                Permission.MANAGE_SERVER);
        guildData = new GuildData(dataSource);
        this.loc = loc;
        this.roleAssigner = roleAssigner;
        this.shardManager = shardManager;
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
        var state = event.getOption("stack").getAsBoolean();

        if (guildData.setRoleStacking(event.getGuild(), state)) {
            event.reply(getBooleanMessage(event.getGuild(), state,
                    "command.roles.sub.stackRoles.stacked", "command.roles.sub.stackRoles.notStacked")).queue();
        }
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

            Permissions.buildGuildPriviledges(guildData, event.getGuild());
        }
    }

    private String getManagerRoleMessage(Guild guild, GuildSettings settings) {
        if (settings.generalSettings().managerRole().isPresent()) {
            var roleById = guild.getRoleById(settings.generalSettings().managerRole().get());
            if (roleById != null) {
                return loc.localize("command.roles.sub.managerRole.current", guild,
                        Replacement.createMention(roleById));
            }
        }
        return loc.localize("command.roles.sub.managerRole.noRole", guild);
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
