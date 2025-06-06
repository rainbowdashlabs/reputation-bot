package de.chojo.repbot.commands.repadmin.handler.support;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.service.PremiumService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Refresh implements SlashHandler {
    private final PremiumService premiumService;

    public Refresh(PremiumService premiumService) {
        this.premiumService = premiumService;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext eventContext) {
        premiumService.refresh(event.getGuild());
    }
}
