package de.chojo.repbot.analyzer;

import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.parsing.WeightedEntry;
import de.chojo.repbot.util.HistoryUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageAnalyzer {
    private static final int LOOKAROUND = 6;

    public static AnalyzerResult processMessage(Pattern pattern, Message message, int maxHistoryAge, boolean limitTargets, double threshold, int limit) {
        if (pattern.pattern().isBlank()) return AnalyzerResult.noMatch();
        var contentRaw = message.getContentRaw();

        if (!pattern.matcher(contentRaw).find()) return AnalyzerResult.noMatch();
        if (message.getType() == MessageType.INLINE_REPLY) {

            var referencedMessage = message.getReferencedMessage();
            if (referencedMessage == null) return AnalyzerResult.noMatch();

            return AnalyzerResult.answer(message.getAuthor(), message.getGuild().retrieveMemberById(referencedMessage.getAuthor().getIdLong()).complete(), referencedMessage);
        }

        Set<Member> targets = Collections.emptySet();
        if (limitTargets) {
            targets = HistoryUtil.getRecentMembers(message, maxHistoryAge);

            if (targets.size() == 1) {
                return AnalyzerResult.mention(message.getAuthor(), new ArrayList<>(targets));
            }
        }

        var mentionedMembers = message.getMentionedUsers();
        if (mentionedMembers.size() > 0) {
            if (mentionedMembers.size() > limit) {
                return resolveMessage(message, pattern, targets, threshold, limit);
            }
            return AnalyzerResult.mention(message.getAuthor(), mentionedMembers.stream().map(u -> message.getGuild().retrieveMemberById(u.getIdLong()).complete()).collect(Collectors.toList()));
        } else {
            return resolveMessage(message, pattern, targets, threshold, limit);
        }
    }


    private static AnalyzerResult resolveMessage(Message message, Pattern thankPattern, @NotNull Set<Member> targets, double threshold, int limit) {
        var contentRaw = message.getContentRaw();

        var words = new ArrayList<>(List.of(contentRaw.split("\\s")));
        words.removeIf(String::isBlank);

        List<Integer> thankWordIndices = new ArrayList<>();
        var i = 0;
        for (var word : words) {
            if (thankPattern.matcher(word).find()) {
                thankWordIndices.add(i);
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
                if (targets.isEmpty()) {
                    weightedMembers = DiscordResolver.fuzzyGuildTargetSearch(word, targets);
                } else {
                    weightedMembers = DiscordResolver.fuzzyGuildUserSearch(message.getGuild(), word);
                }
                if (weightedMembers.isEmpty()) continue;
                users.addAll(weightedMembers);
            }
        }

        var members = users.stream()
                .filter(e -> e.getWeight() >= threshold)
                .distinct()
                .sorted()
                .limit(limit)
                .collect(Collectors.toList());
        if (members.isEmpty()) return AnalyzerResult.noTarget(message.getAuthor());

        return AnalyzerResult.fuzzy(message.getAuthor(), members);
    }
}
