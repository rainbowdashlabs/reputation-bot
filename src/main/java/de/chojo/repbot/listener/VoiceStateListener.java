/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.listener;

import de.chojo.repbot.dao.provider.Voice;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Listener for voice state updates in a guild.
 */
public class VoiceStateListener extends ListenerAdapter implements Runnable {
    private final Voice voice;

    /**
     * Constructs a new VoiceStateListener.
     *
     * @param voice the voice data provider
     */
    public VoiceStateListener(Voice voice) {
        this.voice = voice;
    }

    /**
     * Creates a new VoiceStateListener and schedules it to run at fixed intervals.
     *
     * @param voice the voice data provider
     * @param repBotWorker the scheduled executor service
     * @return the created VoiceStateListener
     */
    public static VoiceStateListener of(Voice voice, ScheduledExecutorService repBotWorker) {
        var voiceStateListener = new VoiceStateListener(voice);
        repBotWorker.scheduleAtFixedRate(voiceStateListener, 2, 12, TimeUnit.HOURS);
        return voiceStateListener;
    }

    /**
     * Handles the event when a member updates their voice state in a guild.
     *
     * @param event the guild voice update event
     */
    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (event.getChannelLeft() != null) {
            voice.logUser(event.getMember(), event.getChannelLeft().getMembers());
        }
    }

    /**
     * Runs the cleanup process for voice data.
     */
    @Override
    public void run() {
        voice.cleanup();
    }
}
