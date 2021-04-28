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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class MessageAnalyzer {
    private static final int LOOKAROUND = 6;

    public static AnalyzerResult processMessage(Pattern pattern, Message message, int maxHistoryAge, boolean limitTargets) {
        if (pattern.pattern().isBlank()) return AnalyzerResult.noMatch();
        var contentRaw = message.getContentRaw();

        if (!pattern.matcher(contentRaw).find()) return AnalyzerResult.noMatch();
        if (message.getType() == MessageType.INLINE_REPLY) {

            var referencedMessage = message.getReferencedMessage();
            if (referencedMessage == null) return AnalyzerResult.noMatch();

            return AnalyzerResult.answer(message.getAuthor(), referencedMessage.getAuthor(), referencedMessage);
        }

        Set<Member> targets = Collections.emptySet();
        if (limitTargets) {
            targets = HistoryUtil.getRecentMembers(message, maxHistoryAge);
        }

        var mentionedMembers = message.getMentionedUsers();
        if (mentionedMembers.size() > 0) {
            if (mentionedMembers.size() > 1) {
                return resolveMessage(message, pattern, targets);
            }
            return AnalyzerResult.mention(message.getAuthor(), mentionedMembers.get(0));
        } else {
            return resolveMessage(message, pattern, targets);
        }
    }


    private static AnalyzerResult resolveMessage(Message message, Pattern thankPattern, @NotNull Set<Member> targets) {
        var contentRaw = message.getContentRaw();

        var words = new ArrayList<>(List.of(contentRaw.split("\\s")));
        words.removeIf(String::isBlank);

        String match = null;
        for (var word : words) {
            if (thankPattern.matcher(word).find()) {
                match = word;
            }
        }

        List<String> resolve = new ArrayList<>();

        var thankwordindex = words.indexOf(match);
        if (thankwordindex != 0) {
            resolve.addAll(words.subList(Math.max(0, thankwordindex - LOOKAROUND), thankwordindex));
        }
        if (thankwordindex != words.size() - 1) {
            resolve.addAll(words.subList(Math.min(thankwordindex + 1, words.size() - 1), Math.min(words.size(), thankwordindex + LOOKAROUND + 1)));
        }

        List<WeightedEntry<Member>> members = new ArrayList<>();

        for (var word : resolve) {
            List<WeightedEntry<Member>> weightedMembers;
            if (targets.isEmpty()) {
                weightedMembers = DiscordResolver.fuzzyGuildTargetSearch(word, targets);
            } else {
                weightedMembers = DiscordResolver.fuzzyGuildUserSearch(message.getGuild(), word);
            }
            if (weightedMembers.isEmpty()) continue;
            members.addAll(weightedMembers);
        }

        members.sort(Comparator.reverseOrder());
        if (members.isEmpty()) return AnalyzerResult.noTarget(message.getAuthor());
        var memberWeightedEntry = members.get(0);

        return AnalyzerResult.fuzzy(message.getAuthor(), memberWeightedEntry.getReference().getUser(), memberWeightedEntry.getWeight());
    }
}
