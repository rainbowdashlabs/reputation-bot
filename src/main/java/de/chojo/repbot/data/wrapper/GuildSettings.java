package de.chojo.repbot.data.wrapper;

import de.chojo.jdautil.parsing.Verifier;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
    private final int minMessages;
    private final String reaction;
    private final boolean reactionActive;
    private final boolean answerActive;
    private final boolean mentionActive;
    private final boolean fuzzyActive;
    private final Set<Long> activeChannel;
    private final int cooldown;
    private final Long managerRole;
    private final Set<String> reactions;
    private final boolean channelWhitelist;

    public GuildSettings(Guild guild, String prefix, String[] thankwords, int maxMessageAge, int minMessages, String reaction,
                         boolean reactionActive, boolean answerActive, boolean mentionActive, boolean fuzzyActive,
                         Long[] activeChannel, int cooldown, Long managerRole, String[] reactions, boolean channelWhitelist) {
        this.guild = guild;
        this.prefix = prefix;
        this.thankwords = thankwords;
        this.maxMessageAge = maxMessageAge;
        this.minMessages = minMessages;
        this.reaction = reaction == null ? "✅" : reaction;
        this.reactionActive = reactionActive;
        this.answerActive = answerActive;
        this.mentionActive = mentionActive;
        this.fuzzyActive = fuzzyActive;
        this.activeChannel = Set.of(activeChannel);
        this.cooldown = cooldown;
        this.managerRole = managerRole;
        this.reactions = Set.of(reactions);
        this.channelWhitelist = channelWhitelist;
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
        if (channelWhitelist) {
            return activeChannel.contains(channel.getIdLong());
        }
        return !activeChannel.contains(channel.getIdLong());
    }

    public boolean isReaction(MessageReaction.ReactionEmote reactionEmote) {
        if (reactionEmote.isEmoji()) {
            return isReaction(reactionEmote.getEmoji());
        }
        return isReaction(reactionEmote.getId());
    }

    private boolean isReaction(String reaction) {
        if (this.reaction.equals(reaction)) {
            return true;
        }
        return reactions.contains(reaction);
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

    @Nullable
    public String reactionMention(Guild guild) {
        if (!reactionIsEmote()) {
            return reaction;
        }
        return guild.retrieveEmoteById(reaction).onErrorFlatMap(n -> null).complete().getAsMention();
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

    public int minMessages() {
        return minMessages;
    }

    public List<String> getAdditionalReactionMentions(Guild guild) {
        return reactions.stream()
                .map(reaction -> {
                    if (Verifier.isValidId(reaction)) {
                        var asMention = guild.retrieveEmoteById(reaction).onErrorFlatMap(n -> null).complete();
                        return asMention == null ? null : asMention.getAsMention();
                    }
                    return reaction;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    public boolean isChannelWhitelist() {
        return channelWhitelist;
    }
}
