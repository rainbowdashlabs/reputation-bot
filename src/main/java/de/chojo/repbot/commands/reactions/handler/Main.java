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

public class Main implements SlashHandler {
    private final Guilds guilds;

    public Main(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var emote = event.getOption("emote").getAsString();
        var message = event.reply(context.localize("command.reaction.message.checking"))
                .flatMap(InteractionHook::retrieveOriginal).complete();
        handleSetCheckResult(event.getGuild(), context, message, emote);
    }

    private void handleSetCheckResult(Guild guild, EventContext context, Message message, String emote) {
        var reactions = guilds.guild(guild).settings().thanking().reactions();
        var result = checkEmoji(message, emote);
        switch (result.result()) {
            case EMOJI_FOUND -> {
                if (reactions.mainReaction(emote)) {
                    message.editMessage(context.localize("command.reaction.main.message.set",
                            Replacement.create("EMOTE", result.mention()))).queue();
                }
            }
            case EMOTE_FOUND -> {
                if (reactions.mainReaction(result.id())) {
                    message.editMessage(context.localize("command.reaction.main.message.set",
                            Replacement.create("EMOTE", result.mention()))).queue();
                }
            }
            case NOT_FOUND -> message.editMessage(context.localize("command.reaction.message.notfound")).queue();
            case UNKNOWN_EMOJI ->
                    message.editMessage(context.localize("command.reaction.message.emojinotfound")).queue();
        }
    }
}
