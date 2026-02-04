/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class OnboardingService extends ListenerAdapter {
    private final GuildRepository repository;

    public OnboardingService(GuildRepository repository) {
        this.repository = repository;
    }

    public void start(SlashCommandInteractionEvent event, EventContext context) {

    }
}
