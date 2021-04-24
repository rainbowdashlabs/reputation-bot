package de.chojo.repbot.listener;

import de.chojo.repbot.data.GuildData;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;

public class StateListener extends ListenerAdapter {

    private final GuildData data;

    public StateListener(DataSource dataSource) {
        data = new GuildData(dataSource);
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        data.initGuild(event.getGuild());
    }
}
