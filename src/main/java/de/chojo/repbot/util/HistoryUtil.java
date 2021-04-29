package de.chojo.repbot.util;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class HistoryUtil {
    /**
     * Get members which have written in the channel of the message.
     * <p>
     * Only members which have written in the last 100 messages which are not older than the max history and are not
     * send before the first message of the message author are returned
     *
     * @param message       message to determine channel, author and start time
     * @param maxHistoryAge max age of analyzed messages. may be overriden by first message of author if this is more
     *                      recent.
     *
     * @return list of members which have written in this channel
     */
    @NotNull
    public static Set<Member> getRecentMembers(Message message, int maxHistoryAge) {
        Set<Member> targets;
        var history = message.getChannel().getHistoryBefore(message, 50).complete();
        var oldest = Instant.now().minus(maxHistoryAge, ChronoUnit.MINUTES);
        List<Message> retrievedHistory = history.getRetrievedHistory();
        // add user message
        retrievedHistory.add(message);
        var first = retrievedHistory.stream().map(m -> m.getTimeCreated().toInstant()).min(Instant::compareTo);

        if (first.isPresent()) {
            oldest = first.get().isAfter(oldest) ? first.get() : oldest;
        }

        var finalOldest = oldest;
        targets = retrievedHistory.stream()
                // filter message for only recent messages and after the first message of the user.
                .filter(m -> m.getTimeCreated().toInstant().isAfter(finalOldest))
                .map(Message::getAuthor)
                .distinct()
                .filter(u -> !u.isBot())
                .map(u -> {
                    try {
                        return message.getGuild().retrieveMemberById(u.getIdLong()).complete();
                    } catch (RuntimeException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        return targets;
    }

}
