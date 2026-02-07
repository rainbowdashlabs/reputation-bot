/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub;

import com.fasterxml.jackson.annotation.JsonSerializeAs;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.access.guild.settings.sub.integrationbypass.Bypass;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.web.pojo.settings.sub.IntegrationBypassPOJO;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

@JsonSerializeAs(IntegrationBypassPOJO.class)
public class IntegrationBypass extends IntegrationBypassPOJO implements GuildHolder {
    private final Settings settings;

    public IntegrationBypass(Settings settings, List<Bypass> bypasses) {
        this.settings = settings;
        bypasses.forEach(e -> this.bypasses.put(e.integrationId(), e));
    }

    public void apply(Bypass bypass) {
        apply(bypass.integrationId(), bypass);
    }

    public void apply(long integrationId, Bypass bypass) {
        query("""
                INSERT
                INTO
                    integration_bypass
                    (guild_id, integration_id, allow_reactions, allow_answer, allow_mention, allow_fuzzy, ignore_cooldown, ignore_limit)
                VALUES
                    (?,?,?,?,?,?,?,?)
                ON CONFLICT(guild_id, integration_id) DO UPDATE SET
                    allow_reactions = excluded.allow_reactions,
                    allow_answer = excluded.allow_answer,
                    allow_mention = excluded.allow_mention,
                    allow_fuzzy = excluded.allow_fuzzy,
                    ignore_cooldown = excluded.ignore_cooldown,
                    ignore_limit = excluded.ignore_limit
                """)
                .single(call().bind(guildId())
                              .bind(integrationId)
                              .bind(bypass.allowReactions())
                              .bind(bypass.allowAnswer())
                              .bind(bypass.allowMention())
                              .bind(bypass.allowFuzzy())
                              .bind(bypass.ignoreCooldown())
                              .bind(bypass.ignoreLimit()))
                .insert();
        bypasses.put(bypass.integrationId(), bypass);
    }

    public Optional<Bypass> getBypass(long integrationId) {
        return Optional.ofNullable(bypasses.get(integrationId));
    }

    public void remove(long integrationId) {
        query("DELETE FROM integration_bypass WHERE guild_id = ? AND integration_id = ?")
                .single(call().bind(guildId()).bind(integrationId))
                .delete();
        bypasses.remove(integrationId);
    }

    @Override
    public Guild guild() {
        return settings.guild();
    }
}
