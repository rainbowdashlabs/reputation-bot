/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub.thanking;

import de.chojo.jdautil.parsing.Verifier;
import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import de.chojo.repbot.dao.components.GuildHolder;
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

/**
 * Manages the reactions for thanking in a guild.
 */
public class Reactions implements GuildHolder {
    private final Thanking thanking;
    private final Set<String> reactions;
    private String mainReaction;

    /**
     * Constructs a Reactions instance.
     *
     * @param thanking the thanking settings
     * @param mainReaction the main reaction
     * @param reactions the set of additional reactions
     */
    public Reactions(Thanking thanking, String mainReaction, Set<String> reactions) {
        this.thanking = thanking;
        this.mainReaction = mainReaction;
        this.reactions = reactions;
    }

    /**
     * Gets the guild associated with the reactions.
     *
     * @return the guild
     */
    @Override
    public Guild guild() {
        return thanking.guild();
    }

    /**
     * Gets the guild ID associated with the reactions.
     *
     * @return the guild ID
     */
    @Override
    public long guildId() {
        return thanking.guildId();
    }

    /**
     * Checks if the given reaction is a valid reaction.
     *
     * @param reaction the message reaction
     * @return true if the reaction is valid, false otherwise
     */
    public boolean isReaction(MessageReaction reaction) {
        if (reaction.getEmoji() instanceof UnicodeEmoji emoji) {
            return isReaction(emoji.getAsReactionCode());
        }
        if (reaction.getEmoji() instanceof CustomEmoji emoji) {
            return isReaction(emoji.getId());
        }
        return false;
    }

    /**
     * Checks if the given reaction string is a valid reaction.
     *
     * @param reaction the reaction string
     * @return true if the reaction is valid, false otherwise
     */
    private boolean isReaction(String reaction) {
        if (mainReaction.equals(reaction)) {
            return true;
        }
        return reactions.contains(reaction);
    }

    /**
     * Checks if the main reaction is an emote.
     *
     * @return true if the main reaction is an emote, false otherwise
     */
    public boolean reactionIsEmote() {
        return Verifier.isValidId(mainReaction());
    }

    /**
     * Gets the mention string for the main reaction.
     *
     * @return an optional containing the mention string if available, otherwise empty
     */
    public Optional<String> reactionMention() {
        if (!reactionIsEmote()) {
            return Optional.ofNullable(mainReaction());
        }
        return Optional.of(guild().retrieveEmojiById(mainReaction()).onErrorMap(err -> null).complete())
                .map(CustomEmoji::getAsMention);
    }

    /**
     * Gets the main reaction string.
     *
     * @return the main reaction string
     */
    public String mainReaction() {
        return mainReaction;
    }

    /**
     * Gets the mentions for the additional reactions.
     *
     * @return a list of additional reaction mentions
     */
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

    /**
     * Adds a new reaction to the set of reactions.
     *
     * @param reaction the reaction string
     * @return true if the reaction was added successfully, false otherwise
     */
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

    /**
     * Removes a reaction from the set of reactions.
     *
     * @param reaction the reaction string
     * @return true if the reaction was removed successfully, false otherwise
     */
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

    /**
     * Sets the main reaction.
     *
     * @param reaction the reaction string
     * @return true if the main reaction was set successfully, false otherwise
     */
    public boolean mainReaction(String reaction) {
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

    /**
     * Gets the set of reactions.
     *
     * @return the set of reactions
     */
    public Set<String> reactions() {
        return reactions;
    }
}
