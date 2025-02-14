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
 * Handler for the "add reaction" slash command, which adds a reaction to a message.
 */
public class Add implements SlashHandler {
    private final Guilds guilds;

    /**
     * Constructs an Add handler with the specified guilds provider.
     *
     * @param guilds the guilds provider
     */
    public Add(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles the slash command interaction event.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var emote = event.getOption("emote").getAsString();
        var message = event.reply(context.localize("command.reactions.message.checking"))
                           .flatMap(InteractionHook::retrieveOriginal).complete();
        handleAddCheckResult(event.getGuild(), context, message, emote);
    }

    /**
     * Handles the result of the emoji check and adds the reaction accordingly.
     *
     * @param guild the guild where the command was executed
     * @param context the event context
     * @param message the message to add the reaction to
     * @param emote the emote to add as a reaction
     */
    private void handleAddCheckResult(Guild guild, EventContext context, Message message, String emote) {
        var reactions = guilds.guild(guild).settings().thanking().reactions();
        var result = checkEmoji(message, emote);
        switch (result.result()) {
            case EMOJI_FOUND -> {
                reactions.add(emote);
                message.editMessage(context.localize("command.reactions.add.message.add",
                        Replacement.create("EMOTE", result.mention()))).queue();
            }
            case EMOTE_FOUND -> {
                reactions.add(result.id());
                message.editMessage(context.localize("command.reactions.add.message.add",
                        Replacement.create("EMOTE", result.mention()))).queue();
            }
            case NOT_FOUND -> message.editMessage(context.localize("command.reactions.message.notfound")).queue();
            case UNKNOWN_EMOJI ->
                    message.editMessage(context.localize("command.reactions.message.emojinotfound")).queue();
        }
    }
}
