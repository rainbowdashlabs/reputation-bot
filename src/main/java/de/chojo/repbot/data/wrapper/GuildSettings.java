package de.chojo.repbot.data.wrapper;

import de.chojo.jdautil.parsing.Verifier;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GuildSettings {
    private static final String THANKWORD = "((?:^|\\b)%s(?:$|\\b))";
    private static final String PATTERN = "(?i)(%s)";
    private final Guild guild;
    private final String prefix;
    private final String[] thankwords;
    private final int maxMessageAge;
    private final String reaction;
    private final boolean reactionActive;
    private final boolean answerActive;
    private final boolean mentionActive;
    private final boolean fuzzyActive;
    private final Set<Long> activeChannel;
    private final int cooldown;
    private final Long managerRole;

    public GuildSettings(Guild guild, String prefix, String[] thankwords, int maxMessageAge, String reaction,
                         boolean reactionActive, boolean answerActive, boolean mentionActive, boolean fuzzyActive,
                         Long[] activeChannel, int cooldown, Long managerRole) {
        this.guild = guild;
        this.prefix = prefix;
        this.thankwords = thankwords;
        this.maxMessageAge = maxMessageAge;
        this.reaction = reaction == null ? "✅" : reaction;
        this.reactionActive = reactionActive;
        this.answerActive = answerActive;
        this.mentionActive = mentionActive;
        this.fuzzyActive = fuzzyActive;
        this.activeChannel = Set.of(activeChannel);
        this.cooldown = cooldown;
        this.managerRole = managerRole;
    }

    public Pattern thankwordPattern() {
        if (thankwords.length == 0) return Pattern.compile("");
        var twPattern = Arrays.stream(this.thankwords)
                .map(t -> String.format(THANKWORD, t))
                .collect(Collectors.joining("|"));
        return Pattern.compile(String.format(PATTERN, twPattern),
                Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL + Pattern.COMMENTS);
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

    public boolean isFreshMessage(Message message) {
        var until = message.getTimeCreated().toInstant().until(Instant.now(), ChronoUnit.MINUTES);
        return until < maxMessageAge;
    }

    public Optional<String> prefix() {
        return Optional.ofNullable(prefix);
    }

    public String reactionMention(Guild guild) {
        if (!reactionIsEmote()) {
            return reaction;
        }
        return guild.retrieveEmoteById(reaction).complete().getAsMention();
    }

    public OptionalLong managerRole() {
        return OptionalLong.of(managerRole);
    }

    public int maxMessageAge() {
        return maxMessageAge;
    }

    public boolean isReactionActive() {
        return reactionActive;
    }

    public boolean isAnswerActive() {
        return answerActive;
    }

    public boolean isMentionActive() {
        return mentionActive;
    }

    public boolean isFuzzyActive() {
        return fuzzyActive;
    }

    public String[] thankwords() {
        return thankwords;
    }

    /**
     * Get the reaction.
     * <p>
     * This may be a unicode emote or a emote id of the guild
     *
     * @return reaction for reputation
     */
    public String reaction() {
        return reaction;
    }

    public Set<Long> activeChannel() {
        return activeChannel;
    }

    public int cooldown() {
        return cooldown;
    }

    public Guild guild() {
        return guild;
    }
}
