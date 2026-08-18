/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub;

import com.fasterxml.jackson.annotation.JsonSerializeAs;
import de.chojo.repbot.analyzer.results.match.ThankType;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.web.pojo.settings.sub.MessagesPOJO;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import org.jetbrains.annotations.PropertyKey;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

@JsonSerializeAs(MessagesPOJO.class)
public class Messages extends MessagesPOJO implements GuildHolder {
    private final Settings settings;

    public Messages(Settings settings) {
        this(settings, false, true, false, false, false, false, false, false, true);
    }

    public Messages(
            Settings settings,
            boolean commandReputationEphemeral,
            boolean announceReaction,
            boolean announceAnswer,
            boolean announceMention,
            boolean announceFuzzy,
            boolean announceEmbed,
            boolean announceDirect,
            boolean announceCommand,
            boolean announceDelete) {
        super(
                commandReputationEphemeral,
                announceReaction,
                announceAnswer,
                announceMention,
                announceFuzzy,
                announceEmbed,
                announceDirect,
                announceCommand,
                announceDelete);
        this.settings = settings;
    }

    public static Messages build(Settings settings, Row rs) throws SQLException {
        return new Messages(
                settings,
                rs.getBoolean("command_reputation_ephemeral"),
                rs.getBoolean("announce_reaction"),
                rs.getBoolean("announce_answer"),
                rs.getBoolean("announce_mention"),
                rs.getBoolean("announce_fuzzy"),
                rs.getBoolean("announce_embed"),
                rs.getBoolean("announce_direct"),
                rs.getBoolean("announce_command"),
                rs.getBoolean("announce_delete"));
    }

    public boolean commandReputationEphemeral(boolean commandReputationEphemeral) {
        var result = set("command_reputation_ephemeral", stmt -> stmt.bind(commandReputationEphemeral));
        if (result) {
            this.commandReputationEphemeral = commandReputationEphemeral;
        }
        return this.commandReputationEphemeral;
    }

    public boolean announceReaction(boolean announceReaction) {
        var result = set("announce_reaction", stmt -> stmt.bind(announceReaction));
        if (result) {
            this.announceReaction = announceReaction;
        }
        return this.announceReaction;
    }

    public boolean announceAnswer(boolean announceAnswer) {
        var result = set("announce_answer", stmt -> stmt.bind(announceAnswer));
        if (result) {
            this.announceAnswer = announceAnswer;
        }
        return this.announceAnswer;
    }

    public boolean announceMention(boolean announceMention) {
        var result = set("announce_mention", stmt -> stmt.bind(announceMention));
        if (result) {
            this.announceMention = announceMention;
        }
        return this.announceMention;
    }

    public boolean announceFuzzy(boolean announceFuzzy) {
        var result = set("announce_fuzzy", stmt -> stmt.bind(announceFuzzy));
        if (result) {
            this.announceFuzzy = announceFuzzy;
        }
        return this.announceFuzzy;
    }

    public boolean announceEmbed(boolean announceEmbed) {
        var result = set("announce_embed", stmt -> stmt.bind(announceEmbed));
        if (result) {
            this.announceEmbed = announceEmbed;
        }
        return this.announceEmbed;
    }

    public boolean announceDirect(boolean announceDirect) {
        var result = set("announce_direct", stmt -> stmt.bind(announceDirect));
        if (result) {
            this.announceDirect = announceDirect;
        }
        return this.announceDirect;
    }

    public boolean announceCommand(boolean announceCommand) {
        var result = set("announce_command", stmt -> stmt.bind(announceCommand));
        if (result) {
            this.announceCommand = announceCommand;
        }
        return this.announceCommand;
    }

    public boolean announceDelete(boolean announceDelete) {
        var result = set("announce_delete", stmt -> stmt.bind(announceDelete));
        if (result) {
            this.announceDelete = announceDelete;
        }
        return this.announceDelete;
    }

    /**
     * Checks whether a reputation of this type should be announced in the channel it was given in.
     * <p>
     * A publicly visible reply of the reputation command is the announcement itself. In that case no additional message
     * is sent and {@link de.chojo.repbot.commands.reputation.handler.Give} renders the announcement as its reply.
     *
     * @param type type of the given reputation
     * @return true if a message should be sent
     */
    public boolean isAnnounced(ThankType type) {
        return switch (type) {
            case FUZZY -> announceFuzzy;
            case MENTION -> announceMention;
            case ANSWER -> announceAnswer;
            case DIRECT -> announceDirect;
            case REACTION -> announceReaction;
            case EMBED -> announceEmbed;
            case COMMAND -> announceCommand && commandReputationEphemeral;
        };
    }

    /**
     * Checks whether the reply of the reputation command carries the announcement itself.
     * <p>
     * This is the case when the reply is visible to everyone. It is the exact counterpart of
     * {@link #isAnnounced(ThankType)} for {@link ThankType#COMMAND}: either the reply announces the reputation, or an
     * additional message does, but never both.
     *
     * @return true if the command reply should render the announcement
     */
    public boolean isAnnouncedInReply() {
        return announceCommand && !commandReputationEphemeral;
    }

    public void apply(MessagesPOJO state) {
        if (isCommandReputationEphemeral() != state.isCommandReputationEphemeral())
            commandReputationEphemeral(state.isCommandReputationEphemeral());
        if (isAnnounceReaction() != state.isAnnounceReaction()) announceReaction(state.isAnnounceReaction());
        if (isAnnounceAnswer() != state.isAnnounceAnswer()) announceAnswer(state.isAnnounceAnswer());
        if (isAnnounceMention() != state.isAnnounceMention()) announceMention(state.isAnnounceMention());
        if (isAnnounceFuzzy() != state.isAnnounceFuzzy()) announceFuzzy(state.isAnnounceFuzzy());
        if (isAnnounceEmbed() != state.isAnnounceEmbed()) announceEmbed(state.isAnnounceEmbed());
        if (isAnnounceDirect() != state.isAnnounceDirect()) announceDirect(state.isAnnounceDirect());
        if (isAnnounceCommand() != state.isAnnounceCommand()) announceCommand(state.isAnnounceCommand());
        if (isAnnounceDelete() != state.isAnnounceDelete()) announceDelete(state.isAnnounceDelete());
    }

    @Override
    public GuildHolder guildHolder() {
        return settings;
    }

    public String toLocalizedString() {
        var setting = List.of(
                getSetting(
                        "command.messages.states.message.option.commandreputationephemeral.name",
                        isCommandReputationEphemeral()),
                "$command.messages.states.message.announcements$",
                getSetting("thankType.reaction.name", isAnnounceReaction()),
                getSetting("thankType.answer.name", isAnnounceAnswer()),
                getSetting("thankType.mention.name", isAnnounceMention()),
                getSetting("thankType.fuzzy.name", isAnnounceFuzzy()),
                getSetting("thankType.embed.name", isAnnounceEmbed()),
                getSetting("thankType.direct.name", isAnnounceDirect()),
                getSetting("thankType.command.name", isAnnounceCommand()),
                getSetting("command.messages.states.message.option.announcedelete.name", isAnnounceDelete()));

        return String.join("\n", setting);
    }

    public String prettyString() {
        return """
                Command reputation ephemeral: %s
                Announce reaction: %s
                Announce answer: %s
                Announce mention: %s
                Announce fuzzy: %s
                Announce embed: %s
                Announce direct: %s
                Announce command: %s
                Delete announcements: %s
                """.stripIndent().formatted(
                commandReputationEphemeral,
                announceReaction,
                announceAnswer,
                announceMention,
                announceFuzzy,
                announceEmbed,
                announceDirect,
                announceCommand,
                announceDelete);
    }

    private boolean set(String parameter, Function<Call, Call> builder) {
        return query("""
                INSERT INTO message_states(guild_id, %s) VALUES (?, ?)
                ON CONFLICT(guild_id)
                    DO UPDATE SET %s = excluded.%s;
                """, parameter, parameter, parameter)
                .single(builder.apply(call().bind(guildId())))
                .insert()
                .changed();
    }

    private String getSetting(@PropertyKey(resourceBundle = "locale") String locale, boolean object) {
        return String.format("$%s$: $%s$", locale, object ? "words.enabled" : "words.disabled");
    }
}
