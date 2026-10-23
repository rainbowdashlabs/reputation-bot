/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.elements.BaseSettings;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collections;
import java.util.function.Supplier;

/**
 * Lets only bot owners use the wrapped handler. Everyone else gets "No." and no auto completion.
 */
public class BotOwnerOnly implements SlashHandler {
    private final Supplier<BaseSettings> baseSettings;
    private final SlashHandler handler;

    /**
     * Creates a new owner check.
     *
     * @param baseSettings supplies the current base settings, which contain the bot owners
     * @param handler      the handler to run for bot owners
     */
    public BotOwnerOnly(Supplier<BaseSettings> baseSettings, SlashHandler handler) {
        this.baseSettings = baseSettings;
        this.handler = handler;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        if (!isOwner(event.getUser().getIdLong())) {
            event.reply("No.").setEphemeral(true).complete();
            return;
        }
        handler.onSlashCommand(event, context);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        if (!isOwner(event.getUser().getIdLong())) {
            event.replyChoices(Collections.emptyList()).complete();
            return;
        }
        handler.onAutoComplete(event, context);
    }

    boolean isOwner(long userId) {
        return baseSettings.get().isOwner(userId);
    }
}
