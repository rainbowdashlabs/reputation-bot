package de.chojo.repbot.data.wrapper;

import de.chojo.jdautil.parsing.Verifier;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Set;
import java.util.regex.Pattern;

@Getter
public class GuildSettings {
    private final Guild guild;
    private final String prefix;
    private final String[] thankwords;
    private final int maxMessageAge;
    private final String reaction;
    private final boolean reactionActive;
    private final boolean answerActive;
    private final boolean mentionActive;
    private final Set<Long> activeChannel;
    private final int cooldown;

    public GuildSettings(Guild guild, String prefix, String[] thankwords, int maxMessageAge, String reaction,
                         boolean reactionActive, boolean answerActive, boolean mentionActive, Long[] activeChannel, int cooldown) {
        this.guild = guild;
        this.prefix = prefix;
        this.thankwords = thankwords;
        this.maxMessageAge = maxMessageAge;
        this.reaction = reaction == null ? "✅" : reaction;
        this.reactionActive = reactionActive;
        this.answerActive = answerActive;
        this.mentionActive = mentionActive;
        this.activeChannel = Set.of(activeChannel);
        this.cooldown = cooldown;
    }

    public Pattern getThankwordPattern() {
        return Pattern.compile(
                "(?i)(" + String.join(")|(", thankwords) + ")",
                Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL);

    }

    public boolean isReputationChannel(TextChannel channel) {
        return activeChannel.contains(channel.getIdLong());
    }

    public boolean isReaction(MessageReaction.ReactionEmote reactionEmote) {
        if (reactionEmote.isEmoji()) {
            return reactionEmote.getEmoji().equals(reaction);
        }
        return reactionEmote.getId().equals(reaction);
    }

    public boolean reactionIsEmote() {
        return Verifier.isValidId(reaction);
    }
}
