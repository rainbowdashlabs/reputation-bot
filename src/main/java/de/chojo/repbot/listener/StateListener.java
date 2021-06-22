package de.chojo.repbot.listener;

import com.zaxxer.hikari.HikariDataSource;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.wrapper.GuildSettingUpdate;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.emote.EmoteRemovedEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class StateListener extends ListenerAdapter implements Runnable {
    private static final Logger log = getLogger(StateListener.class);
    private final GuildData data;

    public StateListener(DataSource dataSource) {
        data = new GuildData(dataSource);
    }

    public static StateListener of(HikariDataSource dataSource, ScheduledExecutorService repBotWorker) {
        var stateListener = new StateListener(dataSource);
        repBotWorker.scheduleAtFixedRate(stateListener, 1, 12, TimeUnit.HOURS);
        return stateListener;
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
    public void onRoleDelete(@NotNull RoleDeleteEvent event) {
        data.removeReputationRole(event.getGuild(), event.getRole());
    }

    @Override
    public void onTextChannelDelete(@NotNull TextChannelDeleteEvent event) {
        data.removeChannel(event.getGuild(), event.getChannel());
    }

    @Override
    public void onEmoteRemoved(@NotNull EmoteRemovedEvent event) {
        var guildSettings = data.getGuildSettings(event.getGuild());
        if (guildSettings.isEmpty()) return;
        if (!guildSettings.get().reactionIsEmote()) return;
        if (!guildSettings.get().reaction().equals(event.getEmote().getId())) return;
        data.updateMessageSettings(GuildSettingUpdate.builder(event.getGuild()).reaction("✅").build());
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        log.info("");
        event.getJDA().getGuildCache().forEach(data::initGuild);
    }

    @Override
    public void run() {
        data.getRemovalTasks().forEach(data::executeRemovalTask);
    }
}
