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
import de.chojo.sadu.base.QueryFactory;
import de.chojo.sadu.wrapper.util.Row;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.SQLException;
import java.util.HashSet;

public class Thanking extends QueryFactory implements GuildHolder {
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
        super(settings);
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
        var channels = builder(Long.class)
                .query("""
                       SELECT channel_id
                       FROM active_channel
                       WHERE guild_id = ?
                       """)
                .parameter(stmt -> stmt.setLong(guildId()))
                .readRow(r -> r.getLong("channel_id"))
                .allSync();
        var categories = builder(Long.class)
                .query("""
                       SELECT category_id
                       FROM active_categories
                       WHERE guild_id = ?
                       """)
                .parameter(stmt -> stmt.setLong(guildId()))
                .readRow(r -> r.getLong("category_id"))
                .allSync();
        this.channels = new Channels(this, channelWhitelist, new HashSet<>(channels), new HashSet<>(categories));
        return this.channels;
    }

    public DonorRoles donorRoles() {
        if (donorRoles != null) {
            return donorRoles;
        }
        var roles = builder(Long.class)
                .query("""
                       SELECT role_id
                       FROM donor_roles
                       WHERE guild_id = ?
                       """)
                .parameter(stmt -> stmt.setLong(guildId()))
                .readRow(r -> r.getLong("role_id"))
                .allSync();

        donorRoles = new DonorRoles(this, new HashSet<>(roles));
        return donorRoles;
    }

    public ReceiverRoles receiverRoles() {
        if (receiverRoles != null) {
            return receiverRoles;
        }
        var roles = builder(Long.class)
                .query("""
                       SELECT role_id
                       FROM receiver_roles
                       WHERE guild_id = ?
                       """)
                .parameter(stmt -> stmt.setLong(guildId()))
                .readRow(r -> r.getLong("role_id"))
                .allSync();

        receiverRoles = new ReceiverRoles(this, new HashSet<>(roles));
        return receiverRoles;
    }

    public Reactions reactions() {
        if (reactions != null) {
            return reactions;
        }
        var reactions = builder(String.class)
                .query("""
                       SELECT reaction
                       FROM guild_reactions
                       WHERE guild_id = ?
                       """)
                .parameter(stmt -> stmt.setLong(guildId()))
                .readRow(r -> r.getString("reaction"))
                .allSync();
        this.reactions = new Reactions(this, mainReaction, new HashSet<>(reactions));
        return this.reactions;
    }

    public Thankwords thankwords() {
        if (thankwords != null) {
            return thankwords;
        }
        var thankwords = builder(String.class)
                .query("""
                       SELECT thankword
                       FROM thankwords
                       WHERE guild_id = ?
                       """)
                .parameter(stmt -> stmt.setLong(guildId()))
                .readRow(r -> r.getString("thankword"))
                .allSync();

        this.thankwords = new Thankwords(this, new HashSet<>(thankwords));
        return this.thankwords;
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
