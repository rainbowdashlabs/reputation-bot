package de.chojo.repbot.util;

import de.chojo.jdautil.parsing.Verifier;
import de.chojo.repbot.data.VoiceData;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ContextResolver {
    private final VoiceData voiceData;

    public ContextResolver(DataSource dataSource) {
        voiceData = new VoiceData(dataSource);
    }

    /**
     * Get members which have written in the channel of the message.
     * <p>
     * Only members which have written in the last 100 messages which are not older than the max history and are not
     * send before the first message of the message author are returned
     *
     * @param message       message to determine channel, author and start time
     * @param maxHistoryAge max age of analyzed messages. may be overriden by first message of author if this is more
     *                      recent.
     * @return list of members which have written in this channel
     */
    @NotNull
    public Set<Member> getChannelContext(Message message, int maxHistoryAge) {
        var history = message.getChannel().getHistoryBefore(message, 50).complete();
        var maxAge = Instant.now().minus(maxHistoryAge, ChronoUnit.MINUTES);
        var retrievedHistory = new ArrayList<>(history.getRetrievedHistory());
        // add user message
        retrievedHistory.add(message);
        // find the oldest message in the history written by the message author.
        var oldest = retrievedHistory.stream()
                .filter(m -> Verifier.equalSnowflake(m.getAuthor(), message.getAuthor()))
                .map(m -> m.getTimeCreated().toInstant())
                .min(Instant::compareTo).filter(entry -> entry.isAfter(maxAge)).orElse(maxAge);

        return retrievedHistory.stream()
                // filter message for only recent messages and after the first message of the user.
                .filter(m -> m.getTimeCreated().toInstant().isAfter(oldest))
                .map(Message::getAuthor)
                .distinct()
                .filter(u -> !u.isBot())
                .filter(u -> !Verifier.equalSnowflake(message.getAuthor(), u))
                .map(u -> {
                    try {
                        return message.getGuild().retrieveMemberById(u.getIdLong()).complete();
                    } catch (RuntimeException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public Set<Member> getVoiceContext(Message message, int maxAge) {
        var pastUser = voiceData.getPastUser(message.getAuthor(), message.getGuild(), maxAge, 10);
        var collect = pastUser.stream()
                .map(id -> message.getGuild().retrieveMemberById(id).onErrorMap(throwable -> null).complete())
                .collect(Collectors.toCollection(HashSet::new));
        var voiceState = message.getMember().getVoiceState();
        if (voiceState.inVoiceChannel()) collect.addAll(voiceState.getChannel().getMembers());
        return collect;
    }

    public Set<Member> getCombinedContext(Message message, int maxAge) {
        Set<Member> members = new HashSet<>();
        members.addAll(getChannelContext(message, maxAge));
        members.addAll(getVoiceContext(message, maxAge));
        return members;
    }
}
