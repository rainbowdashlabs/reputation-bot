package de.chojo.repbot.dao.access.guild;

import javax.sql.DataSource;

public class RepGuildId extends RepGuild {
    private final long guildId;

    public RepGuildId(DataSource dataSource, long guildId) {
        super(dataSource, null);
        this.guildId = guildId;
    }

    @Override
    public long guildId() {
        return guildId;
    }
}
