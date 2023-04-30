package de.chojo.repbot.analyzer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.parsing.WeightedEntry;
import de.chojo.repbot.analyzer.results.AnalyzerResult;
import de.chojo.repbot.analyzer.results.empty.EmptyResultReason;
import de.chojo.repbot.analyzer.results.match.fuzzy.MemberMatch;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.util.LogNotify;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class MessageAnalyzer {
    private static final int LOOKAROUND = 6;
    private static final Logger log = getLogger(MessageAnalyzer.class);
    private static final Object PLACEHOLDER = new Object();
    private final ContextResolver contextResolver;
    private final Cache<Long, AnalyzerResult> resultCache = CacheBuilder.newBuilder()
                                                                        .expireAfterWrite(10, TimeUnit.MINUTES)
                                                                        .maximumSize(100000)
                                                                        .build();
    private final Cache<Long, Object> rejectionCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();
    private final Configuration configuration;
    private final Metrics metrics;
    private final Guilds guilds;

    public MessageAnalyzer(ContextResolver resolver, Configuration configuration, Metrics metrics, Guilds guilds) {
        contextResolver = resolver;
        this.configuration = configuration;
        this.metrics = metrics;
        this.guilds = guilds;
    }

    /**
     * Analyze a message.
     *
     * @param pattern      regex pattern for targetwords
     * @param message      message to analyze
     * @param settings     settings of the guild
     * @param limitTargets true if targets should be limited to users which have written in the channel in the
     *                     maxHistoryAge
     * @param limit        limit for returned matches in the analyzer result
     * @return analyzer results
     */
    public AnalyzerResult processMessage(Pattern pattern, @NotNull Message message, Settings settings, boolean limitTargets, int limit) {
        if (rejectionCache.getIfPresent(settings.guildId()) != null) {
            return AnalyzerResult.empty(EmptyResultReason.INTERNAL_ERROR);
        }
        var analyzer = guilds.guild(message.getGuild()).reputation().analyzer();
        try {
            return analyzer.log(message, resultCache.get(message.getIdLong(), () -> analyzeWithTimeout(pattern, message, settings, limitTargets, limit)));
        } catch (ExecutionException e) {
            if (e.getCause() instanceof TimeoutException) {
                log.warn(LogNotify.NOTIFY_ADMIN, "Timeout when analyzing message using pattern {} for guild {}", pattern.pattern(), settings.guildId());
                rejectionCache.put(settings.guildId(), PLACEHOLDER);
            } else {
                log.error("Could not compute analyzer result", e);
            }
        }
        return analyzer.log(message, AnalyzerResult.empty(EmptyResultReason.INTERNAL_ERROR));
    }

    private AnalyzerResult analyzeWithTimeout(Pattern pattern, Message message, @Nullable Settings settings, boolean limitTargets, int limit) {
        return CompletableFuture.supplyAsync(() -> analyze(pattern, message, settings, limitTargets, limit))
                .orTimeout(1L, TimeUnit.SECONDS)
                .join();
    }

    private AnalyzerResult analyze(Pattern pattern, Message message, @Nullable Settings settings, boolean limitTargets, int limit) {
        metrics.messages().countMessage();
        if (pattern.pattern().isBlank()) return AnalyzerResult.empty(EmptyResultReason.NO_PATTERN);
        var contentRaw = message.getContentRaw().toLowerCase();

        var matcher = pattern.matcher(contentRaw);
        if (!matcher.find()) return AnalyzerResult.empty(EmptyResultReason.NO_MATCH);

        var match = matcher.group("match");

        if (message.getType() == MessageType.INLINE_REPLY) {

            var referencedMessage = message.getReferencedMessage();
            if (referencedMessage == null) return AnalyzerResult.empty(match, EmptyResultReason.REFERENCE_MESSAGE_NOT_FOUND);

            Member user;

            try {
                user = message.getGuild().retrieveMemberById(referencedMessage.getAuthor().getIdLong()).complete();
            } catch (RuntimeException e) {
                log.debug("Could not retrieve member. Probably not on guild anymore.");
                return AnalyzerResult.empty(match, EmptyResultReason.TARGET_NOT_ON_GUILD);
            }

            return AnalyzerResult.answer(match, message.getMember(), user, referencedMessage);
        }

        var context = MessageContext.byMessage(message);
        if (limitTargets) {
            context = contextResolver.getCombinedContext(message, settings);
        }

        var mentionedMembers = message.getMentions().getUsers();
        if (!mentionedMembers.isEmpty()) {
            if (mentionedMembers.size() > limit) {
                return resolveMessage(match, message, pattern, context, limitTargets, limit);
            }

            List<Member> members = new ArrayList<>();

            for (var mentionedMember : mentionedMembers) {
                try {
                    members.add(message.getGuild().retrieveMemberById(mentionedMember.getIdLong()).complete());
                } catch (RuntimeException e) {
                    log.debug("Could not retrieve member. Probably not on guild anymore.");
                }
            }

            if (members.isEmpty()) return AnalyzerResult.empty(match, EmptyResultReason.TARGET_NOT_ON_GUILD);

            Member author;
            try {
                author = message.getGuild().retrieveMember(message.getAuthor()).complete();
            } catch (RuntimeException e) {
                return AnalyzerResult.empty(EmptyResultReason.TARGET_NOT_ON_GUILD);
            }

            return AnalyzerResult.mention(match, author, members);
        }
        return resolveMessage(match, message, pattern, context, limitTargets, limit);
    }


    private AnalyzerResult resolveMessage(String matchPattern, Message message, Pattern thankPattern, MessageContext targets, boolean limitTargets, int limit) {
        var contentRaw = message.getContentRaw();

        var words = new ArrayList<>(List.of(contentRaw.split("\\s")));
        words.removeIf(String::isBlank);

        List<Integer> thankWordIndices = new ArrayList<>();
        var index = 0;
        for (var word : words) {
            if (thankPattern.matcher(word).find()) {
                thankWordIndices.add(index);
            }
        }
        List<WeightedEntry<Member>> users = new ArrayList<>();

        List<MemberMatch> memberMatches = new ArrayList<>();

        for (var thankwordindex : thankWordIndices) {
            List<String> resolve = new ArrayList<>();

            if (thankwordindex != 0) {
                resolve.addAll(words.subList(Math.max(0, thankwordindex - LOOKAROUND), thankwordindex));
            }
            if (thankwordindex != words.size() - 1) {
                resolve.addAll(words.subList(Math.min(thankwordindex + 1, words.size() - 1), Math.min(words.size(), thankwordindex + LOOKAROUND + 1)));
            }

            for (var word : resolve) {
                List<WeightedEntry<Member>> weightedMembers;
                if (limitTargets) {
                    weightedMembers = DiscordResolver.fuzzyGuildTargetSearch(word, targets.members());
                } else {
                    weightedMembers = DiscordResolver.fuzzyGuildUserSearch(message.getGuild(), word);
                }
                if (weightedMembers.isEmpty()) continue;
                for (var match : weightedMembers) {
                    var member = match.getReference();
                    memberMatches.add(new MemberMatch(word, member.getUser().getAsTag(),
                            member.getEffectiveName(), match.getWeight()));
                }
                users.addAll(weightedMembers);
            }
        }

        memberMatches = memberMatches.stream()
                                     .filter(e -> e.score() >= configuration.analyzerSettings().minFuzzyScore())
                                     .toList();

        var members = users.stream()
                           .filter(e -> e.getWeight() >= configuration.analyzerSettings().minFuzzyScore())
                           .distinct()
                           .sorted()
                           .limit(limit)
                           .collect(Collectors.toList());
        if (members.isEmpty()) return AnalyzerResult.empty(matchPattern, EmptyResultReason.INSUFFICIENT_SCORE);

        var thankwords = thankWordIndices.stream().map(words::get).collect(Collectors.toList());

        Member author;
        try {
            author = message.getGuild().retrieveMember(message.getAuthor()).complete();
        } catch (RuntimeException e) {
            return AnalyzerResult.empty(EmptyResultReason.TARGET_NOT_ON_GUILD);
        }

        return AnalyzerResult.fuzzy(matchPattern, thankwords, memberMatches, author, members);
    }
}
