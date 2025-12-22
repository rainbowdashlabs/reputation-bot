package de.chojo.repbot.commands.rep.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.analyzer.results.match.ThankType;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.service.reputation.ReputationService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class Give implements SlashHandler {
    private final GuildRepository guilds;
    private final ReputationService reputationService;

    public Give(GuildRepository guilds, ReputationService reputationService) {
        this.guilds = guilds;
        this.reputationService = reputationService;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        RepGuild guild = guilds.guild(event.getGuild());

        if (!guild.settings().reputation().isCommandActive()) {
            event.reply("not active").setEphemeral(true).queue();
            return;
        }

        event.getIdLong()

        reputationService.submitReputation(event.getGuild(), event.getMember(), event.getOption("user", OptionMapping::getAsMember), null, null, ThankType.ANSWER);
    }
}
