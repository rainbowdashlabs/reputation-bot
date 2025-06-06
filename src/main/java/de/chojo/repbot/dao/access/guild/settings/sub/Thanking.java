/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub;

import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.access.guild.settings.sub.thanking.Channels;
import de.chojo.repbot.dao.access.guild.settings.sub.thanking.DonorRoles;
import de.chojo.repbot.dao.access.guild.settings.sub.thanking.Reactions;
import de.chojo.repbot.dao.access.guild.settings.sub.thanking.ReceiverRoles;
import de.chojo.repbot.dao.access.guild.settings.sub.thanking.Thankwords;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sadu.mapper.wrapper.Row;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.SQLException;
import java.util.HashSet;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class Thanking  implements GuildHolder {
    private static final String DEFAULT_REACTION = "🏅";
    private final String mainReaction;
    private final Settings settings;
    private final boolean channelWhitelist;

    private Channels channels;
    private DonorRoles donorRoles;
    private ReceiverRoles receiverRoles;
    private Reactions reactions;
    private Thankwords thankwords;

    public Thanking(Settings settings) {
        this(settings, DEFAULT_REACTION, true);
    }

    public Thanking(Settings settings, String mainReaction, boolean channelWhitelist) {
        this.settings = settings;
        this.mainReaction = mainReaction;
        this.channelWhitelist = channelWhitelist;
    }

    public static Thanking build(Settings settings, Row row) throws SQLException {
        return new Thanking(settings,
                row.getString("reaction"),
                row.getBoolean("channel_whitelist")
        );
    }

    public Channels channels() {
        if (channels != null) {
            return channels;
        }
        var channels = query("""
                       SELECT channel_id
                       FROM active_channel
                       WHERE guild_id = ?
                       """)
                .single(call().bind(guildId()))
                .map(r -> r.getLong("channel_id"))
                .all();
        var categories = query("""
                       SELECT category_id
                       FROM active_categories
                       WHERE guild_id = ?
                       """)
                .single(call().bind(guildId()))
                .mapAs(Long.class)
                .all();
        this.channels = new Channels(this, channelWhitelist, new HashSet<>(channels), new HashSet<>(categories));
        return this.channels;
    }

    public DonorRoles donorRoles() {
        if (donorRoles != null) {
            return donorRoles;
        }
        var roles = query("""
                       SELECT role_id
                       FROM donor_roles
                       WHERE guild_id = ?
                       """)
                .single(call().bind(guildId()))
                .mapAs(Long.class)
                .all();

        donorRoles = new DonorRoles(this, new HashSet<>(roles));
        return donorRoles;
    }

    public ReceiverRoles receiverRoles() {
        if (receiverRoles != null) {
            return receiverRoles;
        }
        var roles = query("""
                       SELECT role_id
                       FROM receiver_roles
                       WHERE guild_id = ?
                       """)
                .single(call().bind(guildId()))
                .mapAs(Long.class)
                .all();

        receiverRoles = new ReceiverRoles(this, new HashSet<>(roles));
        return receiverRoles;
    }

    public Reactions reactions() {
        if (reactions != null) {
            return reactions;
        }
        var reactions = query("""
                       SELECT reaction
                       FROM guild_reactions
                       WHERE guild_id = ?
                       """)
                .single(call().bind(guildId()))
                .mapAs(String.class)
                .all();
        this.reactions = new Reactions(this, mainReaction, new HashSet<>(reactions));
        return this.reactions;
    }

    public Thankwords thankwords() {
        if (thankwords != null) {
            return thankwords;
        }
        var thankwords = query("""
                       SELECT thankword
                       FROM thankwords
                       WHERE guild_id = ?
                       """)
                .single(call().bind(guildId()))
                .mapAs(String.class)
                .all();

        this.thankwords = new Thankwords(this, new HashSet<>(thankwords));
        return this.thankwords;
    }

    public Settings settings() {
        return settings;
    }

    @Override
    public Guild guild() {
        return settings.guild();
    }

    @Override
    public long guildId() {
        return settings.guildId();
    }
}
