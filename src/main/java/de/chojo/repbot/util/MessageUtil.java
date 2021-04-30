package de.chojo.repbot.util;

import de.chojo.repbot.data.wrapper.GuildSettings;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.Nullable;

@Slf4j
public class MessageUtil {
    public static void markMessage(Message message, @Nullable Message refMessage, GuildSettings settings) {
        if (settings.reactionIsEmote()) {
            message.getGuild().retrieveEmoteById(settings.getReaction()).queue(e -> {
                message.addReaction(e).queue(emote -> {
                }, err -> log.error("Could not add reaction emote", err));
                if (refMessage != null) {
                    refMessage.addReaction(e).queue(emote -> {
                    }, err -> log.error("Could not add reaction emote", err));
                }
            }, err -> log.error("Could not resolve emoji.", err));
        } else {
            if (refMessage != null) {
                message.addReaction(settings.getReaction()).queue(e -> {
                }, err -> log.error("Could not add reaction emoji.", err));
            }
            message.addReaction(settings.getReaction()).queue(e -> {
            }, err -> log.error("Could not add reaction emoji.", err));
        }
    }

}
