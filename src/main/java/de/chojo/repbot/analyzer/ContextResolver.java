package de.chojo.repbot.analyzer;

import de.chojo.jdautil.parsing.Verifier;
import de.chojo.repbot.data.VoiceData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
     * @param message  message to determine channel, author and start time
     * @param settings setting sof the guild.
     * @return list of members which have written in this channel
     */
    @NotNull
    public Set<Member> getChannelContext(Message message, GuildSettings settings) {
        var oldestPossible = Instant.now().minus(12, ChronoUnit.HOURS);
        var history = message.getChannel().getHistoryBefore(message, 100).complete();
        var maxAge = Instant.now().minus(settings == null ? Long.MAX_VALUE : settings.maxMessageAge(), ChronoUnit.MINUTES);
        var retrievedHistory = new ArrayList<Message>();
        // add user message
        retrievedHistory.add(message);
        retrievedHistory.addAll(history.getRetrievedHistory());
        // find the oldest message in the history written by the message author.
        var oldest = retrievedHistory.stream()
                .filter(m -> Verifier.equalSnowflake(m.getAuthor(), message.getAuthor()))
                .map(m -> m.getTimeCreated().toInstant())
                .min(Instant::compareTo)
                .filter(entry -> entry.isAfter(maxAge))
                .orElse(maxAge);
        var oldestEntry = retrievedHistory.stream()
                .filter(m -> Verifier.equalSnowflake(m.getAuthor(), message.getAuthor()))
                .map(m -> m.getTimeCreated().toInstant())
                .min(Instant::compareTo)
                .filter(entry -> entry.isAfter(oldestPossible))
                .orElse(oldestPossible);


        var contextMember = retrievedHistory.stream()
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
        // add users of the last recent messages
        retrievedHistory.stream()
                .limit(settings == null ? 100 : settings.minMessages())
                .filter(m -> m.getTimeCreated().toInstant().isAfter(oldestEntry))
                .filter(m -> !m.getAuthor().isBot())
                .map(Message::getMember)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> contextMember));
        return contextMember;
    }

    public Set<Member> getVoiceContext(Message message, @Nullable GuildSettings settings) {
        Set<Member> members = new LinkedHashSet<>();
        var voiceState = message.getMember().getVoiceState();
        if (voiceState.inVoiceChannel()) members.addAll(voiceState.getChannel().getMembers());
        var pastUser = voiceData.getPastUser(message.getAuthor(), message.getGuild(),
                settings == null ? 0 : settings.maxMessageAge(), 10);
        return pastUser.stream()
                .map(id -> message.getGuild().retrieveMemberById(id).onErrorMap(throwable -> null).complete())
                .collect(Collectors.toCollection(() -> members));
    }

    public Set<Member> getCombinedContext(Message message, @Nullable GuildSettings settings) {
        Set<Member> members = new LinkedHashSet<>();
        members.addAll(getChannelContext(message, settings));
        members.addAll(getVoiceContext(message, settings));
        return members;
    }
}
