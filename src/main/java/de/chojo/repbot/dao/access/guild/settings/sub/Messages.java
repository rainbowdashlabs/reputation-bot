/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub;

import de.chojo.jdautil.consumer.ThrowingConsumer;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sadu.base.QueryFactory;
import de.chojo.sadu.wrapper.util.ParamBuilder;
import de.chojo.sadu.wrapper.util.Row;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.PropertyKey;

import java.sql.SQLException;
import java.util.List;

public class Messages extends QueryFactory implements GuildHolder {
    private final Settings settings;
    private boolean reactionConfirmation;

    public Messages(Settings settings) {
        this(settings, true);
    }

    public Messages(Settings settings, boolean reactionConfirmation) {
        super(settings);
        this.settings = settings;
        this.reactionConfirmation = reactionConfirmation;
    }

    public static Messages build(Settings settings, Row rs) throws SQLException {
        return new Messages(settings,
                rs.getBoolean("reaction_confirmation"));
    }

    public boolean reactionConfirmation(boolean reactionConfirmation) {
        var result = set("reaction_confirmation", stmt -> stmt.setBoolean(reactionConfirmation));
        if (result) {
            this.reactionConfirmation = reactionConfirmation;
        }
        return this.reactionConfirmation;
    }

    public boolean isReactionConfirmation() {
        return reactionConfirmation;
    }

    @Override
    public Guild guild() {
        return settings.guild();
    }

    @Override
    public long guildId() {
        return settings.guildId();
    }

    private boolean set(String parameter, ThrowingConsumer<ParamBuilder, SQLException> builder) {
        return builder()
                .query("""
                       INSERT INTO message_states(guild_id, %s) VALUES (?, ?)
                       ON CONFLICT(guild_id)
                           DO UPDATE SET %s = excluded.%s;
                       """, parameter, parameter, parameter)
                .parameter(stmts -> {
                    stmts.setLong(guildId());
                    builder.accept(stmts);
                }).insert()
                .sendSync()
                .changed();
    }

    public String toLocalizedString() {
        var setting = List.of(
                getSetting("command.messages.states.message.option.reactionconfirmation.name", isReactionConfirmation())
        );

        return String.join("\n", setting);
    }

    private String getSetting(@PropertyKey(resourceBundle = "locale") String locale, boolean object) {
        return String.format("$%s$: $%s$", locale, object ? "words.enabled" : "words.disabled");
    }

    public String prettyString() {
        return """
               Reaction confirmation: %s
               """.stripIndent()
                .formatted(reactionConfirmation);
    }
}
