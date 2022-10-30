package de.chojo.repbot.dao.access.guild;

import de.chojo.repbot.config.Configuration;

import javax.sql.DataSource;

public class RepGuildId extends RepGuild {
    private final long guildId;

    public RepGuildId(DataSource dataSource, long guildId, Configuration configuration) {
        super(dataSource, null, configuration);
        this.guildId = guildId;
    }

    @Override
    public long guildId() {
        return guildId;
    }
}
