/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.jdautil.botlist.modules.voting.post.VoteData;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.config.elements.Voting;
import de.chojo.repbot.dao.access.user.UserSettings;
import de.chojo.repbot.dao.access.vote.VoteReason;
import de.chojo.repbot.dao.access.vote.VoteStreak;
import de.chojo.repbot.dao.provider.UserRepository;
import de.chojo.repbot.dao.provider.VoteRepository;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class VoteService implements Consumer<VoteData> {
    Configuration configuration;
    VoteRepository voteRepository;
    UserRepository userRepository;
    ShardManager shardManager;

    public VoteService(
            Configuration configuration,
            VoteRepository voteRepository,
            UserRepository userRepository,
            ShardManager shardManager) {
        this.configuration = configuration;
        this.voteRepository = voteRepository;
        this.userRepository = userRepository;
        this.shardManager = shardManager;
    }

    @Override
    public void accept(VoteData voteData) {
        long userId = voteData.userId();
        VoteStreak lastVote = voteRepository.getLastVote(userId, voteData.listId());
        Voting voting = configuration.voting();
        UserSettings settings = userRepository.getSettingsById(userId);
        long guildId = settings.voteGuild();

        if (lastVote.lastVote().isAfter(Instant.now().minus(voting.hoursSteak(), ChronoUnit.HOURS))) {
            lastVote.incrementStreak();
        } else {
            lastVote.resetStreak();
        }
        List<String> messages = new LinkedList<>();
        messages.add("Thank you for voting on  %s.".formatted(voteData.listId()));
        if (lastVote.streak() >= voting.minDaysStreak()) {
            voteRepository.addToken(userId, guildId, 2, VoteReason.STREAK);
            messages.add(
                    "You received two tokens. You have a streak of %d day/s. Since you have a streak of %d or higher, you receive one extra token."
                            .formatted(lastVote.streak(), voting.minDaysStreak()));
        } else {
            voteRepository.addToken(userId, guildId, 1, VoteReason.STANDARD);
            messages.add(
                    "You received one token. You have a streak of %d day/s. When reaching a streak of %d or higher you receive one extra token."
                            .formatted(lastVote.streak(), voting.minDaysStreak()));
        }

        int votesToday = voteRepository.getVoteCountToday(userId);

        if (configuration.botlist().maxVotes() >= votesToday) {
            voteRepository.addToken(userId, guildId, configuration.botlist().maxVotes(), VoteReason.BONUS);
            messages.add("Since you voted on all botlists in the last 6 hours, you receive %d extra token."
                    .formatted(configuration.botlist().maxVotes()));
        }

        try {
            User user = shardManager.retrieveUserById(userId).complete();
            PrivateChannel channel = user.openPrivateChannel().complete();
            channel.sendMessage(String.join("\n", messages)).queue();
        } catch (Exception ignored) {
        }
    }
}
