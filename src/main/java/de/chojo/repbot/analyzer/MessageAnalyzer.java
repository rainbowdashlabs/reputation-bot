package de.chojo.repbot.analyzer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.parsing.WeightedEntry;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.statistic.Statistic;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class MessageAnalyzer {
    private static final int LOOKAROUND = 6;
    private static final Logger log = getLogger(MessageAnalyzer.class);
    private final ContextResolver contextResolver;
    private final Cache<Long, AnalyzerResult> resultCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(100000)
            .build();
    private final Configuration configuration;
    private final Statistic statistic;

    public MessageAnalyzer(ContextResolver resolver, Configuration configuration, Statistic statistic) {
        contextResolver = resolver;
        this.configuration = configuration;
        this.statistic = statistic;
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
        try {
            return resultCache.get(message.getIdLong(), () -> analyze(pattern, message, settings, limitTargets, limit));
        } catch (ExecutionException e) {
            log.error("Could not compute anaylzer result", e);
        }
        return AnalyzerResult.noMatch();
    }

    private AnalyzerResult analyze(Pattern pattern, Message message, @Nullable Settings settings, boolean limitTargets, int limit) {
        statistic.messageAnalyzed(message.getJDA());
        if (pattern.pattern().isBlank()) return AnalyzerResult.noMatch();
        var contentRaw = message.getContentRaw();

        if (!pattern.matcher(contentRaw).find()) return AnalyzerResult.noMatch();
        if (message.getType() == MessageType.INLINE_REPLY) {

            var referencedMessage = message.getReferencedMessage();
            if (referencedMessage == null) return AnalyzerResult.noMatch();

            Member user;

            try {
                user = message.getGuild().retrieveMemberById(referencedMessage.getAuthor().getIdLong()).complete();
            } catch (RuntimeException e) {
                log.debug("Could not retrieve member. Probably not on guild anymore.");
                return AnalyzerResult.noMatch();
            }

            return AnalyzerResult.answer(message.getMember(), user, referencedMessage);
        }

        var context = MessageContext.byMessage(message);
        if (limitTargets) {
            context = contextResolver.getCombinedContext(message, settings);
        }

        var mentionedMembers = message.getMentionedUsers();
        if (!mentionedMembers.isEmpty()) {
            if (mentionedMembers.size() > limit) {
                return resolveMessage(message, pattern, context, limitTargets, limit);
            }

            List<Member> members = new ArrayList<>();

            for (var mentionedMember : mentionedMembers) {
                try {
                    members.add(message.getGuild().retrieveMemberById(mentionedMember.getIdLong()).complete());
                } catch (RuntimeException e) {
                    log.debug("Could not retrieve member. Probably not on guild anymore.");
                }
            }

            if (members.isEmpty()) return AnalyzerResult.noMatch();

            return AnalyzerResult.mention(message.getMember(), members);
        }
        return resolveMessage(message, pattern, context, limitTargets, limit);
    }


    private AnalyzerResult resolveMessage(Message message, Pattern thankPattern, MessageContext targets, boolean limitTargets, int limit) {
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
                users.addAll(weightedMembers);
            }
        }

        var members = users.stream()
                .filter(e -> e.getWeight() >= configuration.analyzerSettings().minFuzzyScore())
                .distinct()
                .sorted()
                .limit(limit)
                .collect(Collectors.toList());
        if (members.isEmpty()) return AnalyzerResult.noTarget(message.getMember());

        return AnalyzerResult.fuzzy(message.getMember(), members);
    }
}
