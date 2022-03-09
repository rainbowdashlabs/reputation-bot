package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.service.GdprService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
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
                var idRaw = Verifier.getIdRaw(user.getAsString());
                if (idRaw.isPresent()) {
                    gdprService.cleanupGuildUser(event.getGuild(), Long.valueOf(idRaw.get()));
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
