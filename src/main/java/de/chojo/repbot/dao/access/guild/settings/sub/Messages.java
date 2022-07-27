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

public class Messages extends QueryFactoryHolder implements GuildHolder {
    private final Settings settings;
    private boolean reactionConfirmation;

    public Messages(Settings settings) {
        this(settings, true);
    }

    public Messages(Settings settings, boolean reactionConfirmation) {
        super(settings);
        this.settings = settings;
        this.reactionConfirmation = reactionConfirmation;
    }

    public static Messages build(Settings settings, ResultSet rs) throws SQLException {
        return new Messages(settings,
                rs.getBoolean("reaction_confirmation"));
    }

    public boolean reactionConfirmation(boolean reactionConfirmation) {
        var result = set("reaction_confirmation", stmt -> stmt.setBoolean(reactionConfirmation));
        if (result) {
            this.reactionConfirmation = reactionConfirmation;
        }
        return this.reactionConfirmation;
    }

    public boolean isReactionConfirmation() {
        return reactionConfirmation;
    }

    @Override
    public Guild guild() {
        return settings.guild();
    }

    private boolean set(String parameter, ThrowingConsumer<ParamBuilder, SQLException> builder) {
        return builder()
                       .query("""
                               INSERT INTO message_states(guild_id, %s) VALUES (?, ?)
                               ON CONFLICT(guild_id)
                                   DO UPDATE SET %s = excluded.%s;
                               """, parameter, parameter, parameter)
                       .paramsBuilder(stmts -> {
                           stmts.setLong(guildId());
                           builder.accept(stmts);
                       }).insert()
                       .executeSync() > 0;
    }

    public String toLocalizedString() {
        var setting = List.of(
                getSetting("command.messages.embed.reactionConfirmation", isReactionConfirmation())
        );

        return String.join("\n", setting);
    }

    private String getSetting(@PropertyKey(resourceBundle = "locale") String locale, boolean object) {
        return String.format("$%s$: $%s$", locale, object ? "words.enabled" : "words.disabled");
    }
}
