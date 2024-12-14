/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.reactions.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

import static de.chojo.repbot.commands.reactions.util.EmojiCheck.checkEmoji;

/**
 * Handler for the main slash command related to reactions.
 */
public class Main implements SlashHandler {
    private final Guilds guilds;

    /**
     * Constructs a new Main handler.
     *
     * @param guilds the Guilds provider
     */
    public Main(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles the slash command interaction event.
     *
     * @param event the SlashCommandInteractionEvent
     * @param context the EventContext
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var emote = event.getOption("emote").getAsString();
        var message = event.reply(context.localize("command.reactions.message.checking"))
                           .flatMap(InteractionHook::retrieveOriginal).complete();
        handleSetCheckResult(event.getGuild(), context, message, emote);
    }

    /**
     * Handles the result of the emoji check and updates the message accordingly.
     *
     * @param guild the Guild where the command was executed
     * @param context the EventContext
     * @param message the Message to update
     * @param emote the emote to check
     */
    private void handleSetCheckResult(Guild guild, EventContext context, Message message, String emote) {
        var reactions = guilds.guild(guild).settings().thanking().reactions();
        var result = checkEmoji(message, emote);
        switch (result.result()) {
            case EMOJI_FOUND -> {
                if (reactions.mainReaction(emote)) {
                    message.editMessage(context.localize("command.reactions.main.message.set",
                            Replacement.create("EMOTE", result.mention()))).queue();
                }
            }
            case EMOTE_FOUND -> {
                if (reactions.mainReaction(result.id())) {
                    message.editMessage(context.localize("command.reactions.main.message.set",
                            Replacement.create("EMOTE", result.mention()))).queue();
                }
            }
            case NOT_FOUND -> message.editMessage(context.localize("command.reactions.message.notfound")).queue();
            case UNKNOWN_EMOJI ->
                    message.editMessage(context.localize("command.reactions.message.emojinotfound")).queue();
        }
    }
}
