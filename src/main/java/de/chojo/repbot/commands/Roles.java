package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.service.RoleAccessException;
import de.chojo.repbot.service.RoleAssigner;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static de.chojo.repbot.util.Guilds.prettyName;
import static org.slf4j.LoggerFactory.getLogger;

public class Roles extends SimpleCommand {
    private static final Logger log = getLogger(Roles.class);
    private final Guilds guilds;
    private final RoleAssigner roleAssigner;
    private final Set<Long> running = new HashSet<>();

    public Roles(Guilds guilds, RoleAssigner roleAssigner) {
        super(CommandMeta.builder("roles", "command.roles.description")
                .addSubCommand("add", "command.roles.sub.add", argsBuilder()
                        .add(SimpleArgument.role("role", "command.roles.sub.add.arg.role").asRequired())
                        .add(SimpleArgument.integer("reputation", "command.roles.sub.add.arg.reputation").asRequired()))
                .addSubCommand("remove", "command.roles.sub.remove", argsBuilder()
                        .add(SimpleArgument.role("role", "command.roles.sub.remove.arg.role").asRequired()))
                .addSubCommand("adddonor", "command.roles.sub.addDonor", argsBuilder()
                        .add(SimpleArgument.role("role", "command.roles.sub.addDonor.arg.role").asRequired()))
                .addSubCommand("addreceiver", "command.roles.sub.addReceiver", argsBuilder()
                        .add(SimpleArgument.role("role", "command.roles.sub.addReceiver.arg.role").asRequired()))
                .addSubCommand("removedonor", "command.roles.sub.removeDonor", argsBuilder()
                        .add(SimpleArgument.role("role", "command.roles.sub.removeDonor.arg.role").asRequired()))
                .addSubCommand("removereceiver", "command.roles.sub.removeReceiver", argsBuilder()
                        .add(SimpleArgument.role("role", "command.roles.sub.removeReceiver.arg.role").asRequired()))
                .addSubCommand("refresh", "command.roles.sub.refresh")
                .addSubCommand("list", "command.roles.sub.list")
                .addSubCommand("stackroles", "command.roles.sub.stackRoles", argsBuilder()
                        .add(SimpleArgument.bool("stack", "command.roles.sub.stackRoles.arg.stack")))
                .withPermission());
        this.guilds = guilds;
        this.roleAssigner = roleAssigner;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var subCmd = event.getSubcommandName();
        if ("list".equalsIgnoreCase(subCmd)) {
            list(event, context);
        }

        if ("add".equalsIgnoreCase(subCmd)) {
            add(event, context);
        }

        if ("remove".equalsIgnoreCase(subCmd)) {
            remove(event, context);
        }

        if ("addDonor".equalsIgnoreCase(subCmd)) {
            addDonor(event, context, event.getOption("role").getAsRole());
        }

        if ("addReceiver".equalsIgnoreCase(subCmd)) {
            addReceiver(event, context, event.getOption("role").getAsRole());
        }

        if ("removeDonor".equalsIgnoreCase(subCmd)) {
            removeDonor(event, context, event.getOption("role").getAsRole());
        }

        if ("removeReceiver".equalsIgnoreCase(subCmd)) {
            removeReceiver(event, context, event.getOption("role").getAsRole());
        }

        if ("refresh".equalsIgnoreCase(subCmd)) {
            refresh(event, context);
        }

        if ("stackRoles".equalsIgnoreCase(subCmd)) {
            stackRoles(event, context);
        }
    }

    private void refresh(SlashCommandInteractionEvent event, SlashCommandContext context) {
        if (running.contains(event.getGuild().getIdLong())) {
            event.reply(context.localize("command.roles.sub.refresh.running")).queue();
            return;
        }

        running.add(event.getGuild().getIdLong());

        event.reply(context.localize("command.roles.sub.refresh.started")).queue();
        var start = Instant.now();
        roleAssigner
                .updateBatch(event.getGuild())
                .onSuccess(res -> {
                    var duration = DurationFormatUtils.formatDuration(start.until(Instant.now(), ChronoUnit.MILLIS), "mm:ss");
                    log.info("Update of roles on {} took {}.", prettyName(event.getGuild()), duration);
                    if (event.getHook().isExpired()) {
                        log.debug("Interaction hook is expired. Using fallback message.");
                        event.getChannel()
                                .sendMessage(context.localize("command.roles.sub.refresh.finished"))
                                .queue();
                        return;
                    }
                    event.getHook()
                            .editOriginal(context.localize("command.roles.sub.refresh.finished"))
                            .queue();
                    running.remove(event.getGuild().getIdLong());
                }).onError(err -> {
                    log.warn("Update of role failed on guild {}", prettyName(event.getGuild()), err);
                    if (err instanceof RoleAccessException roleException) {
                        event.getHook()
                                .editOriginal(context.localize("error.roleAccess",
                                        Replacement.createMention("ROLE", roleException.role())))
                                .queue();
                    }
                    running.remove(event.getGuild().getIdLong());
                });
    }

    private void stackRoles(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var settings = guilds.guild(event.getGuild()).settings();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(context, settings.general().isStackRoles(),
                    "command.roles.sub.stackRoles.stacked", "command.roles.sub.stackRoles.notStacked")).queue();
            return;
        }
        var state = event.getOption("stack").getAsBoolean();

        if (settings.general().stackRoles(state)) {
            event.reply(getBooleanMessage(context, state,
                    "command.roles.sub.stackRoles.stacked", "command.roles.sub.stackRoles.notStacked")).queue();
        }
    }

    private void remove(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var ranks = guilds.guild(event.getGuild()).settings().ranks();
        var role = event.getOption("role").getAsRole();

        if (ranks.remove(role)) {
            event.reply(context.localize("command.roles.sub.remove.removed",
                    Replacement.createMention("ROLE", role))).allowedMentions(Collections.emptyList()).queue();
            return;
        }
        event.reply(context.localize("command.roles.sub.remove.notARepRole")).setEphemeral(true).queue();
    }

    private void add(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var role = event.getOption("role").getAsRole();
        var reputation = event.getOption("reputation").getAsLong();
        if (!event.getGuild().getSelfMember().canInteract(role)) {
            event.reply(context.localize("error.roleAccess",
                    Replacement.createMention(role))).setEphemeral(true).queue();
            return;
        }

        var ranks = guilds.guild(event.getGuild()).settings().ranks();
        ranks.add(role, reputation);
        event.reply(context.localize("command.roles.sub.add.added",
                        Replacement.createMention("ROLE", role), Replacement.create("POINTS", reputation)))
                .allowedMentions(Collections.emptyList()).queue();

    }

    private void list(SlashCommandInteractionEvent event, SlashCommandContext context) {
        event.replyEmbeds(getRoleList(context, event.getGuild())).allowedMentions(Collections.emptyList()).queue();
    }

    private MessageEmbed getRoleList(SlashCommandContext context, Guild guild) {
        var settings = guilds.guild(guild).settings();
        var ranks = settings.ranks();

        var reputationRoles = ranks.ranks()
                .stream()
                .sorted(Comparator.reverseOrder())
                .filter(role -> role.getRole(guild) != null)
                .map(role -> role.reputation() + " ➜ " + role.getRole(guild).getAsMention())
                .collect(Collectors.joining("\n"));

        var builder = new LocalizedEmbedBuilder(context.localizer())
                .setTitle("Role Info");

        builder.addField("Reputation Roles", reputationRoles, true);

        var thankSettings = settings.thanking();

        if (!thankSettings.donorRoles().roles().isEmpty()) {
            var donorRoles = thankSettings.donorRoles()
                    .roles()
                    .stream()
                    .map(IMentionable::getAsMention)
                    .collect(Collectors.joining("\n"));

            builder.addField("Donor Roles", donorRoles, true);
        }
        if (!thankSettings.receiverRoles().roles().isEmpty()) {
            var receiverRoles = thankSettings.receiverRoles()
                    .roles()
                    .stream()
                    .map(IMentionable::getAsMention)
                    .collect(Collectors.joining("\n"));

            builder.addField("Receiver Roles", receiverRoles, true);
        }
        return builder.build();
    }

    private void addDonor(SlashCommandInteractionEvent event, SlashCommandContext context, Role role) {
        guilds.guild(event.getGuild()).settings().thanking().donorRoles().add(role);
        event.reply(context.localize("command.roles.sub.addDonor.add",
                Replacement.createMention(role))).allowedMentions(Collections.emptyList()).queue();
    }

    private void addReceiver(SlashCommandInteractionEvent event, SlashCommandContext context, Role role) {
        guilds.guild(event.getGuild()).settings().thanking().receiverRoles().add(role);
        event.reply(context.localize("command.roles.sub.addReceiver.add",
                Replacement.createMention(role))).allowedMentions(Collections.emptyList()).queue();
    }

    private void removeDonor(SlashCommandInteractionEvent event, SlashCommandContext context, Role role) {
        guilds.guild(event.getGuild()).settings().thanking().donorRoles().remove(role);
        event.reply(context.localize("command.roles.sub.removeDonor.remove",
                Replacement.createMention(role))).allowedMentions(Collections.emptyList()).queue();
    }

    private void removeReceiver(SlashCommandInteractionEvent event, SlashCommandContext context, Role role) {
        guilds.guild(event.getGuild()).settings().thanking().receiverRoles().remove(role);
        event.reply(context.localize("command.roles.sub.removeReceiver.remove",
                Replacement.createMention(role))).allowedMentions(Collections.emptyList()).queue();
    }

    private String getBooleanMessage(SlashCommandContext context, boolean value, String whenTrue, String whenFalse) {
        return context.localize(value ? whenTrue : whenFalse);
    }

    public boolean refreshActive(Guild guild) {
        return running.contains(guild.getIdLong());
    }
}
