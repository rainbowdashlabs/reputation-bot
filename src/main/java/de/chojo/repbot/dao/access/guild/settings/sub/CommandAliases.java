/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub;

import com.fasterxml.jackson.annotation.JsonSerializeAs;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.web.pojo.settings.sub.CommandAliasesPOJO;

import java.util.Map;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

/**
 * Custom command names of a guild.
 */
@JsonSerializeAs(CommandAliasesPOJO.class)
public class CommandAliases extends CommandAliasesPOJO implements GuildHolder {
    private final Settings settings;

    public CommandAliases(Settings settings, Map<String, String> aliases) {
        this.settings = settings;
        this.aliases.putAll(aliases);
    }

    public Optional<String> alias(String command) {
        return Optional.ofNullable(aliases.get(command));
    }

    public Map<String, String> aliases() {
        return Map.copyOf(aliases);
    }

    public void set(String command, String alias) {
        query("""
                INSERT
                INTO
                    command_aliases
                    (guild_id, command, alias)
                VALUES
                    (?, ?, ?)
                ON CONFLICT(guild_id, command) DO UPDATE SET
                    alias = excluded.alias
                """).single(call().bind(guildId()).bind(command).bind(alias)).insert();
        aliases.put(command, alias);
    }

    public void remove(String command) {
        query("DELETE FROM command_aliases WHERE guild_id = ? AND command = ?")
                .single(call().bind(guildId()).bind(command))
                .delete();
        aliases.remove(command);
    }

    @Override
    public GuildHolder guildHolder() {
        return settings;
    }
}
