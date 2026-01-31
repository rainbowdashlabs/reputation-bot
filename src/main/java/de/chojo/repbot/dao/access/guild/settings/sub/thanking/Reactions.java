/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub.thanking;

import com.fasterxml.jackson.annotation.JsonSerializeAs;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.jdautil.util.Premium;
import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.web.pojo.settings.sub.AutopostPOJO;
import de.chojo.repbot.web.pojo.settings.sub.thanking.ReactionsPOJO;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

@JsonSerializeAs(ReactionsPOJO.class)
public class Reactions extends ReactionsPOJO implements GuildHolder {
    private final Thanking thanking;

    public Reactions(Thanking thanking, String mainReaction, Set<String> reactions) {
        super(reactions, mainReaction);
        this.thanking = thanking;
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
        if (Premium.isNotEntitled(
                thanking.settings().repGuild().subscriptions(),
                thanking.settings().repGuild().configuration().skus().features().additionalEmojis().additionalEmojis())) {
            return false;
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
        var result = query("""
                INSERT INTO guild_reactions(guild_id, reaction) VALUES (?,?)
                    ON CONFLICT(guild_id, reaction)
                        DO NOTHING;
                """)
                .single(call().bind(guildId()).bind(reaction))
                .update()
                .changed();
        if (result) {
            reactions.add(reaction);
        }
        return result;
    }

    public boolean remove(String reaction) {
        var result = query("""
                DELETE FROM guild_reactions WHERE guild_id = ? AND reaction = ?;
                """)
                .single(call().bind(guildId()).bind(reaction))
                .update()
                .changed();
        if (result) {
            reactions.remove(reaction);
        }

        return result;
    }

    public boolean mainReaction(String reaction) {
        reaction = Objects.requireNonNullElse(reaction, Thanking.DEFAULT_REACTION);
        var result = query("""
                INSERT INTO thank_settings(guild_id, reaction) VALUES (?,?)
                    ON CONFLICT(guild_id)
                        DO UPDATE
                            SET reaction = excluded.reaction
                """)
                .single(call().bind(guildId()).bind(reaction))
                .update()
                .changed();
        if (result) {
            mainReaction = reaction;
        }
        return result;
    }

}
