/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.repsettings.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.util.Text;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.PropertyKey;

import java.util.List;

import static de.chojo.repbot.util.EmojiDebug.DONOR_LIMIT;
import static de.chojo.repbot.util.EmojiDebug.DONOR_NOT_IN_CONTEXT;
import static de.chojo.repbot.util.EmojiDebug.EMPTY_CONTEXT;
import static de.chojo.repbot.util.EmojiDebug.FOUND_THANKWORD;
import static de.chojo.repbot.util.EmojiDebug.ONLY_COOLDOWN;
import static de.chojo.repbot.util.EmojiDebug.PROMPTED;
import static de.chojo.repbot.util.EmojiDebug.RECEIVER_LIMIT;
import static de.chojo.repbot.util.EmojiDebug.TARGET_NOT_IN_CONTEXT;
import static de.chojo.repbot.util.EmojiDebug.TOO_OLD;

/**
 * Handles the emoji information slash command.
 */
public class EmojiInfo implements SlashHandler {

    private final Guilds guilds;

    /**
     * Constructs a new EmojiInfo instance.
     *
     * @param guilds the Guilds instance
     */
    public EmojiInfo(Guilds guilds) {
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
        var generalSettings = guilds.guild(event.getGuild()).settings().general();
        event.reply(Text.getBooleanMessage(context, generalSettings.isEmojiDebug(),
                "command.repsettings.emojidebug.message.true", "command.repsettings.emojidebug.message.false")
                    + "\n" + context.localize(emojiExplanation())).queue();
    }

    /**
     * Provides an explanation of the emoji debug messages.
     *
     * @return the explanation string
     */
    private String emojiExplanation() {
        var emojis = List.of(
                String.format("$%s$", "command.repsettings.emojidebug.message.title"),
                emojiString(FOUND_THANKWORD, "command.repsettings.emojidebug.message.found"),
                emojiString(ONLY_COOLDOWN, "command.repsettings.emojidebug.message.cooldown"),
                emojiString(EMPTY_CONTEXT, "command.repsettings.emojidebug.message.noReceiver"),
                emojiString(TARGET_NOT_IN_CONTEXT, "command.repsettings.emojidebug.message.noRecentMessages"),
                emojiString(DONOR_NOT_IN_CONTEXT, "command.repsettings.emojidebug.message.noDonor"),
                emojiString(TOO_OLD, "command.repsettings.emojidebug.message.tooold"),
                emojiString(PROMPTED, "command.repsettings.emojidebug.message.prompted"),
                emojiString(DONOR_LIMIT, "command.repsettings.emojidebug.message.donorLimit"),
                emojiString(RECEIVER_LIMIT, "command.repsettings.emojidebug.message.receiverLimit")
        );
        return String.join("\n", emojis);
    }

    /**
     * Formats an emoji string with its corresponding message code.
     *
     * @param emoji the emoji string
     * @param code the message code
     * @return the formatted emoji string
     */
    private String emojiString(String emoji, @PropertyKey(resourceBundle = "locale") String code) {
        return String.format("%s ➜ $%s$", emoji, code);
    }
}
