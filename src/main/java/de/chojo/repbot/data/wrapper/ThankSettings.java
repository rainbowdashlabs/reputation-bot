package de.chojo.repbot.data.wrapper;

import de.chojo.jdautil.parsing.Verifier;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ThankSettings {
    private static final String THANKWORD = "((?:^|\\b)%s(?:$|\\b))";
    private static final String PATTERN = "(?i)(%s)";
    private final String reaction;
    private final Set<String> reactions;
    private final String[] thankwords;
    private final Set<Long> activeChannel;
    private final boolean channelWhitelist;
    private final Set<Long> donorRoles;
    private final Set<Long> receiverRoles;

    public ThankSettings() {
        reaction = null;
        reactions = Collections.emptySet();
        thankwords = new String[0];
        activeChannel = Collections.emptySet();
        channelWhitelist =true;
        donorRoles = Collections.emptySet();
        receiverRoles = Collections.emptySet();
    }

    public ThankSettings(String reaction, String[] reactions, String[] thankwords, Long[] activeChannel, boolean channelWhitelist, Long[] donorRoles, Long[] receiverRoles) {
        this.reaction = reaction == null ? "✅" : reaction;
        this.reactions = Set.of(reactions);
        this.thankwords = thankwords;
        this.activeChannel = Set.of(activeChannel);
        this.channelWhitelist = channelWhitelist;
        this.donorRoles = Set.of(donorRoles);
        this.receiverRoles = Set.of(receiverRoles);
    }

    public String reaction() {
        return reaction;
    }

    public Set<String> reactions() {
        return reactions;
    }

    public String[] thankwords() {
        return thankwords;
    }

    public Set<Long> activeChannel() {
        return activeChannel;
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

    @Nullable
    public String reactionMention(Guild guild) {
        if (!reactionIsEmote()) {
            return reaction;
        }
        return guild.retrieveEmoteById(reaction).onErrorFlatMap(n -> null).complete().getAsMention();
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


    public boolean hasDonorRole(@Nullable Member member) {
        return hasRole(member, donorRoles);
    }

    public boolean hasReceiverRole(@Nullable Member member) {
        return hasRole(member, receiverRoles);
    }

    private boolean hasRole(@Nullable Member member, Set<Long> roleIds) {
        if (member == null) return false;
        if (roleIds.isEmpty()) return true;
        for (var role : member.getRoles()) {
            if (roleIds.contains(role.getIdLong())) return true;
        }
        return false;
    }

    public Set<Long> donorRoles() {
        return donorRoles;
    }

    public Set<Long> receiverRoles() {
        return receiverRoles;
    }
}
