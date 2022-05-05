package de.chojo.repbot.dao.access;

import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import net.dv8tion.jda.api.entities.Guild;

import javax.sql.DataSource;

public class RepGuild extends QueryFactoryHolder implements GuildHolder {
    private Guild guild;

    public RepGuild(DataSource dataSource, Guild guild) {
        super(dataSource);
        this.guild = guild;
    }

    public Guild guild() {
        return guild;
    }

    public RepGuild refresh(Guild guild) {
        this.guild = guild;
        return this;
    }
}
