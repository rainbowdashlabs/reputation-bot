package de.chojo.repbot.dao.access.guild;

import javax.sql.DataSource;

public class RepGuildIdImpl extends RepGuild {
    private final long guildId;

    public RepGuildIdImpl(DataSource dataSource, long guildId) {
        super(dataSource, null);
        this.guildId = guildId;
    }

    @Override
    public long guildId() {
        return guildId;
    }
}
