package de.chojo.repbot.commands.abuseprotection.handler.cooldown;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.settings.sub.CooldownDirection;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SetCooldownDirection implements SlashHandler {
    private final GuildRepository guildRepository;

    public SetCooldownDirection(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guild = guildRepository.guild(event.getGuild());
        var abuseSettings = guild.settings().abuseProtection();
        var cooldown = CooldownDirection.valueOf(event.getOption("direction").getAsString());

        event.reply(context.localize("command.abuseprotection.cooldown.direction.message.set",
                Replacement.create("DIRECTION", abuseSettings.cooldownDirection(cooldown)))).queue();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        event.replyChoices(Completion.complete(event.getFocusedOption().getValue(), CooldownDirection.class)).queue();
    }
}
