package de.chojo.repbot.data;

import de.chojo.jdautil.database.QueryObject;
import de.chojo.jdautil.database.builder.QueryBuilderConfig;
import de.chojo.jdautil.database.builder.QueryBuilderFactory;
import de.chojo.jdautil.database.builder.stage.ResultStage;
import de.chojo.jdautil.localization.util.Language;
import de.chojo.repbot.data.util.DbUtil;
import de.chojo.repbot.data.wrapper.GuildSettingUpdate;
import de.chojo.repbot.data.wrapper.GuildSettings;
import de.chojo.repbot.data.wrapper.RemovalTask;
import de.chojo.repbot.data.wrapper.ReputationRole;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class GuildData extends QueryObject {
    private final QueryBuilderFactory factory;
    private static final Logger log = getLogger(GuildData.class);

    public GuildData(DataSource source) {
        super(source);
        factory = new QueryBuilderFactory(QueryBuilderConfig.builder().build(), source);
    }

    /**
     * Get the settings of the guild
     *
     * @param guild guild
     * @return guild settings if present
     */
    public Optional<GuildSettings> getGuildSettings(Guild guild) {
        return factory.builder(GuildSettings.class)
                .query("""
                        SELECT
                            prefix,
                            thankswords,
                            max_message_age,
                            min_messages,
                            reaction,
                            reactions_active,
                            answer_active,
                            mention_active,
                            fuzzy_active,
                            active_channels,
                            cooldown,
                            manager_role
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
                        row.getLong("manager_role")))
                .firstSync();
    }

    /**
     * Get the prefix of the guild.
     *
     * @param guild guild
     * @return prefix if set
     */
    public Optional<String> getPrefix(Guild guild) {
        return factory.builder(String.class).query("SELECT prefix FROM guild_bot_settings where guild_id = ?;")
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
        return factory.builder()
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
        return factory.builder(String.class)
                .query("SELECT language FROM guild_bot_settings where guild_id = ?;")
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
        return factory.builder()
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
        return factory.builder()
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
        return factory.builder()
                .query("DELETE FROM active_channel where guild_id = ? and channel_id = ?;")
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
        return factory.builder()
                .query("DELETE FROM active_channel where guild_id = ?;")
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
        return factory.builder()
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
        return factory.builder()
                .query("DELETE FROM guild_ranks where guild_id =? and role_id = ?;")
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(role.getIdLong()))
                .update().executeSync() > 0;
    }

    public Optional<ReputationRole> getCurrentReputationRole(Guild guild, long reputation) {
        return factory.builder(ReputationRole.class)
                .query("""
                        SELECT
                            role_id,
                            reputation
                        from
                            guild_ranks
                        where guild_id = ?
                            AND reputation <= ?
                        order by reputation DESC
                        limit 1;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(reputation))
                .readRow(this::buildRole).firstSync();
    }

    public Optional<ReputationRole> getNextReputationRole(Guild guild, long reputation) {
        return factory.builder(ReputationRole.class)
                .query("""
                        SELECT
                            role_id,
                            reputation
                        from
                            guild_ranks
                        where guild_id = ?
                            AND reputation > ?
                        order by reputation
                        limit 1;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(reputation))
                .readRow(this::buildRole).firstSync();
    }

    public List<ReputationRole> getReputationRoles(Guild guild) {
        return factory.builder(ReputationRole.class)
                .query("""
                        SELECT
                            role_id,
                            reputation
                        from
                            guild_ranks
                        where guild_id = ?
                        order by reputation;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .readRow(this::buildRole).allSync();
    }

    public boolean updateMessageSettings(GuildSettingUpdate update) {
        return factory.builder()
                .query("""
                        UPDATE
                            message_settings
                        SET max_message_age = coalesce(?, max_message_age),
                            min_messages = coalesce(?, min_messages),
                            reaction = coalesce(?, reaction),
                            reactions_active = coalesce(?, reactions_active),
                            answer_active = coalesce(?, answer_active),
                            mention_active = coalesce(?, mention_active),
                            fuzzy_active = coalesce(?, fuzzy_active),
                            cooldown = coalesce(?, cooldown)
                        where guild_id = ?;
                        """)
                .paramsBuilder(stmt -> stmt.setInt(update.maxMessageAge()).setString(update.reaction())
                        .setBoolean(update.reactionsActive()).setBoolean(update.answerActive())
                        .setBoolean(update.mentionActive()).setBoolean(update.fuzzyActive()).setInt(update.cooldown())
                        .setLong(update.guild().getIdLong()))
                .update().executeSync() > 0;
    }

    public boolean addThankWord(Guild guild, String pattern) {
        return factory.builder()
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
        return factory.builder()
                .query("""
                        DELETE FROM
                            thankwords
                        WHERE
                            guild_id = ?
                            and thankword = ?
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
        factory.builder()
                .query("INSERT INTO message_settings(guild_id) VALUES (?) ON CONFLICT DO NOTHING;")
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .update().executeSync();
    }

    public boolean setManagerRole(Guild guild, Role role) {
        return factory.builder()
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

    public void queueDeletion(Guild guild) {
        factory.builder()
                .query("""
                        INSERT INTO
                            cleanup_schedule(guild_id)
                            VALUES (?)
                                ON CONFLICT(guild_id, user_id)
                                    DO NOTHING;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .update().executeSync();
    }

    public void queueDeletion(User user, Guild guild) {
        factory.builder()
                .query("""
                        INSERT INTO
                            cleanup_schedule(guild_id, user_id)
                            VALUES (?,?)
                                ON CONFLICT(guild_id, user_id)
                                    DO NOTHING;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(user.getIdLong()))
                .update().executeSync();
    }

    public void dequeueDeletion(Guild guild) {
        factory.builder()
                .query("DELETE FROM cleanup_schedule where guild_id = ?;")
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .update().executeSync();
    }

    public void dequeueDeletion(Member member) {
        factory.builder()
                .query("""
                        DELETE FROM
                            cleanup_schedule
                        where guild_id = ?
                            AND user_id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(member.getGuild().getIdLong()).setLong(member.getIdLong()))
                .update().executeSync();
    }

    public List<RemovalTask> getRemovalTasks() {
        return factory.builder(RemovalTask.class)
                .queryWithoutParams("""
                        SELECT
                            task_id,
                            user_id,
                            guild_id
                        from
                            cleanup_schedule
                        where delete_after < now();
                        """)
                .readRow(rs -> new RemovalTask(rs.getLong("task_id"), rs.getLong("guild_id"), rs.getLong("user_id")))
                .allSync();
    }

    public void executeRemovalTask(RemovalTask task) {
        ResultStage<Void> builder;
        if (task.userId() == null) {
            builder = factory.builder().query("DELETE FROM reputation_log where guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()))
                    .append().query("DELETE FROM guild_bot_settings where guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()))
                    .append().query("DELETE FROM active_channel where guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()))
                    .append().query("DELETE FROM message_settings where guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()))
                    .append().query("DELETE FROM guild_ranks where guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()))
                    .append().query("DELETE FROM thankwords where guild_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()));
            log.info("Removed guild settings for {}", task.guildId());
        } else {
            builder = factory.builder().query("DELETE FROM reputation_log where guild_id = ? AND receiver_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()).setLong(task.userId()))
                    .append().query("UPDATE reputation_log SET donor_id = 0 where guild_id = ? AND donor_id = ?;")
                    .paramsBuilder(stmt -> stmt.setLong(task.guildId()).setLong(task.userId()));
            log.info("Removed user reputation from guild {} of user {}", task.guildId(), task.userId());
        }

        builder.append().query("DELETE FROM cleanup_schedule where task_id = ?;")
                .params(stmt -> stmt.setLong(1, task.taskId()))
                .update().executeSync();
    }
}