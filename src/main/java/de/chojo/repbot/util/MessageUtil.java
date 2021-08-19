package de.chojo.repbot.util;

import de.chojo.repbot.data.wrapper.GuildSettings;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class MessageUtil {
    private static final Logger log = getLogger(MessageUtil.class);

    public static void markMessage(Message message, @Nullable Message refMessage, GuildSettings settings) {
        if (settings.thankSettings().reactionIsEmote()) {
            message.getGuild().retrieveEmoteById(settings.thankSettings().reaction()).queue(e -> {
                message.addReaction(e).queue(emote -> {
                }, err -> log.error("Could not add reaction emote", err));
                if (refMessage != null) {
                    refMessage.addReaction(e).queue(emote -> {
                    }, err -> log.error("Could not add reaction emote", err));
                }
            }, err -> log.error("Could not resolve emoji.", err));
        } else {
            if (refMessage != null) {
                message.addReaction(settings.thankSettings().reaction()).queue(e -> {
                }, err -> log.error("Could not add reaction emoji.", err));
            }
            message.addReaction(settings.thankSettings().reaction()).queue(e -> {
            }, err -> log.error("Could not add reaction emoji.", err));
        }
    }

}
