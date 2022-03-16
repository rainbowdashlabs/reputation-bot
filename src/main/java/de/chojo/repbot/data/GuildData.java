package de.chojo.repbot.data;

import de.chojo.jdautil.localization.util.Language;
import de.chojo.repbot.data.wrapper.AbuseSettings;
import de.chojo.repbot.data.wrapper.AnnouncementSettings;
import de.chojo.repbot.data.wrapper.GeneralSettings;
import de.chojo.repbot.data.wrapper.GuildSettings;
import de.chojo.repbot.data.wrapper.MessageSettings;
import de.chojo.repbot.data.wrapper.ReputationRole;
import de.chojo.repbot.data.wrapper.ThankSettings;
import de.chojo.repbot.util.LogNotify;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.conversion.ArrayConverter;
import de.chojo.sqlutil.exceptions.ExceptionTransformer;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.slf4j.LoggerFactory.getLogger;

public class GuildData extends QueryFactoryHolder {
    private static final Logger log = getLogger(GuildData.class);

    public GuildData(DataSource dataSource) {
        super(dataSource, QueryBuilderConfig.builder().withExceptionHandler(e ->
                        log.error(LogNotify.NOTIFY_ADMIN, ExceptionTransformer.prettyException("Query execution failed", e), e))
                .build());
    }

    /**
     * Get the settings of the guild
     *
     * @param guild guild
     * @return guild settings if present
     */
    public GuildSettings getGuildSettings(Guild guild) {
        var generalSettings = getGeneralSettings(guild);
        var messageSettings = getMessageSettings(guild);
        var abuseSettings = getAbuseSettings(guild);
        var thankSettings = getThankSettings(guild);
        var announcementSettings = getAnnouncementSettings(guild);

        return new GuildSettings(guild,
                generalSettings.join().orElse(new GeneralSettings()),
                messageSettings.join().orElse(new MessageSettings()),
                abuseSettings.join().orElse(new AbuseSettings()),
                thankSettings.join().orElse(new ThankSettings()),
                announcementSettings.join().orElse(new AnnouncementSettings()));
    }

    private CompletableFuture<Optional<AnnouncementSettings>> getAnnouncementSettings(Guild guild) {
        return builder(AnnouncementSettings.class)
                .query("""
                        SELECT
                            active,
                            same_channel,
                            channel_id
                        FROM
                            announcements
                        WHERE guild_id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .readRow(this::buildAnnounceSettings)
                .first();
    }

    private CompletableFuture<Optional<AbuseSettings>> getAbuseSettings(Guild guild) {
        return builder(AbuseSettings.class)
                .query("""
                        SELECT
                            min_messages,
                            max_message_age,
                            receiver_context,
                            donor_context,
                            cooldown
                        FROM
                            abuse_protection
                        WHERE guild_id = ?;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .readRow(this::buildAbuseSettings)
                .first();

    }

    private CompletableFuture<Optional<GeneralSettings>> getGeneralSettings(Guild guild) {
        return builder(GeneralSettings.class)
                .query("""
                        SELECT
                            prefix,
                            emoji_debug,
                            manager_role,
                            stack_roles
                        FROM
                            guild_settings
                        WHERE guild_id = ?;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .readRow(this::buildGeneralSettings)
                .first();

    }

    private CompletableFuture<Optional<MessageSettings>> getMessageSettings(Guild guild) {
        return builder(MessageSettings.class)
                .query("""
                        SELECT
                            reactions_active,
                            answer_active,
                            mention_active,
                            fuzzy_active,
                            embed_active
                        FROM
                            message_settings
                        WHERE guild_id = ?;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .readRow(this::buildMessageSettings)
                .first();
    }

    private CompletableFuture<Optional<ThankSettings>> getThankSettings(Guild guild) {
        return builder(ThankSettings.class)
                .query("""
                        SELECT
                            reaction,
                            reactions,
                            thankswords,
                            active_channels,
                            channel_whitelist,
                            receiver_roles,
                            donor_roles
                        FROM
                            get_thank_settings(?);
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .readRow(this::buildThankSettings)
                .first();
    }

    private AnnouncementSettings buildAnnounceSettings(ResultSet rs) throws SQLException {
        return new AnnouncementSettings(rs.getBoolean("active"), rs.getBoolean("same_channel"), rs.getLong("channel_id"));
    }

    private AbuseSettings buildAbuseSettings(ResultSet rs) throws SQLException {
        return new AbuseSettings(
                rs.getInt("cooldown"),
                rs.getInt("max_message_age"),
                rs.getInt("min_messages"),
                rs.getBoolean("donor_context"),
                rs.getBoolean("receiver_context"));
    }

    private GeneralSettings buildGeneralSettings(ResultSet rs) throws SQLException {
        return new GeneralSettings(
                rs.getString("prefix"),
                rs.getBoolean("emoji_debug"),
                rs.getLong("manager_role"),
                rs.getBoolean("stack_roles"));
    }

    private MessageSettings buildMessageSettings(ResultSet rs) throws SQLException {
        return new MessageSettings(
                rs.getBoolean("reactions_active"),
                rs.getBoolean("answer_active"),
                rs.getBoolean("mention_active"),
                rs.getBoolean("fuzzy_active"),
                rs.getBoolean("embed_active"));
    }

    private ThankSettings buildThankSettings(ResultSet row) throws SQLException {
        return new ThankSettings(
                row.getString("reaction"),
                ArrayConverter.toArray(row, "reactions", new String[0]),
                ArrayConverter.toArray(row, "thankswords", new String[0]),
                ArrayConverter.toArray(row, "active_channels", new Long[0]),
                row.getBoolean("channel_whitelist"),
                ArrayConverter.toArray(row, "donor_roles", new Long[0]),
                ArrayConverter.toArray(row, "receiver_roles", new Long[0])
        );
    }

    /**
     * Get the prefix of the guild.
     *
     * @param guild guild
     * @return prefix if set
     */
    public Optional<String> getPrefix(Guild guild) {
        return builder(String.class).query("SELECT prefix FROM guild_settings WHERE guild_id = ?;")
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
                                   guild_settings(guild_id, prefix) VALUES (?,?)
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
                .query("SELECT language FROM guild_settings WHERE guild_id = ?;")
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
                                   guild_settings(guild_id, language) VALUES (?,?)
                                   ON CONFLICT(guild_id)
                                       DO UPDATE
                                           SET language = excluded.language;
                               """)
                       .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setString(language == null ? null : language.getCode()))
                       .update().executeSync() > 0;
    }

    /**
     * Set if emoji debug is enabled for a guild
     *
     * @param guild      guild
     * @param emojiDebug set to true to enable debug
     * @return true if the emoji debug state was changed
     */
    public boolean setEmojiDebug(Guild guild, boolean emojiDebug) {
        return builder()
                       .query("""
                               INSERT INTO
                                   guild_settings(guild_id, emoji_debug) VALUES (?,?)
                                   ON CONFLICT(guild_id)
                                       DO UPDATE
                                           SET emoji_debug = excluded.emoji_debug;
                               """)
                       .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setBoolean(emojiDebug))
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
    public boolean removeChannel(Guild guild, Channel channel) {
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

    public List<ReputationRole> getCurrentReputationRole(Guild guild, long reputation, boolean stack) {
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
                        LIMIT %s;
                        """, stack ? "ALL" : "1")
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(reputation))
                .readRow(this::buildRole).allSync();
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

    public void addDonorRole(Guild guild, Role role) {
        builder().query("INSERT INTO donor_roles(guild_id, role_id) VALUES (?,?) ON CONFLICT DO NOTHING")
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(role.getIdLong()))
                .update().executeSync();
    }

    public void addReceiverRole(Guild guild, Role role) {
        builder().query("INSERT INTO receiver_roles(guild_id, role_id) VALUES (?,?) ON CONFLICT DO NOTHING")
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(role.getIdLong()))
                .update().executeSync();
    }

    public void removeDonorRole(Guild guild, Role role) {
        builder().query("DELETE FROM donor_roles WHERE guild_id = ? AND role_id = ?")
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(role.getIdLong()))
                .update().executeSync();
    }

    public void removeReceiverRole(Guild guild, Role role) {
        builder().query("DELETE FROM receiver_roles WHERE guild_id = ? AND role_id = ?")
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(role.getIdLong()))
                .update().executeSync();
    }


    public boolean updateMessageSettings(Guild guild, MessageSettings settings) {
        return builder()
                       .query("""
                               UPDATE
                                   message_settings
                               SET reactions_active = ?,
                                   answer_active = ?,
                                   mention_active = ?,
                                   fuzzy_active = ?,
                                   embed_active = ?
                                WHERE guild_id = ?;
                               """)
                       .paramsBuilder(stmt -> stmt
                               .setBoolean(settings.isReactionActive()).setBoolean(settings.isAnswerActive())
                               .setBoolean(settings.isMentionActive()).setBoolean(settings.isFuzzyActive())
                               .setBoolean(settings.isEmbedActive()).setLong(guild.getIdLong()))
                       .update().executeSync() > 0;
    }

    public boolean updateAbuseSettings(Guild guild, AbuseSettings update) {
        return builder()
                       .query("""
                               UPDATE
                                   abuse_protection
                               SET max_message_age = ?,
                                   min_messages = ?,
                                   cooldown = ?,
                                   receiver_context = ?,
                                   donor_context = ?
                               WHERE guild_id = ?;
                               """)
                       .paramsBuilder(stmt -> stmt.setInt(update.maxMessageAge()).setInt(update.minMessages())
                               .setInt(update.cooldown()).setBoolean(update.isDonorContext())
                               .setBoolean(update.isReceiverContext()).setLong(guild.getIdLong()))
                       .update().executeSync() > 0;
    }

    public boolean updateAnnouncementSettings(Guild guild, AnnouncementSettings update) {
        return builder()
                       .query("""
                               UPDATE
                                   announcements
                               SET active = ?,
                                   same_channel = ?,
                                   channel_id = ?
                               WHERE guild_id = ?;
                               """)
                       .paramsBuilder(stmt -> stmt.setBoolean(update.isActive()).setBoolean(update.isSameChannel())
                               .setLong(update.channelId()).setLong(guild.getIdLong()))
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
                .append()
                .query("INSERT INTO abuse_protection(guild_id) VALUES (?) ON CONFLICT DO NOTHING;")
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .append()
                .query("INSERT INTO guild_settings(guild_id) VALUES(?) ON CONFLICT DO NOTHING")
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .append()
                .query("INSERT INTO thank_settings(guild_id) VALUES(?) ON CONFLICT DO NOTHING")
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .update().executeSync();
    }

    public boolean setManagerRole(Guild guild, Role role) {
        return builder()
                       .query("""
                               INSERT INTO
                                   guild_settings(guild_id, manager_role) VALUES (?,?)
                                   ON CONFLICT(guild_id)
                                       DO UPDATE
                                           SET manager_role = excluded.manager_role;
                               """)
                       .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setLong(role.getIdLong()))
                       .update().executeSync() > 0;
    }

    public boolean setRoleStacking(Guild guild, boolean state) {
        return builder()
                       .query("""
                               INSERT INTO
                                   guild_settings(guild_id, stack_roles) VALUES (?,?)
                                   ON CONFLICT(guild_id)
                                       DO UPDATE
                                           SET stack_roles = excluded.stack_roles;
                               """)
                       .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setBoolean(state))
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
                        INSERT INTO thank_settings(guild_id, channel_whitelist) VALUES (?,?)
                            ON CONFLICT(guild_id)
                                DO UPDATE
                                    SET channel_whitelist = excluded.channel_whitelist
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setBoolean(whitelist))
                .update()
                .executeSync();
    }

    public boolean setMainReaction(Guild guild, String reaction) {
        return builder().query("""
                        INSERT INTO thank_settings(guild_id, reaction) VALUES (?,?)
                            ON CONFLICT(guild_id)
                                DO UPDATE
                                    SET reaction = excluded.reaction
                        """)
                       .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setString(reaction))
                       .update()
                       .executeSync() > 0;
    }

    public void selfCleanupPrompt(Guild guild) {
        builder().query("""
                        INSERT INTO self_cleanup(guild_id) VALUES(?)
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .update().executeSync();
    }

    public Optional<LocalDateTime> getCleanupPromptTime(Guild guild) {
        return builder(LocalDateTime.class)
                .query("""
                        SELECT prompted FROM self_cleanup WHERE guild_id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .readRow(rs -> rs.getTimestamp("prompted").toLocalDateTime())
                .firstSync();
    }

    public List<Long> getCleanupList() {
        return builder(Long.class)
                .queryWithoutParams("""
                                    SELECT guild_id FROM self_cleanup;
                        """)
                .readRow(stmt -> stmt.getLong("guild_id"))
                .allSync();
    }

    public void cleanupDone(Guild guild) {
        builder(Boolean.class).query("""
                                DELETE FROM self_cleanup WHERE guild_id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .update().executeSync();
    }

    public void promptMigration(Guild guild) {
        builder().query("""
                        INSERT INTO migrations(guild_id) VALUES(?) ON CONFLICT DO NOTHING
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .update().executeSync();
    }

    public boolean migrationActive(Guild guild) {
        return builder(Boolean.class).query("""
                        SELECT EXISTS(SELECT 1 FROM migrations WHERE guild_id = ?) AS exists
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .readRow(rs -> rs.getBoolean("exists"))
                .firstSync().get();
    }

    public int getActiveMigrations(int days) {
        return builder(Integer.class).query("""
                        SELECT COUNT(1) FROM migrations WHERE prompted > NOW() - ?::interval
                        """)
                .paramsBuilder(stmt -> stmt.setString(days + "days"))
                .readRow(rs -> rs.getInt("count"))
                .firstSync().get();
    }

    public void migrated(Guild guild) {
        builder(Integer.class).query("""
                        UPDATE migrations SET migrated = NOW() WHERE guild_id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .update().execute();
    }
}
