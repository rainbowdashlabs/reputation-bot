package de.chojo.repbot.data;

import de.chojo.jdautil.localization.util.Language;
import de.chojo.repbot.data.util.DbUtil;
import de.chojo.repbot.data.wrapper.GuildSettingUpdate;
import de.chojo.repbot.data.wrapper.GuildSettings;
import de.chojo.repbot.data.wrapper.ReputationRole;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class GuildData extends QueryFactoryHolder {
    public GuildData(DataSource source) {
        super(source, QueryBuilderConfig.builder().build());
    }

    /**
     * Get the settings of the guild
     *
     * @param guild guild
     * @return guild settings if present
     */
    public Optional<GuildSettings> getGuildSettings(Guild guild) {
        return builder(GuildSettings.class)
                .query("""
                        SELECT
                            prefix,
                            thankswords,
                            max_message_age,
                            min_messages,
                            reaction,
                            reactions,
                            reactions_active,
                            answer_active,
                            mention_active,
                            fuzzy_active,
                            active_channels,
                            cooldown,
                            manager_role,
                            channel_whitelist
                        FROM
                            guild_settings
                        WHERE
                            guild_id = ?;
                        """)
                .params(stmt -> stmt.setLong(1, guild.getIdLong()))
                .readRow(row -> new GuildSettings(guild,
                        row.getString("prefix"),
                        DbUtil.arrayToArray(row, "thankswords", new String[0]),
                        row.getInt("max_message_age"),
                        row.getInt("min_messages"),
                        row.getString("reaction"),
                        row.getBoolean("reactions_active"),
                        row.getBoolean("answer_active"),
                        row.getBoolean("mention_active"),
                        row.getBoolean("fuzzy_active"),
                        DbUtil.arrayToArray(row, "active_channels", new Long[0]),
                        row.getInt("cooldown"),
                        row.getLong("manager_role"),
                        DbUtil.arrayToArray(row, "reactions", new String[0]),
                        row.getBoolean("channel_whitelist")))
                .firstSync();
    }

    /**
     * Get the prefix of the guild.
     *
     * @param guild guild
     * @return prefix if set
     */
    public Optional<String> getPrefix(Guild guild) {
        return builder(String.class).query("SELECT prefix FROM guild_bot_settings WHERE guild_id = ?;")
                .params(stmt -> stmt.setLong(1, guild.getIdLong()))
                .readRow(row -> row.getString(1))
                .firstSync();
    }

    /**
     * Set the prefix for a guild.
     *
     * @param guild  guild
     * @param prefix prefix. may be null
     * @return true if prefix was changed
     */
    public boolean setPrefix(Guild guild, @Nullable String prefix) {
        return builder()
                .query("""
                        INSERT INTO
                            guild_bot_settings(guild_id, prefix) VALUES (?,?)
                            ON CONFLICT(guild_id)
                                DO UPDATE
                                    SET prefix = excluded.prefix;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setString(prefix))
                .update().executeSync() > 0;
    }

    /**
     * Get the language of the guild if set.
     *
     * @param guild guild
     * @return language as string if set
     */
    public Optional<String> getLanguage(Guild guild) {
        return builder(String.class)
                .query("SELECT language FROM guild_bot_settings WHERE guild_id = ?;")
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .readRow(rs -> rs.getString(1))
                .firstSync();
    }

    /**
     * Set the language for a guild
     *
     * @param guild    guild
     * @param language language. May be null
     * @return true if the language was changed
     */
    public boolean setLanguage(Guild guild, @Nullable Language language) {
        return builder()
                .query("""
                        INSERT INTO
                            guild_bot_settings(guild_id, language) VALUES (?,?)
                            ON CONFLICT(guild_id)
                                DO UPDATE
                                    SET language = excluded.language;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setString(language == null ? null : language.getCode()))
                .update().executeSync() > 0;
    }

    /**
     * Add a channel to reputation channel
     *
     * @param guild   guild
     * @param channel channel
     * @return true if a channel was added
     */
    public boolean addChannel(Guild guild, MessageChannel channel) {
        return builder()
                .query("INSERT INTO active_channel(guild_id, channel_id) VALUES(?,?) ON CONFLICT DO NOTHING;")
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(channel.getIdLong()))
                .update().executeSync() > 0;
    }

    /**
     * Remove a reputation channel
     *
     * @param guild   guild
     * @param channel channel
     * @return true if the channel was removed
     */
    public boolean removeChannel(Guild guild, MessageChannel channel) {
        return builder()
                .query("DELETE FROM active_channel WHERE guild_id = ? AND channel_id = ?;")
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(channel.getIdLong()))
                .update().executeSync() > 0;
    }

    /**
     * Remove all channel of a guild
     *
     * @param guild guild
     * @return the amount of removed channel
     */
    public int clearChannel(Guild guild) {
        return builder()
                .query("DELETE FROM active_channel WHERE guild_id = ?;")
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .update().executeSync();
    }

    /**
     * Add a reputation role.
     * <p>
     * If the role or the reputation amount is already in use it will be removed first.
     *
     * @param guild      guild
     * @param role       role
     * @param reputation required reputation of role
     * @return true if the role was added or updated
     */
    public boolean addReputationRole(Guild guild, Role role, long reputation) {
        return builder()
                .query("""
                        DELETE FROM
                            guild_ranks
                        WHERE
                            guild_id = ?
                                AND (role_id = ?
                                    OR reputation = ?);
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(role.getIdLong()).setLong(reputation))
                .append()
                .query("""
                        INSERT INTO guild_ranks(guild_id, role_id, reputation) VALUES(?,?,?)
                            ON CONFLICT(guild_id, role_id)
                                DO UPDATE
                                    SET reputation = excluded.reputation,
                                        role_id = excluded.role_id;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(role.getIdLong()).setLong(reputation))
                .update().executeSync() > 0;
    }

    /**
     * Remove a reputation role.
     *
     * @param guild guild
     * @param role  role
     * @return true
     */
    public boolean removeReputationRole(Guild guild, Role role) {
        return builder()
                .query("DELETE FROM guild_ranks WHERE guild_id = ? AND role_id = ?;")
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(role.getIdLong()))
                .update().executeSync() > 0;
    }

    public Optional<ReputationRole> getCurrentReputationRole(Guild guild, long reputation) {
        return builder(ReputationRole.class)
                .query("""
                        SELECT
                            role_id,
                            reputation
                        FROM
                            guild_ranks
                        WHERE guild_id = ?
                            AND reputation <= ?
                        ORDER BY reputation DESC
                        LIMIT 1;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(reputation))
                .readRow(this::buildRole).firstSync();
    }

    public Optional<ReputationRole> getNextReputationRole(Guild guild, long reputation) {
        return builder(ReputationRole.class)
                .query("""
                        SELECT
                            role_id,
                            reputation
                        FROM
                            guild_ranks
                        WHERE guild_id = ?
                            AND reputation > ?
                        ORDER BY reputation
                        LIMIT 1;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(reputation))
                .readRow(this::buildRole).firstSync();
    }

    public List<ReputationRole> getReputationRoles(Guild guild) {
        return builder(ReputationRole.class)
                .query("""
                        SELECT
                            role_id,
                            reputation
                        FROM
                            guild_ranks
                        WHERE guild_id = ?
                        ORDER BY reputation;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .readRow(this::buildRole).allSync();
    }

    public boolean updateMessageSettings(GuildSettingUpdate update) {
        return builder()
                .query("""
                        UPDATE
                            message_settings
                        SET max_message_age = COALESCE(?, max_message_age),
                            min_messages = COALESCE(?, min_messages),
                            reaction = COALESCE(?, reaction),
                            reactions_active = COALESCE(?, reactions_active),
                            answer_active = COALESCE(?, answer_active),
                            mention_active = COALESCE(?, mention_active),
                            fuzzy_active = COALESCE(?, fuzzy_active),
                            cooldown = COALESCE(?, cooldown)
                        WHERE guild_id = ?;
                        """)
                .paramsBuilder(stmt -> stmt.setInt(update.maxMessageAge()).setInt(update.minMessages()).setString(update.reaction())
                        .setBoolean(update.reactionsActive()).setBoolean(update.answerActive())
                        .setBoolean(update.mentionActive()).setBoolean(update.fuzzyActive()).setInt(update.cooldown())
                        .setLong(update.guild().getIdLong()))
                .update().executeSync() > 0;
    }

    public boolean addThankWord(Guild guild, String pattern) {
        return builder()
                .query("""
                        INSERT INTO
                            thankwords(guild_id, thankword) VALUES(?,?)
                                ON CONFLICT
                                    DO NOTHING;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setString(pattern))
                .update().executeSync() > 0;
    }

    public boolean removeThankWord(Guild guild, String pattern) {
        return builder()
                .query("""
                        DELETE FROM
                            thankwords
                        WHERE
                            guild_id = ?
                            AND thankword = ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setString(pattern))
                .update().executeSync() > 0;
    }

    private ReputationRole buildRole(ResultSet rs) throws SQLException {
        return new ReputationRole(
                rs.getLong("role_id"),
                rs.getLong("reputation")
        );
    }

    public void initGuild(Guild guild) {
        builder()
                .query("INSERT INTO message_settings(guild_id) VALUES (?) ON CONFLICT DO NOTHING;")
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .update().executeSync();
    }

    public boolean setManagerRole(Guild guild, Role role) {
        return builder()
                .query("""
                        INSERT INTO
                            guild_bot_settings(guild_id, manager_role) VALUES (?,?)
                            ON CONFLICT(guild_id)
                                DO UPDATE
                                    SET manager_role = excluded.manager_role;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(role.getIdLong()))
                .update().executeSync() > 0;
    }

    public List<Long> guildUserIds(Guild guild) {
        return builder(Long.class)
                .query("""
                        SELECT
                        	user_id AS user_id
                        FROM
                        	(
                        		SELECT
                        			donor_id AS user_id
                        		FROM
                        			reputation_log
                        		WHERE guild_id = ?
                        		UNION
                        		DISTINCT
                        		SELECT
                        			receiver_id AS user_id
                        		FROM
                        			reputation_log
                        		WHERE guild_id = ?
                        	) users
                        WHERE user_id != 0
                         """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(guild.getIdLong()))
                .readRow(rs -> rs.getLong("user_id"))
                .allSync();
    }

    public boolean addReaction(Guild guild, String reaction) {
        builder().query("""
                INSERT INTO guild_reactions(guild_id, reaction) VALUES (?,?)
                    ON CONFLICT(guild_id, reaction)
                        DO NOTHING;
                """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setString(reaction))
                .update()
                .executeSync();
        return true;
    }

    public boolean removeReaction(Guild guild, String reaction) {
        return builder().query("""
                DELETE FROM guild_reactions WHERE guild_id = ? AND reaction = ?;
                """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setString(reaction))
                .update()
                .executeSync() > 0;
    }

    public void setChannelListType(Guild guild, boolean whitelist) {
        builder().query("""
                INSERT INTO guild_bot_settings(guild_id, channel_whitelist) VALUES (?,?)
                    ON CONFLICT(guild_id)
                        DO UPDATE
                            SET channel_whitelist = excluded.channel_whitelist
                """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setBoolean(whitelist))
                .update()
                .executeSync();
    }
}
