package de.chojo.repbot.commands.abuseprotection.handler.cooldown;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class GetCooldown implements SlashHandler {
    private final GuildRepository guildRepository;

    public GetCooldown(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guild = guildRepository.guild(event.getGuild());
        var abuseSettings = guild.settings().abuseProtection();
        String message="";
        if(abuseSettings.cooldown() == 0){
            message = context.localize("command.abuseprotection.cooldown.get.message.disabled");
        }else if(abuseSettings.cooldown() < 0){
             message = context.localize("command.abuseprotection.cooldown.once.message.set");
        }else {
            message = context.localize("command.abuseprotection.cooldown.get.message.get",
                    Replacement.create("MINUTES", abuseSettings.cooldown()));
        }
        message = message + "\n$words.direction$: $%s$".formatted(abuseSettings.cooldownDirection().localCode());
        event.reply(context.localize(message)).setEphemeral(true).queue();
    }
}
