/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings;

import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.access.guild.settings.sub.AbuseProtection;
import de.chojo.repbot.dao.access.guild.settings.sub.General;
import de.chojo.repbot.dao.access.guild.settings.sub.LogChannel;
import de.chojo.repbot.dao.access.guild.settings.sub.Messages;
import de.chojo.repbot.dao.access.guild.settings.sub.Ranks;
import de.chojo.repbot.dao.access.guild.settings.sub.Reputation;
import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import de.chojo.repbot.dao.access.guild.settings.sub.autopost.Autopost;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.dao.access.guild.settings.sub.Announcements;
import net.dv8tion.jda.api.entities.Guild;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class Settings implements GuildHolder {
    private final RepGuild repGuild;
    private AbuseProtection abuseProtection;
    private General general;
    private Reputation reputation;
    private Ranks ranks;
    private Thanking thanking;
    private Announcements announcements;
    private Autopost autopost;
    private Messages messages;
    private LogChannel logChannel;

    public Settings(RepGuild repGuild) {
        this.repGuild = repGuild;
    }

    public AbuseProtection abuseProtection() {
        if (abuseProtection != null) {
            return abuseProtection;
        }
        abuseProtection = query("""
                SELECT
                    min_messages,
                    max_message_age,
                    receiver_context,
                    donor_context,
                    cooldown,
                    cooldown_direction,
                    max_given,
                    max_given_hours,
                    max_received,
                    max_received_hours,
                    max_message_reputation
                FROM
                    abuse_protection
                WHERE guild_id = ?;
                """)
                .single(call().bind(guildId()))
                .map(rs -> AbuseProtection.build(this, rs))
                .first()
                .orElseGet(() -> new AbuseProtection(this));
        return abuseProtection;
    }

    public Announcements announcements() {
        if (announcements != null) {
            return announcements;
        }
        announcements = query("""
                SELECT
                    active,
                    same_channel,
                    channel_id
                FROM
                    announcements
                WHERE guild_id = ?;
                """)
                .single(call().bind(guildId()))
                .map(rs -> Announcements.build(this, rs))
                .first()
                .orElseGet(() -> new Announcements(this));
        return announcements;
    }

    public Reputation reputation() {
        if (reputation != null) {
            return reputation;
        }
        reputation = query("""
                SELECT
                    reactions_active,
                    answer_active,
                    mention_active,
                    fuzzy_active,
                    embed_active,
                    skip_single_embed,
                    command_active
                FROM
                    reputation_settings
                WHERE guild_id = ?;
                """)
                .single(call().bind(guildId()))
                .map(rs -> Reputation.build(this, rs))
                .first()
                .orElseGet(() -> new Reputation(this));
        return reputation;
    }

    public General general() {
        if (general != null) {
            return general;
        }
        general = query("""
                SELECT
                    language,
                    stack_roles,
                    reputation_mode,
                    reset_date,
                    system_channel_id
                FROM
                    guild_settings
                WHERE guild_id = ?;
                """)
                .single(call().bind(guildId()))
                .map(rs -> General.build(this, rs))
                .first()
                .orElseGet(() -> new General(this));
        return general;
    }

    public Thanking thanking() {
        if (thanking != null) {
            return thanking;
        }
        thanking = query("""
                SELECT
                    reaction,
                    channel_whitelist
                FROM
                    thank_settings
                WHERE guild_id = ?;
                """)
                .single(call().bind(guildId()))
                .map(rs -> Thanking.build(this, rs))
                .first()
                .orElseGet(() -> new Thanking(this));
        return thanking;
    }

    public Messages messages() {
        if (messages != null) {
            return messages;
        }
        messages = query("""
                SELECT
                    reaction_confirmation,
                    command_reputation_ephemeral
                FROM
                    message_states
                WHERE guild_id = ?;
                """)
                .single(call().bind(guildId()))
                .map(rs -> Messages.build(this, rs))
                .first()
                .orElseGet(() -> new Messages(this));
        return messages;
    }

    public Ranks ranks() {
        if (ranks != null) {
            return ranks;
        }
        ranks = new Ranks(this, general().stackRoles());
        return ranks;
    }

    public RepGuild repGuild() {
        return repGuild;
    }

    @Override
    public Guild guild() {
        return repGuild.guild();
    }

    @Override
    public long guildId() {
        return repGuild.guildId();
    }

    public Autopost autopost() {
        if (autopost != null) {
            return autopost;
        }
        autopost = query("""
                SELECT
                    active,
                    message_id,
                    channel_id,
                    refresh_type,
                    refresh_interval
                FROM
                    autopost
                WHERE guild_id = ?;
                """)
                .single(call().bind(guildId()))
                .map(rs -> Autopost.build(this, rs))
                .first()
                .orElseGet(() -> new Autopost(this));
        return autopost;
    }

    public LogChannel logChannel() {
        if (logChannel != null) {
            return logChannel;
        }
        logChannel = query("""
                SELECT
                    active,
                    channel_id
                FROM
                    log_channel
                WHERE guild_id = ?;
                """)
                .single(call().bind(guildId()))
                .map(rs -> LogChannel.build(this, rs))
                .first()
                .orElseGet(() -> new LogChannel(this));
        return logChannel;
    }
}
