package de.chojo.repbot.listener;

import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.wrapper.RemovalTask;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.util.List;

public class StateListener extends ListenerAdapter implements Runnable {

    private final GuildData data;

    public StateListener(DataSource dataSource) {
        data = new GuildData(dataSource);
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        data.initGuild(event.getGuild());
        data.dequeueDeletion(event.getGuild());
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        data.queueDeletion(event.getGuild());
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        data.dequeueDeletion(event.getMember());
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        data.queueDeletion(event.getUser(), event.getGuild());
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        event.getJDA().getGuildCache().forEach(data::initGuild);
    }

    @Override
    public void run() {
        data.getRemovalTasks().forEach(data::executeRemovalTask);
    }
}
