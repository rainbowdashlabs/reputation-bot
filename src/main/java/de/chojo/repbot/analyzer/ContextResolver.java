package de.chojo.repbot.analyzer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.data.VoiceData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

public class ContextResolver {
    private static final Logger log = getLogger(ContextResolver.class);
    private final VoiceData voiceData;
    private final Configuration configuration;
    private final Cache<Long, Set<Long>> messageContextCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.SECONDS)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build();

    private final Cache<Long, Set<Long>> voiceContextCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.SECONDS)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build();

    public ContextResolver(DataSource dataSource, Configuration configuration) {
        voiceData = new VoiceData(dataSource);
        this.configuration = configuration;
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
        try {
            return messageContextCache.get(message.getIdLong(), () -> retrieveChannelContext(message, settings))
                    .stream()
                    .map(id -> message.getGuild().retrieveMemberById(id).onErrorMap(e -> null).complete())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (ExecutionException e) {
            log.error("Could not conpute channel context.", e);
        }
        return Collections.emptySet();
    }

    private Set<Long> retrieveChannelContext(Message message, GuildSettings settings) {
        var history = message.getChannel().getHistoryBefore(message, configuration.analyzerSettings().historySize()).complete();
        List<Message> retrievedHistory = new ArrayList<>();
        // add user message
        retrievedHistory.add(message);
        retrievedHistory.addAll(history.getRetrievedHistory());
        retrievedHistory = retrievedHistory.stream()
                // Remove all bot messages. we wont need them anyway.
                .filter(m -> !m.getAuthor().isBot())
                .collect(Collectors.toList());

        var result = new LinkedHashSet<Long>();

        result.addAll(getRecentAuthors(retrievedHistory, message.getAuthor(), message.getGuild(), settings));
        result.addAll(getLatestAuthors(retrievedHistory, message.getAuthor(), message.getGuild(), settings));
        return result;
    }

    private Set<Long> getLatestAuthors(List<Message> messages, User author, Guild guild, GuildSettings settings) {
        var maxAge = Instant.now().minus(configuration.analyzerSettings().latestMaxHours(), ChronoUnit.HOURS);

        var oldest = findOldestMessageByAuthor(author, messages, maxAge);

        // add users of the last recent messages
        return getMemberAfter(messages.stream().limit(settings == null ? configuration.analyzerSettings().historySize() : settings.minMessages()), guild, oldest);
    }

    private Set<Long> getRecentAuthors(List<Message> messages, User author, Guild guild, GuildSettings settings) {
        var maxAge = Instant.now().minus(settings == null ? Long.MAX_VALUE : settings.maxMessageAge(), ChronoUnit.MINUTES);
        // find the oldest message in the history written by the message author which is newer than the max message age.
        var oldest = findOldestMessageByAuthor(author, messages, maxAge);

        return getMemberAfter(messages.stream(), guild, oldest);
    }

    private Set<Long> getMemberAfter(Stream<Message> messages, Guild guild, Instant oldest) {
        // filter message for only recent messages and after the first message of the user.
        return messages.filter(m -> m.getTimeCreated().toInstant().isAfter(oldest))
                .map(Message::getAuthor)
                .distinct()
                .map(u -> guild.retrieveMemberById(u.getIdLong()).onErrorMap(m -> null).complete())
                .filter(Objects::nonNull)
                .map(Member::getIdLong)
                .collect(Collectors.toSet());

    }

    private Instant findOldestMessageByAuthor(User author, List<Message> messages, Instant maxAge) {
        return messages.stream()
                .filter(m -> Verifier.equalSnowflake(m.getAuthor(), author))
                .map(m -> m.getTimeCreated().toInstant())
                .filter(entry -> entry.isAfter(maxAge))
                .min(Instant::compareTo)
                .orElse(maxAge);
    }

    public Set<Member> getVoiceContext(Message message, @Nullable GuildSettings settings) {
        try {
            return voiceContextCache.get(message.getIdLong(), () -> retrieveVoiceContext(message, settings))
                    .stream()
                    .map(id -> message.getGuild().retrieveMemberById(id).onErrorMap(e -> null).complete())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (ExecutionException e) {
            log.error("Could not compute voice cache", e);
        }
        return Collections.emptySet();
    }

    private Set<Long> retrieveVoiceContext(Message message, @Nullable GuildSettings settings) {
        Set<Long> members = new LinkedHashSet<>();
        if (message.getMember() == null) return Collections.emptySet();
        var voiceState = message.getMember().getVoiceState();
        if (voiceState == null) return Collections.emptySet();
        if (voiceState.inVoiceChannel()) {
            var voice = voiceState.getChannel().getMembers()
                    .stream()
                    .map(Member::getIdLong)
                    .collect(Collectors.toSet());
            members.addAll(voice);
        }
        var pastUser = voiceData.getPastUser(message.getAuthor(), message.getGuild(),
                settings == null ? 0 : settings.maxMessageAge(), configuration.analyzerSettings().voiceMembers());
        return pastUser.stream()
                .map(id -> message.getGuild().retrieveMemberById(id).onErrorMap(throwable -> null).complete())
                .map(Member::getIdLong)
                .collect(Collectors.toCollection(() -> members));
    }

    public Set<Member> getCombinedContext(Message message, @Nullable GuildSettings settings) {
        Set<Member> members = new LinkedHashSet<>();
        members.addAll(getChannelContext(message, settings));
        members.addAll(getVoiceContext(message, settings));
        return members;
    }
}
