package de.chojo.repbot.commands.bot.handler.log;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.ValueParser;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.commands.log.handler.LogFormatter;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Analyzer implements SlashHandler {
    private final Guilds guilds;

    public Analyzer(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guild_id = ValueParser.parseLong(event.getOption("guild_id").getAsString());
        var optMessageId = ValueParser.parseLong(event.getOption("message_id").getAsString());
        if (optMessageId.isEmpty()) {
            event.reply(context.localize("error.invalidMessage")).setEphemeral(true).queue();
            return;
        }
        var reputation = guilds.byId(guild_id.get()).reputation();
        var resultEntry = reputation.analyzer()
                                    .get(optMessageId.get());

        if (resultEntry.isEmpty()) {
            event.reply(context.localize("command.log.analyzer.notanalyzed")).setEphemeral(true).queue();
            return;
        }

        var embed = resultEntry.get().embed(event.getGuild(), context);

        var reputationLogEntries = reputation.log().messageLog(optMessageId.get(), 10);

        var entries = LogFormatter.mapMessageLogEntry(context, reputationLogEntries);

        var builder = new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.log.message.message.log", Replacement.create("ID", optMessageId.get()));

        LogFormatter.buildFields(entries, builder);

        embed.add(builder.build());

        event.replyEmbeds(embed).setEphemeral(true).queue();
    }
}
