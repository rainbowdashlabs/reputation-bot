package de.chojo.repbot.listener;

import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.analyzer.ThankType;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.manager.ReputationManager;
import de.chojo.repbot.util.HistoryUtil;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEmoteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ReactionListener extends ListenerAdapter {
    private final GuildData guildData;
    private final ReputationData reputationData;
    private final Map<Long, VoteRequest> voteRequests = new HashMap<>();
    private final Localizer localizer;
    private final ReputationManager reputationManager;

    public ReactionListener(DataSource dataSource, Localizer localizer, ReputationManager reputationManager) {
        guildData = new GuildData(dataSource);
        reputationData = new ReputationData(dataSource);
        this.localizer = localizer;
        this.reputationManager = reputationManager;
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot()) return;
        var optGuildSettings = guildData.getGuildSettings(event.getGuild());
        if (optGuildSettings.isEmpty()) return;
        var guildSettings = optGuildSettings.get();

        if (voteRequests.containsKey(event.getMessageIdLong())) {
            if (event.getReactionEmote().isEmote()) return;
            var voteRequest = voteRequests.get(event.getMessageIdLong());
            if (!voteRequest.getMember().equals(event.getMember())) return;
            var target = voteRequest.getTarget(event.getReactionEmote().getEmoji());
            if (target.isEmpty()) {
                if (event.getReactionEmote().getEmoji().equals("🗑️")) {
                    unregisterVote(voteRequest.getVoteMessage());
                    voteRequest.getVoteMessage().delete().queue();
                }
                return;
            }
            if (voteRequest.getRemainingVotes() == 0) return;

            if (reputationManager.submitReputation(event.getGuild(), event.getUser(), target.get().getUser(), voteRequest.getRefMessage(), null, ThankType.REACTION)) {
                voteRequest.voted();
                voteRequest.getVoteMessage().
                        editMessage(voteRequest.getNewEmbed(localizer.localize("listener.messages.request.descrThank"
                                , event.getGuild(), Replacement.create("MORE", voteRequest.getRemainingVotes()))))
                        .queue();
            }
            return;
        }

        if (!guildSettings.isReputationChannel(event.getChannel())) return;
        if (!guildSettings.isReactionActive()) return;
        if (!guildSettings.isReaction(event.getReaction().getReactionEmote())) return;

        var message = event.getChannel()
                .retrieveMessageById(event.getMessageId())
                .timeout(10, TimeUnit.SECONDS).complete();
        var recentMembers = HistoryUtil.getRecentMembers(message, guildSettings.getMaxMessageAge());
        if (!recentMembers.contains(event.getMember())) return;

        var receiver = message.getAuthor();

        var logEntry = reputationData.getLogEntry(message);
        if (logEntry.isPresent()) {
            Member newReceiver;
            try {
                newReceiver = event.getGuild().retrieveMemberById(logEntry.get().getReceiverId()).complete();
            } catch (RuntimeException e) {
                return;
            }
            if (newReceiver == null) return;
            receiver = newReceiver.getUser();
        }
        if (reputationManager.submitReputation(event.getGuild(), event.getUser(), receiver, message, null, ThankType.REACTION)) {
            event.getChannel().sendMessage(localizer.localize("listener.reaction.confirmation", event.getGuild(),
                    Replacement.create("DONOR", event.getUser().getAsMention()), Replacement.create("RECEIVER", receiver.getAsMention())))
                    .mention(event.getUser())
                    .queue(m -> m.delete().queueAfter(30, TimeUnit.SECONDS));
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
                    .queue(m -> m.delete().queueAfter(30, TimeUnit.SECONDS));
        }
    }

    @Override
    public void onGuildMessageReactionRemoveAll(@NotNull GuildMessageReactionRemoveAllEvent event) {
        reputationData.removeMessage(event.getMessageIdLong());
    }

    public void registerAfterVote(Message message, VoteRequest request) {
        voteRequests.put(message.getIdLong(), request);
    }

    public void unregisterVote(Message voteMessage) {
        voteRequests.remove(voteMessage.getIdLong());
    }
}
