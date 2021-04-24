package de.chojo.repbot.analyzer;

import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.parsing.WeightedEntry;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class MessageAnalyzer {
    private static final int LOOKAROUND = 6;

    public static AnalyzerResult processMessage(Pattern pattern, Message message) {
        var contentRaw = message.getContentRaw();

        if (!pattern.matcher(contentRaw).find()) return AnalyzerResult.noMatch();
        if (message.getType() == MessageType.INLINE_REPLY) {

            var referencedMessage = message.getReferencedMessage();
            if (referencedMessage == null) return AnalyzerResult.noMatch();

            return AnalyzerResult.answer(message.getAuthor(), referencedMessage.getAuthor(), referencedMessage);
        }

        var mentionedMembers = message.getMentionedUsers();
        if (mentionedMembers.size() > 0) {
            if (mentionedMembers.size() > 1) {
                return resolveMessage(message, pattern);
            }
            return AnalyzerResult.mention(message.getAuthor(), mentionedMembers.get(0));
        } else {
            return resolveMessage(message, pattern);
        }
    }

    private static AnalyzerResult resolveMessage(Message message, Pattern thankPattern) {
        var contentRaw = message.getContentRaw();

        var words = List.of(contentRaw.split("\\s"));

        String match = null;
        for (var word : words) {
            if (thankPattern.matcher(word).find()) {
                match = word;
            }
        }

        List<String> resolve = new ArrayList<>();

        var thankwordindex = words.indexOf(match);
        resolve.addAll(words.subList(Math.max(0, thankwordindex - LOOKAROUND), thankwordindex));
        resolve.addAll(words.subList(Math.min(thankwordindex + 1, words.size() - 1), Math.min(words.size(), thankwordindex + LOOKAROUND + 1)));

        List<WeightedEntry<Member>> members = new ArrayList<>();

        for (var word : resolve) {
            var weightedMembers = DiscordResolver.fuzzyGuildUserSearch(message.getGuild(), word);
            if (weightedMembers.isEmpty()) continue;
            members.addAll(weightedMembers);
        }

        members.sort(Comparator.reverseOrder());
        if (members.isEmpty()) return AnalyzerResult.noMatch();
        var memberWeightedEntry = members.get(0);

        return AnalyzerResult.fuzzy(message.getAuthor(), memberWeightedEntry.getReference().getUser(), memberWeightedEntry.getWeight());
    }
}
