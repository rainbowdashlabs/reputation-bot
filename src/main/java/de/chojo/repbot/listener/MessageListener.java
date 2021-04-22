package de.chojo.repbot.listener;

import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.RepData;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Pattern;

public class MessageListener extends ListenerAdapter {
    private final Configuration configuration;
    private final GuildData guildData;
    private final RepData repData;

    public MessageListener(DataSource dataSource, Configuration configuration) {
        guildData = new GuildData(dataSource);
        repData = new RepData(dataSource);
        this.configuration = configuration;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        var optGuildSettings = guildData.getGuildSettings(event.getGuild());
        if (optGuildSettings.isEmpty()) return;
        var guildSettings = optGuildSettings.get();

        Pattern thankwordPattern = guildSettings.getThankwordPattern();

        Message message = event.getMessage();
        String contentRaw = message.getContentRaw();
        if (message.getType() == MessageType.INLINE_REPLY) {
            if (!thankwordPattern.matcher(contentRaw).find()) return;

            Message referencedMessage = message.getReferencedMessage();
            long minutes = referencedMessage.getTimeCreated().toInstant().until(Instant.now(), ChronoUnit.MINUTES);

            if (minutes > guildSettings.getMaxMessageAge()) return;

            if (repData.logReputation(event.getGuild(), event.getAuthor(), referencedMessage.getAuthor(), referencedMessage)) {
                markMessage(message);
            }
            return;
        }

        if (thankwordPattern.matcher(contentRaw).find()) {
            List<Member> mentionedMembers = message.getMentionedMembers();
            if (mentionedMembers.size() == 1) {
                if (repData.logReputation(event.getGuild(), event.getAuthor(), mentionedMembers.get(0).getUser(), message)) {
                    markMessage(message);
                }
                return;
            }
        }
    }

    public void markMessage(Message message) {
        message.addReaction("🟩").queue();
    }
}
