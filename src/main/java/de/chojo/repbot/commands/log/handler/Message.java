package de.chojo.repbot.commands.log.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.ValueParser;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static de.chojo.repbot.commands.log.handler.LogFormatter.buildFields;
import static de.chojo.repbot.commands.log.handler.LogFormatter.mapMessageLogEntry;

public class Message implements SlashHandler {
    private final Guilds guilds;

    public Message(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        event.getOption("messageid");
        var optMessageId = ValueParser.parseLong(event.getOption("messageid").getAsString());
        if (optMessageId.isEmpty()) {
            event.reply(context.localize("error.invalidMessage")).setEphemeral(true).queue();
            return;
        }

        event.replyEmbeds(getMessageLog(context, event.getGuild(), event.getOption("message_id").getAsLong())).setEphemeral(true).queue();
    }

    private MessageEmbed getMessageLog(EventContext context, Guild guild, long messageId) {
        var messageLog = guilds.guild(guild).reputation().log().messageLog(messageId, 50);

        var log = mapMessageLogEntry(context, messageLog);

        var builder = new LocalizedEmbedBuilder(context.guildLocalizer())
                .setAuthor("command.log.message.message.log", Replacement.create("ID", messageId));
        buildFields(log, builder);
        return builder.build();
    }
}
