package de.chojo.repbot.listener;

import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.analyzer.ThankType;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.service.ReputationService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEmoteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class ReactionListener extends ListenerAdapter {
    private static final Logger log = getLogger(ReactionListener.class);
    private final GuildData guildData;
    private final ReputationData reputationData;
    private final Localizer localizer;
    private final ReputationService reputationService;

    public ReactionListener(DataSource dataSource, Localizer localizer, ReputationService reputationService) {
        guildData = new GuildData(dataSource);
        reputationData = new ReputationData(dataSource);
        this.localizer = localizer;
        this.reputationService = reputationService;
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot()) return;
        var optGuildSettings = guildData.getGuildSettings(event.getGuild());
        if (optGuildSettings.isEmpty()) return;
        var guildSettings = optGuildSettings.get();

        if (!guildSettings.isReputationChannel(event.getChannel())) return;
        if (!guildSettings.isReactionActive()) return;
        if (!guildSettings.isReaction(event.getReaction().getReactionEmote())) return;

        var message = event.getChannel()
                .retrieveMessageById(event.getMessageId())
                .timeout(10, TimeUnit.SECONDS).complete();

        var receiver = message.getAuthor();

        var logEntry = reputationData.getLogEntry(message);
        if (logEntry.isPresent()) {
            Member newReceiver;
            try {
                newReceiver = event.getGuild().retrieveMemberById(logEntry.get().receiverId()).complete();
            } catch (RuntimeException e) {
                return;
            }
            if (newReceiver == null) return;
            receiver = newReceiver.getUser();
        }
        if (reputationService.submitReputation(event.getGuild(), event.getUser(), receiver, message, null, ThankType.REACTION)) {
            event.getChannel().sendMessage(localizer.localize("listener.reaction.confirmation", event.getGuild(),
                    Replacement.create("DONOR", event.getUser().getAsMention()), Replacement.create("RECEIVER", receiver.getAsMention())))
                    .mention(event.getUser())
                    .onErrorFlatMap(err -> null)
                    .delay(30, TimeUnit.SECONDS).flatMap(Message::delete).queue();
        }
    }

    @Override
    public void onGuildMessageReactionRemoveEmote(@NotNull GuildMessageReactionRemoveEmoteEvent event) {
        var optGuildSettings = guildData.getGuildSettings(event.getGuild());
        if (optGuildSettings.isEmpty()) return;
        var guildSettings = optGuildSettings.get();
        if (!guildSettings.isReaction(event.getReactionEmote())) return;
        reputationData.removeMessage(event.getMessageIdLong());
    }

    @Override
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
        var optGuildSettings = guildData.getGuildSettings(event.getGuild());
        if (optGuildSettings.isEmpty()) return;
        var guildSettings = optGuildSettings.get();
        if (!guildSettings.isReaction(event.getReactionEmote())) return;
        if (reputationData.removeReputation(event.getUserIdLong(), event.getMessageIdLong(), ThankType.REACTION)) {
            event.getChannel().sendMessage(localizer.localize("listener.reaction.removal", event.getGuild(),
                    Replacement.create("DONOR", User.fromId(event.getUserId()).getAsMention())))
                    .delay(30, TimeUnit.SECONDS).flatMap(Message::delete).queue();
        }
    }

    @Override
    public void onGuildMessageReactionRemoveAll(@NotNull GuildMessageReactionRemoveAllEvent event) {
        reputationData.removeMessage(event.getMessageIdLong());
    }
}
