package de.chojo.repbot.dao.access.guild.settings.sub;

import de.chojo.jdautil.consumer.ThrowingConsumer;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.data.wrapper.GuildSettings;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.wrapper.ParamBuilder;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.PropertyKey;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class Messages extends QueryFactoryHolder implements GuildHolder {
    private Settings settings;
    private boolean reactionActive;
    private boolean answerActive;
    private boolean mentionActive;
    private boolean fuzzyActive;
    private boolean embedActive;

    public Messages(Settings settings) {
        this(settings, true, true, true, true, true);
    }

    public Messages(Settings settings, boolean reactionActive, boolean answerActive, boolean mentionActive, boolean fuzzyActive, boolean embedActive) {
        super(settings);
        this.settings = settings;
        this.reactionActive = reactionActive;
        this.answerActive = answerActive;
        this.mentionActive = mentionActive;
        this.fuzzyActive = fuzzyActive;
        this.embedActive = embedActive;
    }

    public static Messages build(Settings settings, ResultSet rs) throws SQLException {
        return new Messages(settings,
                rs.getBoolean("reactions_active"),
                rs.getBoolean("answer_active"),
                rs.getBoolean("mention_active"),
                rs.getBoolean("fuzzy_active"),
                rs.getBoolean("embed_active"));
    }

    public boolean isReactionActive() {
        return reactionActive;
    }

    public boolean isAnswerActive() {
        return answerActive;
    }

    public boolean isMentionActive() {
        return mentionActive;
    }

    public boolean isFuzzyActive() {
        return fuzzyActive;
    }

    public boolean isEmbedActive() {
        return embedActive;
    }

    public void embedActive(boolean embedActive) {

        this.embedActive = embedActive;
    }

    public void reactionActive(boolean reactionActive) {
        this.reactionActive = reactionActive;
    }

    public void answerActive(boolean answerActive) {
        this.answerActive = answerActive;
    }

    public void mentionActive(boolean mentionActive) {
        this.mentionActive = mentionActive;
    }

    public void fuzzyActive(boolean fuzzyActive) {
        this.fuzzyActive = fuzzyActive;
    }

    public String toLocalizedString(GuildSettings guildSettings) {
        var setting = List.of(
                getSetting("command.repSettings.embed.descr.byReaction", isReactionActive()),
                getSetting("command.repSettings.embed.descr.byAnswer", isAnswerActive()),
                getSetting("command.repSettings.embed.descr.byMention", isMentionActive()),
                getSetting("command.repSettings.embed.descr.byFuzzy", isFuzzyActive()),
                getSetting("command.repSettings.embed.descr.byEmbed", isEmbedActive()),
                getSetting("command.repSettings.embed.descr.emojidebug", guildSettings.generalSettings().isEmojiDebug())
        );

        return String.join("\n", setting);
    }

    private String getSetting(@PropertyKey(resourceBundle = "locale") String locale, Object object) {
        return String.format("$%s$: %s", locale, object);
    }

    @Override
    public Guild guild() {
        return settings.guild();
    }

    private boolean set(String parameter, ThrowingConsumer<ParamBuilder, SQLException> builder) {
        return builder()
                       .query("""
                               INSERT INTO message_settings(guild_id, %s) VALUES (?, ?)
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
