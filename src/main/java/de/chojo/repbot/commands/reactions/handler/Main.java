/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.reactions.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.util.WebPromo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

import static de.chojo.repbot.commands.reactions.util.EmojiCheck.checkEmoji;

public class Main implements SlashHandler {
    private final GuildRepository guildRepository;

    public Main(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var emote = event.getOption("emote").getAsString();
        var message = event.reply(context.localize("command.reactions.message.checking"))
                           .flatMap(InteractionHook::retrieveOriginal)
                           .complete();
        handleSetCheckResult(event.getGuild(), context, message, emote);
    }

    private void handleSetCheckResult(Guild guild, EventContext context, Message message, String emote) {
        var reactions = guildRepository.guild(guild).settings().thanking().reactions();
        var result = checkEmoji(message, emote);
        switch (result.result()) {
            case EMOJI_FOUND -> {
                if (reactions.mainReaction(emote)) {
                    message.editMessage(WebPromo.promoString(context) + "\n" + context.localize("command.reactions.main.message.set",
                            Replacement.create("EMOTE", result.mention()))).complete();
                }
            }
            case EMOTE_FOUND -> {
                if (reactions.mainReaction(result.id())) {
                    message.editMessage(WebPromo.promoString(context) + "\n" + context.localize("command.reactions.main.message.set",
                            Replacement.create("EMOTE", result.mention()))).queue();
                }
            }
            case NOT_FOUND ->
                    message.editMessage(WebPromo.promoString(context) + "\n" + context.localize("command.reactions.message.notfound")).queue();
            case UNKNOWN_EMOJI ->
                    message.editMessage(WebPromo.promoString(context) + "\n" + context.localize("command.reactions.message.emojinotfound")).queue();
        }
    }
}
