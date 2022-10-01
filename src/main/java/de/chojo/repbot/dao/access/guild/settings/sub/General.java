package de.chojo.repbot.dao.access.guild.settings.sub;

import de.chojo.jdautil.consumer.ThrowingConsumer;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sadu.base.QueryFactory;
import de.chojo.sadu.wrapper.util.ParamBuilder;
import de.chojo.sadu.wrapper.util.Row;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.Nullable;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class General extends QueryFactory implements GuildHolder {
    private final AtomicBoolean stackRoles;
    private final Settings settings;
    private DiscordLocale language;
    private boolean emojiDebug;
    private ReputationMode reputationMode;
    private LocalDate resetDate;

    public General(Settings settings) {
        this(settings, null, true, false, ReputationMode.TOTAL, null);
    }

    public General(Settings settings, DiscordLocale language, boolean emojiDebug, boolean stackRoles, ReputationMode reputationMode, LocalDate resetDate) {
        super(settings);
        this.settings = settings;
        this.language = language;
        this.emojiDebug = emojiDebug;
        this.stackRoles = new AtomicBoolean(stackRoles);
        this.reputationMode = reputationMode;
        this.resetDate = resetDate;
    }

    public static General build(Settings settings, Row rs) throws SQLException {
        var lang = rs.getString("language");
        return new General(settings,
                lang == null ? null : DiscordLocale.from(lang),
                rs.getBoolean("emoji_debug"),
                rs.getBoolean("stack_roles"),
                ReputationMode.valueOf(rs.getString("reputation_mode")),
                Optional.ofNullable(rs.getDate("reset_date")).map(Date::toLocalDate).orElse(null));
    }

    public boolean language(@Nullable DiscordLocale language) {
        var result = set("language", stmt -> stmt.setString(language == null ? null : language.getLocale()));
        if (result) {
            this.language = language;
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

    public ReputationMode reputationMode(ReputationMode reputationMode) {
        var result = set("reputation_mode", stmt -> stmt.setString(reputationMode.name()));
        if (result) {
            this.reputationMode = reputationMode;
        }
        return reputationMode;
    }

    public boolean stackRoles(boolean stackRoles) {
        var result = set("stack_roles", stmt -> stmt.setBoolean(stackRoles));
        if (result) {
            this.stackRoles.set(stackRoles);
        }
        return result;
    }

    public boolean resetDate(LocalDate resetDate) {
            boolean result;
        if (resetDate == null) {
            result = set("reset_date", stmt -> stmt.setDate(null));
        } else {
            result = set("reset_date", stmt -> stmt.setDate(Date.valueOf(resetDate)));
        }
        if (result) {
            this.resetDate = resetDate;
        }
        return result;
    }

    public Optional<DiscordLocale> language() {
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

    public LocalDate resetDate() {
        return resetDate;
    }

    @Override
    public Guild guild() {
        return settings.guild();
    }

    @Override
    public long guildId() {
        return settings.guildId();
    }

    private boolean set(String parameter, ThrowingConsumer<ParamBuilder, SQLException> builder) {
        return builder()
                .query("""
                       INSERT INTO guild_settings(guild_id, %s) VALUES (?, ?)
                       ON CONFLICT(guild_id)
                           DO UPDATE SET %s = excluded.%s;
                       """, parameter, parameter, parameter)
                .parameter(stmts -> {
                    stmts.setLong(guildId());
                    builder.accept(stmts);
                }).insert()
                .sendSync()
                .changed();
    }

    public ReputationMode reputationMode() {
        return reputationMode;
    }

    public String prettyString() {
        return """
               Stack roles: %s
               Emoji Debug: %s
               Language: %s
               Reputation Mode: %s
               """.stripIndent()
                  .formatted(stackRoles.get(), emojiDebug, language.getLanguageName(), reputationMode.name());
    }
}
