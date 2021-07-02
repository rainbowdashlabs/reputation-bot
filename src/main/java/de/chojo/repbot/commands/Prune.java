package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.service.GdprService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class Prune extends SimpleCommand {
    private final GdprService gdprService;
    private final ILocalizer localizer;

    public Prune(GdprService service, ILocalizer localizer) {
        super("prune", null, "command.prune.description",
                subCommandBuilder()
                        .add("user", "command.prune.sub.user", argsBuilder()
                                .add(OptionType.USER, "user", "user", false)
                                .add(OptionType.STRING, "userid", "user id", false)
                                .build())
                        .add("guild", "command.prune.sub.guild")
                        .build(),
                Permission.ADMINISTRATOR);
        gdprService = service;
        this.localizer = localizer;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) return false;

        var cmd = context.argString(0).get();

        if ("user".equalsIgnoreCase(cmd)) {
            var user = context.argString(1);
            if (user.isEmpty()) return false;
            if (Verifier.isValidId(user.get())) {
                gdprService.cleanupGuildUser(eventWrapper.getGuild(), Long.valueOf(user.get()));
                eventWrapper.reply(localizer.localize("command.prune.sub.user.removed")).queue();
                return true;
            }
            var optUser = DiscordResolver.getUser(eventWrapper.getJda().getShardManager(), user.get());
            if (optUser.isPresent()) {
                gdprService.cleanupGuildUser(eventWrapper.getGuild(), optUser.get().getIdLong());
                eventWrapper.reply(localizer.localize("command.prune.sub.user.removed")).queue();
                return true;
            }
            eventWrapper.replyErrorAndDelete(localizer.localize("error.userNotFound", eventWrapper.getGuild()), 10);
            return true;
        }

        if ("guild".equalsIgnoreCase(cmd)) {
            eventWrapper.reply(localizer.localize("command.prune.sub.guild.started", eventWrapper.getGuild())).queue();
            gdprService.cleanupGuildUsers(eventWrapper.getGuild()).thenAccept(amount -> {
                eventWrapper.reply(localizer.localize("command.prune.sub.guild.done", eventWrapper.getGuild(),
                        Replacement.create("AMOUNT", amount))).queue();
            });
            return true;
        }
        return false;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {
        var cmd = event.getSubcommandName();

        if ("user".equalsIgnoreCase(cmd)) {
            var user = event.getOption("user");
            if (user != null) {
                gdprService.cleanupGuildUser(event.getGuild(), user.getAsUser().getIdLong());
                event.reply(localizer.localize("command.prune.sub.user.removed")).queue();
                return;
            }

            user = event.getOption("userid");
            if (user != null) {
                if (Verifier.isValidId(user.getAsString())) {
                    gdprService.cleanupGuildUser(event.getGuild(), user.getAsUser().getIdLong());
                    event.reply(localizer.localize("command.prune.sub.user.removed")).queue();
                    return;
                }
                event.reply(localizer.localize("error.userNotFound", event.getGuild())).setEphemeral(true).queue();
                return;
            }
            return;
        }

        if ("guild".equalsIgnoreCase(cmd)) {
            event.reply(localizer.localize("command.prune.sub.guild.started", event.getGuild())).queue();
            gdprService.cleanupGuildUsers(event.getGuild()).thenAccept(amount -> {
                event.getHook().editOriginal(localizer.localize("command.prune.sub.guild.done", event.getGuild(),
                        Replacement.create("AMOUNT", amount))).queue();
            });
        }
    }
}
