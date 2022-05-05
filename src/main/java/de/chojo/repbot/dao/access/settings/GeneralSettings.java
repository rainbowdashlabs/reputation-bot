package de.chojo.repbot.dao.access.settings;

import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sqlutil.wrapper.QueryBuilderFactory;
import net.dv8tion.jda.api.entities.Guild;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class GeneralSettings extends QueryBuilderFactory implements GuildHolder {
    private final String prefix;
    private final boolean emojiDebug;
    private final boolean stackRoles;
    private Settings settings;

    public GeneralSettings(Settings settings) {
        this(settings, null, true, false);
        this.settings = settings;
    }

    public GeneralSettings(Settings settings, String prefix, boolean emojiDebug, boolean stackRoles) {
        super(settings);
        this.prefix = prefix;
        this.emojiDebug = emojiDebug;
        this.stackRoles = stackRoles;
    }

    public static GeneralSettings build(Settings settings, ResultSet rs) throws SQLException {
        return new GeneralSettings(settings,
                rs.getString("prefix"),
                rs.getBoolean("emoji_debug"),
                rs.getBoolean("stack_roles"));
    }

    public Optional<String> prefix() {
        return Optional.ofNullable(prefix);
    }

    public boolean isEmojiDebug() {
        return emojiDebug;
    }

    public boolean isStackRoles() {
        return stackRoles;
    }

    @Override
    public Guild guild() {
        return settings.guild();
    }
}
