package de.chojo.repbot.dao.access.settings;

import de.chojo.jdautil.parsing.Verifier;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.conversion.ArrayConverter;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.ThreadChannel;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ThankSettings extends QueryFactoryHolder implements GuildHolder {
    private static final String THANKWORD = "((?:^|\\b)%s(?:$|\\b))";
    private static final String PATTERN = "(?i)(%s)";
    private Settings settings;
    private final String reaction;
    private final Set<String> reactions;
    private final String[] thankwords;
    private final Set<Long> activeChannel;
    private final boolean channelWhitelist;
    private final Set<Long> donorRoles;
    private final Set<Long> receiverRoles;

    public ThankSettings(Settings settings) {
        this(settings, null, new String[0], new String[0], new Long[0], true, new Long[0], new Long[0])
    }

    public ThankSettings(Settings settings, String reaction, String[] reactions, String[] thankwords, Long[] activeChannel, boolean channelWhitelist, Long[] donorRoles, Long[] receiverRoles) {
        super(settings);
        this.settings = settings;
        this.reaction = reaction == null ? "✅" : reaction;
        this.reactions = Arrays.stream(reactions).collect(Collectors.toSet());
        this.thankwords = thankwords;
        this.activeChannel = Arrays.stream(activeChannel).collect(Collectors.toSet());
        this.channelWhitelist = channelWhitelist;
        this.donorRoles = Arrays.stream(donorRoles).collect(Collectors.toSet());
        this.receiverRoles = Arrays.stream(receiverRoles).collect(Collectors.toSet());
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
        var twPattern = Arrays.stream(thankwords)
                .map(t -> String.format(THANKWORD, t))
                .collect(Collectors.joining("|"));
        return Pattern.compile(String.format(PATTERN, twPattern),
                Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL + Pattern.COMMENTS);
    }

    public boolean isReputationChannel(Channel channel) {
        if (channel.getType() == ChannelType.GUILD_PUBLIC_THREAD) {
            channel = ((ThreadChannel) channel).getParentMessageChannel();
        }

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

    public static  ThankSettings build(Settings settings, ResultSet row) throws SQLException {
        return new ThankSettings(settings,
                row.getString("reaction"),
                ArrayConverter.toArray(row, "reactions", new String[0]),
                ArrayConverter.toArray(row, "thankswords", new String[0]),
                ArrayConverter.toArray(row, "active_channels", new Long[0]),
                row.getBoolean("channel_whitelist"),
                ArrayConverter.toArray(row, "donor_roles", new Long[0]),
                ArrayConverter.toArray(row, "receiver_roles", new Long[0])
        );
    }

    @Override
    public Guild guild() {
        return settings.guild();
    }
}
