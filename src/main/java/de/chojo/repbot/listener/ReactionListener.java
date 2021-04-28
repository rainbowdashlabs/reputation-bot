package de.chojo.repbot.listener;

import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.repbot.analyzer.ThankType;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import de.chojo.repbot.manager.RoleAssigner;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static de.chojo.repbot.util.MessageUtil.markMessage;

@Slf4j
public class ReactionListener extends ListenerAdapter {
    private final GuildData guildData;
    private final ReputationData reputationData;
    private final RoleAssigner roleAssigner;
    private final Map<Long, VoteRequest> voteRequests = new HashMap<>();
    private final Localizer localizer;

    public ReactionListener(DataSource dataSource, RoleAssigner roleAssigner, Localizer localizer) {
        guildData = new GuildData(dataSource);
        reputationData = new ReputationData(dataSource);
        this.roleAssigner = roleAssigner;
        this.localizer = localizer;
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot()) return;
        var optGuildSettings = guildData.getGuildSettings(event.getGuild());
        if (optGuildSettings.isEmpty()) return;
        var guildSettings = optGuildSettings.get();

        if (voteRequests.containsKey(event.getMessageIdLong())) {
            var voteRequest = voteRequests.get(event.getMessageIdLong());
            if (event.getReactionEmote().isEmote()) return;
            if (!Verifier.equalSnowflake(voteRequest.getMember(), event.getMember())) return;
            if (voteRequest.getRemainingVotes() == 0) return;
            var target = voteRequest.getTarget(event.getReactionEmote().getEmoji());
            if (target.isEmpty()) return;

            var lastRatedDuration = reputationData.getLastRatedDuration(event.getGuild(), event.getUser(), target.get().getUser(), ChronoUnit.MINUTES);
            if (lastRatedDuration < guildSettings.getCooldown()) return;

            if (submitRepVote(event.getGuild(), event.getUser(), target.get().getUser(), voteRequest.getRefMessage(), null, guildSettings)) {
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

        event.getChannel()
                .retrieveMessageById(event.getMessageId())
                .timeout(10, TimeUnit.SECONDS)
                .queue(message -> {
                    var until = message.getTimeCreated().toInstant().until(Instant.now(), ChronoUnit.MINUTES);
                    if (until > guildSettings.getMaxMessageAge()) return;

                    var lastRatedDuration = reputationData.getLastRatedDuration(event.getGuild(), event.getUser(), message.getAuthor(), ChronoUnit.MINUTES);
                    if (lastRatedDuration < guildSettings.getCooldown()) return;

                    if (Verifier.equalSnowflake(event.getMember(), message.getAuthor())) return;

                    var logEntry = reputationData.getLogEntry(message);
                    if (logEntry.isPresent()) {
                        Member newReceiver;
                        try {
                            newReceiver = event.getGuild().retrieveMemberById(logEntry.get().getReceiverId()).complete();
                        } catch (RuntimeException e) {
                            return;
                        }
                        if (newReceiver == null) return;
                        submitRepVote(event.getGuild(), event.getUser(), newReceiver.getUser(), message, null, guildSettings);
                        return;
                    }

                    reputationData.logReputation(event.getGuild(), event.getUser(), message.getAuthor(), message, null, ThankType.REACTION);
                    roleAssigner.update(message.getMember());
                }, err -> log.error("Could not retrieve reaction message.", err));
    }

    private boolean submitRepVote(Guild guild, User donator, User receiver, Message scope, Message refMessage, GuildSettings settings) {
        if (receiver.isBot()) return false;
        var lastRatedDuration = reputationData.getLastRatedDuration(guild, donator, receiver, ChronoUnit.MINUTES);
        if (lastRatedDuration < settings.getCooldown()) return false;

        if (reputationData.logReputation(guild, donator, receiver, scope, refMessage, ThankType.REACTION)) {
            markMessage(scope, refMessage, settings);
            roleAssigner.update(guild.getMember(receiver));
            return true;
        }
        return false;
    }


    public void registerAfterVote(Message message, VoteRequest request) {
        voteRequests.put(message.getIdLong(), request);
    }

    public void unregisterVote(Message voteMessage) {
        voteRequests.remove(voteMessage.getIdLong());
    }
}
