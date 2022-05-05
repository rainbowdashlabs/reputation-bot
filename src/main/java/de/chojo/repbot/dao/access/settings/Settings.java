package de.chojo.repbot.dao.access.settings;

import de.chojo.jdautil.localization.util.Language;
import de.chojo.repbot.dao.access.RepGuild;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nullable;
import java.util.Optional;

public class Settings extends QueryFactoryHolder implements GuildHolder {
    private RepGuild repGuild;

    public Settings(RepGuild repGuild) {
        super(repGuild);
        this.repGuild = repGuild;
    }

    private AbuseSettings getAbuseSettings(Guild guild) {
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
                .readRow(rs -> AbuseSettings.build(this, rs))
                .firstSync()
                .orElseGet(() -> new AbuseSettings(this));

    }

    private MessageSettings getMessageSettings(Guild guild) {
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
                .readRow(rs -> MessageSettings.build(this, rs))
                .firstSync()
                .orElseGet(() -> new MessageSettings(this));
    }

    private GeneralSettings getGeneralSettings(Guild guild) {
        return builder(GeneralSettings.class)
                .query("""
                        SELECT
                            prefix,
                            emoji_debug,
                            stack_roles
                        FROM
                            guild_settings
                        WHERE guild_id = ?;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .readRow(rs -> GeneralSettings.build(this, rs))
                .firstSync()
                .orElseGet(() -> new GeneralSettings(this));

    }

    private ThankSettings getThankSettings(Guild guild) {
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
                .readRow(rs -> ThankSettings.build(this, rs))
                .firstSync()
                .orElseGet(() -> new ThankSettings(this));
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

    @Override
    public Guild guild() {
        return repGuild.guild();
    }
}
