package de.chojo.repbot.listener;

import com.zaxxer.hikari.HikariDataSource;
import de.chojo.repbot.data.VoiceData;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VoiceStateListener extends ListenerAdapter implements Runnable {
    private final VoiceData voiceData;

    public VoiceStateListener(DataSource dataSource) {
        this.voiceData = new VoiceData(dataSource);
    }

    public static VoiceStateListener of(HikariDataSource dataSource, ScheduledExecutorService repBotWorker) {
        var voiceStateListener = new VoiceStateListener(dataSource);
        repBotWorker.scheduleAtFixedRate(voiceStateListener, 2, 12, TimeUnit.HOURS);
        return voiceStateListener;
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        voiceData.logUser(event.getMember(), event.getChannelLeft().getMembers());
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        voiceData.logUser(event.getMember(), event.getChannelLeft().getMembers());
    }

    @Override
    public void run() {
        voiceData.cleanup();
    }
}
