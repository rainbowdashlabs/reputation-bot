package de.chojo.repbot.listener;

import de.chojo.repbot.data.VoiceData;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;

public class VoiceStateListener extends ListenerAdapter implements Runnable {
    private final VoiceData voiceData;

    public VoiceStateListener(DataSource dataSource) {
        this.voiceData = new VoiceData(dataSource);
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
