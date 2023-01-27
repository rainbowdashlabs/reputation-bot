package de.chojo.repbot.dao.access.guild.settings.sub.thanking;

import de.chojo.jdautil.parsing.Verifier;
import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Reactions extends QueryFactory implements GuildHolder {
    private final Thanking thanking;
    private final Set<String> reactions;
    private String mainReaction;

    public Reactions(Thanking thanking, String mainReaction, Set<String> reactions) {
        super(thanking);
        this.thanking = thanking;
        this.mainReaction = mainReaction;
        this.reactions = reactions;
    }

    @Override
    public Guild guild() {
        return thanking.guild();
    }

    @Override
    public long guildId() {
        return thanking.guildId();
    }

    public boolean isReaction(MessageReaction reaction) {
        if (reaction.getEmoji() instanceof UnicodeEmoji emoji) {
            return isReaction(emoji.getAsReactionCode());
        }
        if (reaction.getEmoji() instanceof CustomEmoji emoji) {
            return isReaction(emoji.getId());
        }
        return false;
    }

    private boolean isReaction(String reaction) {
        if (mainReaction.equals(reaction)) {
            return true;
        }
        return reactions.contains(reaction);
    }

    public boolean reactionIsEmote() {
        return Verifier.isValidId(mainReaction());
    }

    public Optional<String> reactionMention() {
        if (!reactionIsEmote()) {
            return Optional.ofNullable(mainReaction());
        }
        return Optional.of(guild().retrieveEmojiById(mainReaction()).onErrorMap(err -> null).complete())
                .map(CustomEmoji::getAsMention);
    }

    public String mainReaction() {
        return mainReaction;
    }

    public List<String> getAdditionalReactionMentions() {
        return reactions.stream()
                        .map(reaction -> {
                            if (Verifier.isValidId(reaction)) {
                                var asMention = guild().retrieveEmojiById(reaction).onErrorMap(err -> null)
                                                       .complete();
                                return asMention == null ? null : asMention.getAsMention();
                            }
                            return reaction;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
    }

    public boolean add(String reaction) {
        var result = builder()
                .query("""
                       INSERT INTO guild_reactions(guild_id, reaction) VALUES (?,?)
                           ON CONFLICT(guild_id, reaction)
                               DO NOTHING;
                       """)
                .parameter(stmt -> stmt.setLong(guildId()).setString(reaction))
                .update()
                .sendSync()
                .changed();
        if (result) {
            reactions.add(reaction);
        }
        return result;
    }

    public boolean remove(String reaction) {
        var result = builder()
                .query("""
                       DELETE FROM guild_reactions WHERE guild_id = ? AND reaction = ?;
                       """)
                .parameter(stmt -> stmt.setLong(guildId()).setString(reaction))
                .update()
                .sendSync()
                .changed();
        if (result) {
            reactions.remove(reaction);
        }

        return result;
    }

    public boolean mainReaction(String reaction) {
        var result = builder()
                .query("""
                       INSERT INTO thank_settings(guild_id, reaction) VALUES (?,?)
                           ON CONFLICT(guild_id)
                               DO UPDATE
                                   SET reaction = excluded.reaction
                       """)
                .parameter(stmt -> stmt.setLong(guildId()).setString(reaction))
                .update()
                .sendSync()
                .changed();
        if (result) {
            mainReaction = reaction;
        }
        return result;
    }

    public Set<String> reactions() {
        return reactions;
    }
}
