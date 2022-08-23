package de.chojo.repbot.commands.prune.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.service.GdprService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Guild implements SlashHandler {
    private final GdprService service;

    public Guild(GdprService service) {
        this.service = service;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        event.reply(context.localize("command.prune.guild.message.started")).queue();
        service.cleanupGuildUsers(event.getGuild())
                .thenAccept(amount -> event.getHook().editOriginal(context.localize("command.prune.guild.message.done",
                        Replacement.create("AMOUNT", amount))).queue());
    }
}
