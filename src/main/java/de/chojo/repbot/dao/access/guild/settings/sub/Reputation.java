package de.chojo.repbot.dao.access.guild.settings.sub;

import de.chojo.jdautil.consumer.ThrowingConsumer;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.wrapper.ParamBuilder;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.PropertyKey;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class Reputation extends QueryFactoryHolder implements GuildHolder {
    private final Settings settings;
    private boolean reactionActive;
    private boolean answerActive;
    private boolean mentionActive;
    private boolean fuzzyActive;
    private boolean embedActive;
    private boolean skipSingleEmbed;

    public Reputation(Settings settings) {
        this(settings, true, true, true, true, true, false);
    }

    public Reputation(Settings settings, boolean reactionActive, boolean answerActive, boolean mentionActive, boolean fuzzyActive, boolean embedActive, boolean skipSingleEmbed) {
        super(settings);
        this.settings = settings;
        this.reactionActive = reactionActive;
        this.answerActive = answerActive;
        this.mentionActive = mentionActive;
        this.fuzzyActive = fuzzyActive;
        this.embedActive = embedActive;
        this.skipSingleEmbed = skipSingleEmbed;
    }

    public static Reputation build(Settings settings, ResultSet rs) throws SQLException {
        return new Reputation(settings,
                rs.getBoolean("reactions_active"),
                rs.getBoolean("answer_active"),
                rs.getBoolean("mention_active"),
                rs.getBoolean("fuzzy_active"),
                rs.getBoolean("embed_active"),
                rs.getBoolean("skip_single_embed"));
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

    public boolean isSkipSingleEmbed() {
        return skipSingleEmbed;
    }

    public boolean embedActive(boolean embedActive) {
        var result = set("embed_active", stmt -> stmt.setBoolean(embedActive));
        if (result) {
            this.embedActive = embedActive;
        }
        return this.embedActive;
    }

    public boolean reactionActive(boolean reactionActive) {
        var result = set("reactions_active", stmt -> stmt.setBoolean(reactionActive));
        if (result) {
            this.reactionActive = reactionActive;
        }
        return this.reactionActive;
    }

    public boolean answerActive(boolean answerActive) {
        var result = set("answer_active", stmt -> stmt.setBoolean(answerActive));
        if (result) {
            this.answerActive = answerActive;
        }
        return this.answerActive;
    }

    public boolean mentionActive(boolean mentionActive) {
        var result = set("mention_active", stmt -> stmt.setBoolean(mentionActive));
        if (result) {
            this.mentionActive = mentionActive;
        }
        return this.mentionActive;
    }

    public boolean fuzzyActive(boolean fuzzyActive) {
        var result = set("fuzzy_active", stmt -> stmt.setBoolean(fuzzyActive));
        if (result) {
            this.fuzzyActive = fuzzyActive;
        }
        return this.fuzzyActive;
    }

    public boolean skipSingleEmbed(boolean skipSingleEmbed) {
        var result = set("skip_single_embed", stmt -> stmt.setBoolean(skipSingleEmbed));
        if (result) {
            this.skipSingleEmbed = skipSingleEmbed;
        }
        return this.skipSingleEmbed;
    }

    public String toLocalizedString() {
        var setting = List.of(
                getSetting("command.repSettings.embed.descr.byReaction", isReactionActive()),
                getSetting("command.repSettings.embed.descr.byAnswer", isAnswerActive()),
                getSetting("command.repSettings.embed.descr.byMention", isMentionActive()),
                getSetting("command.repSettings.embed.descr.byFuzzy", isFuzzyActive()),
                getSetting("command.repSettings.embed.descr.byEmbed", isEmbedActive()),
                getSetting("command.repSettings.embed.descr.emojidebug", settings.general().isEmojiDebug()),
                getSetting("command.repSettings.embed.descr.skipSingleEmbed", settings.reputation().isSkipSingleEmbed())
        );

        return String.join("\n", setting);
    }

    private String getSetting(@PropertyKey(resourceBundle = "locale") String locale, boolean object) {
        return String.format("$%s$: $%s$", locale, object ? "words.enabled" : "words.disabled");
    }

    @Override
    public Guild guild() {
        return settings.guild();
    }

    private boolean set(String parameter, ThrowingConsumer<ParamBuilder, SQLException> builder) {
        return builder()
                       .query("""
                               INSERT INTO reputation_settings(guild_id, %s) VALUES (?, ?)
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
