package de.chojo.repbot.dao.access.guild.settings.sub;

import de.chojo.jdautil.consumer.ThrowingConsumer;
import de.chojo.jdautil.localization.util.Language;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.wrapper.ParamBuilder;
import de.chojo.sqlutil.wrapper.QueryBuilderFactory;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class General extends QueryFactoryHolder implements GuildHolder {
    private final AtomicBoolean stackRoles;
    private String language;
    private boolean emojiDebug;
    private final Settings settings;

    public General(Settings settings) {
        this(settings, null, true, false);
    }

    public General(Settings settings, String language, boolean emojiDebug, boolean stackRoles) {
        super(settings);
        this.settings = settings;
        this.language = language;
        this.emojiDebug = emojiDebug;
        this.stackRoles = new AtomicBoolean(stackRoles);
    }

    public static General build(Settings settings, ResultSet rs) throws SQLException {
        return new General(settings,
                rs.getString("language"),
                rs.getBoolean("emoji_debug"),
                rs.getBoolean("stack_roles"));
    }

    public boolean language(Language language) {
        var result = set("language", stmt -> stmt.setString(language.getCode()));
        if (result) {
            this.language = language.getCode();
        }
        return result;
    }

    public boolean emojiDebug(boolean emojiDebug) {
        var result = set("emoji_debug", stmt -> stmt.setBoolean(emojiDebug));
        if (result) {
            this.emojiDebug = emojiDebug;
        }
        return result;
    }

    public boolean stackRoles(boolean stackRoles) {
        var result = set("stack_roles", stmt -> stmt.setBoolean(stackRoles));
        if (result) {
            this.stackRoles.set(stackRoles);
        }
        return result;
    }

    public Optional<String> language() {
        return Optional.ofNullable(language);
    }

    public boolean isEmojiDebug() {
        return emojiDebug;
    }

    public boolean isStackRoles() {
        return stackRoles.get();
    }

    public AtomicBoolean stackRoles() {
        return stackRoles;
    }

    @Override
    public Guild guild() {
        return settings.guild();
    }

    private boolean set(String parameter, ThrowingConsumer<ParamBuilder, SQLException> builder) {
        return builder()
                       .query("""
                               INSERT INTO guild_settings(guild_id, %s) VALUES (?, ?)
                               ON CONFLICT(guild_id)
                                   DO UPDATE SET %s = excluded.%s;
                               """, parameter, parameter, parameter)
                       .paramsBuilder(stmts -> {
                           stmts.setLong(guildId());
                           builder.accept(stmts);
                       }).insert()
                       .executeSync() > 0;
    }
}
