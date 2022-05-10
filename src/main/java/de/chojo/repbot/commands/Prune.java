package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.service.GdprService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Prune extends SimpleCommand {
    private final GdprService gdprService;

    public Prune(GdprService service) {
        super(CommandMeta.builder("prune", "command.prune.description")
                .addSubCommand("user", "command.prune.sub.user", argsBuilder()
                        .add(SimpleArgument.user("user", "command.prune.sub.user.arg.user"))
                        .add(SimpleArgument.string("userid", "command.prune.sub.user.arg.userId")))
                .addSubCommand("guild", "command.prune.sub.guild")
                .withPermission());
        gdprService = service;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var cmd = event.getSubcommandName();

        if ("user".equalsIgnoreCase(cmd)) {
            var user = event.getOption("user");
            if (user != null) {
                gdprService.cleanupGuildUser(event.getGuild(), user.getAsUser().getIdLong());
                event.reply(context.localize("command.prune.sub.user.removed")).queue();
                return;
            }

            user = event.getOption("userid");
            if (user != null) {
                var idRaw = Verifier.getIdRaw(user.getAsString());
                if (idRaw.isPresent()) {
                    gdprService.cleanupGuildUser(event.getGuild(), Long.valueOf(idRaw.get()));
                    event.reply(context.localize("command.prune.sub.user.removed")).queue();
                    return;
                }
                event.reply(context.localize("error.userNotFound")).setEphemeral(true).queue();
                return;
            }
            return;
        }

        if ("guild".equalsIgnoreCase(cmd)) {
            event.reply(context.localize("command.prune.sub.guild.started")).queue();
            gdprService.cleanupGuildUsers(event.getGuild())
                    .thenAccept(amount -> event.getHook().editOriginal(context.localize("command.prune.sub.guild.done",
                            Replacement.create("AMOUNT", amount))).queue());
        }
    }
}
