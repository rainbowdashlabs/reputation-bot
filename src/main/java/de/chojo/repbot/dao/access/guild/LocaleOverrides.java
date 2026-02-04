/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild;

import de.chojo.jdautil.container.Pair;
import de.chojo.repbot.dao.components.GuildHolder;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class LocaleOverrides implements GuildHolder {
    private final RepGuild guild;
    private Map<String, String> overrides;

    public LocaleOverrides(RepGuild guild) {
        this.guild = guild;
    }

    @Override
    public Guild guild() {
        return guild.guild();
    }

    public Optional<String> getOverride(String code) {
        return Optional.ofNullable(overrides().get(code));
    }

    public void setOverride(String code, String value) {
        query("INSERT INTO guild_locale_overrides(guild_id, code, value) VALUES(?,?,?) ON CONFLICT(guild_id, code) DO UPDATE SET value = excluded.value")
                .single(call().bind(guildId()).bind(code).bind(value))
                .insert()
                .ifChanged(i -> overrides().put(code, value));
    }

    public void removeOverride(String code) {
        query("DELETE FROM guild_locale_overrides WHERE guild_id =? AND  code =?")
                .single(call().bind(guildId()).bind(code))
                .delete()
                .ifChanged(i -> overrides().remove(code));
    }

    private Map<String, String> overrides() {
        if (overrides == null) {
            overrides = query("SELECT code, value FROM guild_locale_overrides WHERE guild_id = ?")
                    .single(call().bind(guildId()))
                    .map(r -> Pair.of(r.getString("code"), r.getString("value")))
                    .all()
                    .stream()
                    .collect(Collectors.toMap(p -> p.first, p -> p.second, (a, b) -> a, HashMap::new));
        }
        return overrides;
    }
}
