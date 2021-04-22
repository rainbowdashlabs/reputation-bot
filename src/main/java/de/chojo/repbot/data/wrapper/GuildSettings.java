package de.chojo.repbot.data.wrapper;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;

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

    public GuildSettings(Guild guild, String prefix, String[] thankwords, int maxMessageAge, String reaction,
                         boolean reactionActive, boolean answerActive, boolean mentionActive, Long[] activeChannel) {
        this.guild = guild;
        this.prefix = prefix;
        this.thankwords = thankwords;
        this.maxMessageAge = maxMessageAge;
        this.reaction = reaction;
        this.reactionActive = reactionActive;
        this.answerActive = answerActive;
        this.mentionActive = mentionActive;
        this.activeChannel = Set.of(activeChannel);
    }

    public Pattern getThankwordPattern() {
        return Pattern.compile(
                "(?i)(" + String.join(")|(", thankwords) + ")",
                Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL);

    }
}
