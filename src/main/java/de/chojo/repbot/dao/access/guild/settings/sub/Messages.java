/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub;

import com.fasterxml.jackson.annotation.JsonSerializeAs;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.web.pojo.settings.sub.MessagesPOJO;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import net.dv8tion.jda.api.entities.Guild;
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
        this(settings, true, false);
    }

    public Messages(Settings settings, boolean reactionConfirmation, boolean commandReputationEphemeral) {
        super(reactionConfirmation, commandReputationEphemeral);
        this.settings = settings;
    }

    public static Messages build(Settings settings, Row rs) throws SQLException {
        return new Messages(settings,
                rs.getBoolean("reaction_confirmation"),
                rs.getBoolean("command_reputation_ephemeral"));
    }

    public boolean reactionConfirmation(boolean reactionConfirmation) {
        var result = set("reaction_confirmation", stmt -> stmt.bind(reactionConfirmation));
        if (result) {
            this.reactionConfirmation = reactionConfirmation;
        }
        return this.reactionConfirmation;
    }

    public boolean commandReputationEphemeral(boolean commandReputationEphemeral) {
        var result = set("command_reputation_ephemeral", stmt -> stmt.bind(commandReputationEphemeral));
        if (result) {
            this.commandReputationEphemeral = commandReputationEphemeral;
        }
        return this.commandReputationEphemeral;
    }

    public void apply(MessagesPOJO state) {
        if (isReactionConfirmation() != state.isReactionConfirmation())
            reactionConfirmation(state.isReactionConfirmation());
        if (isCommandReputationEphemeral() != state.isCommandReputationEphemeral())
            commandReputationEphemeral(state.isCommandReputationEphemeral());
    }

    @Override
    public Guild guild() {
        return settings.guild();
    }

    @Override
    public long guildId() {
        return settings.guildId();
    }

    public String toLocalizedString() {
        var setting = List.of(
                getSetting("command.messages.states.message.option.reactionconfirmation.name", isReactionConfirmation()),
                getSetting("command.messages.states.message.option.commandreputationephemeral.name", isCommandReputationEphemeral())
        );

        return String.join("\n", setting);
    }

    public String prettyString() {
        return """
                Reaction confirmation: %s
                """.stripIndent()
                   .formatted(reactionConfirmation);
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
