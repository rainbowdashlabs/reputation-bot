/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.jdautil.botlist.BotListConfig;
import de.chojo.jdautil.botlist.modules.voting.post.VoteData;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.config.elements.Voting;
import de.chojo.repbot.core.Threading;
import de.chojo.repbot.dao.access.user.sub.UserSettings;
import de.chojo.repbot.dao.access.vote.VoteReason;
import de.chojo.repbot.dao.access.vote.VoteStreak;
import de.chojo.repbot.dao.provider.UserRepository;
import de.chojo.repbot.dao.provider.VoteRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class BotlistVoteService extends ListenerAdapter implements Consumer<VoteData> {
    private static final Logger log = getLogger(BotlistVoteService.class);
    private final Threading threading;
    private final Configuration configuration;
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final ShardManager shardManager;

    public BotlistVoteService(
            Configuration configuration,
            VoteRepository voteRepository,
            UserRepository userRepository,
            ShardManager shardManager,
            Threading threading) {
        this.configuration = configuration;
        this.voteRepository = voteRepository;
        this.userRepository = userRepository;
        this.shardManager = shardManager;
        this.threading = threading;
        threading.repBotWorker().scheduleAtFixedRate(this::scheduleReminder, 0, 15, TimeUnit.MINUTES);
    }

    @Override
    public void accept(VoteData voteData) {
        long userId = voteData.userId();
        VoteStreak lastVote = voteRepository.getLastVote(userId, voteData.listId());
        Voting voting = configuration.voting();
        UserSettings settings = userRepository.byId(userId).settings();
        long guildId = settings.voteGuild();

        if (lastVote.lastVote().isAfter(Instant.now().minus(voting.hoursSteak(), ChronoUnit.HOURS))) {
            lastVote.incrementStreak();
        } else {
            lastVote.resetStreak();
        }
        List<String> messages = new LinkedList<>();
        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle("Thank you for voting on  %s.".formatted(voteData.listId()));
        int voteToken = 1;
        if (lastVote.streakDays() < voting.minDaysStreak()) {
            voteRepository.addToken(userId, guildId, 1, VoteReason.STANDARD);
            messages.add("You received **1** token.");
            embed.addField(
                    "Streak",
                    "%d day/s\n*Vote every day for %s days for a bonus.*"
                            .formatted(lastVote.streakDays(), voting.minDaysStreak()),
                    true);
        } else {
            voteRepository.addToken(userId, guildId, 2, VoteReason.STREAK);
            voteToken = 2;
            messages.add("You received **2** token.");
            embed.addField(
                    "Streak",
                    "%d day/s\n*You get double the token for a streak of %s days.*"
                            .formatted(lastVote.streakDays(), voting.minDaysStreak()),
                    true);
        }

        int votesToday = voteRepository.getVoteCountToday(userId);

        List<Button> buttons = new ArrayList<>();
        int maxVotes = configuration.botlist().maxVotes();
        if (configuration.botlist().maxVotes() == votesToday) {
            voteRepository.addToken(userId, guildId, configuration.botlist().maxVotes(), VoteReason.BONUS);
            embed.addField(
                    "Bonus",
                    "%s/%s Botlists\n*You have voted on all lists in 6 hours and got %s extra token.*"
                            .formatted(votesToday, maxVotes, maxVotes),
                    true);
            int token = maxVotes + voteToken;
            messages = messages.stream()
                    .map(message -> message.replaceAll("[12]", String.valueOf(token)))
                    .toList();
        } else {
            embed.addField(
                    "Bonus",
                    "%s/%s Botlists\n*Vote on all botlists in 6 hours for %s extra token.*"
                            .formatted(votesToday, maxVotes, maxVotes),
                    true);
            Instant halfDay = Instant.now().minus(12, ChronoUnit.HOURS);
            Arrays.stream(configuration.botlist().botlists())
                    .filter(config -> !config.voteUrl().isBlank())
                    .filter(config -> voteRepository
                            .getLastVote(userId, config.name())
                            .lastVote()
                            .isBefore(halfDay))
                    .map(config -> Button.link(config.voteUrl(), config.name()))
                    .forEach(buttons::add);
        }

        embed.setDescription(String.join("\n", messages));

        try {
            User user = shardManager.retrieveUserById(userId).complete();
            PrivateChannel channel = user.openPrivateChannel().complete();
            if (!lastVote.reminder()) {
                buttons.add(Button.success("voting:%s:enable".formatted(voteData.listId()), "Enable Reminder"));
            }
            if (!buttons.isEmpty()) {
                channel.sendMessageEmbeds(embed.build())
                        .addComponents(ActionRow.of(buttons))
                        .queue();
            } else {
                channel.sendMessageEmbeds(embed.build()).queue();
            }
        } catch (Exception ignored) {
        }
    }

    private void scheduleReminder() {
        List<VoteStreak> unsendReminder = voteRepository.getUnsendReminder(Duration.ofHours(1));
        for (VoteStreak voteStreak : unsendReminder) {
            long until = Instant.now().until(voteStreak.reminderTimestamp(), ChronoUnit.MINUTES);
            until = Math.max(1, until);
            threading.repBotWorker().schedule(() -> sendReminder(voteStreak), until, TimeUnit.SECONDS);
        }
    }

    private void sendReminder(VoteStreak streak) {
        // Refresh strea meta
        streak = voteRepository.getLastVote(streak.userId(), streak.botlist());
        // The reminder no longer needs to be send.
        if (streak.isReminderSent() || streak.reminderTimestamp().isAfter(Instant.now())) return;
        User user;
        try {
            Optional<BotListConfig> optConfig = configuration.botlist().byName(streak.botlist());

            if (optConfig.isEmpty()) {
                log.error("Could not find botlist {} in configuration", streak.botlist());
                return;
            }

            BotListConfig botlist = optConfig.get();

            user = shardManager.retrieveUserById(streak.userId()).complete();
            PrivateChannel privateChannel = user.openPrivateChannel().complete();
            privateChannel
                    .sendMessage("You can vote again on %s".formatted(streak.botlist()))
                    .addComponents(ActionRow.of(
                            Button.link(botlist.voteUrl(), botlist.name()),
                            Button.success("voting:%s:snooze:30".formatted(botlist.name()), "Snooze 30 Minutes"),
                            Button.success("voting:%s:snooze:60".formatted(botlist.name()), "Snooze 1 Hour"),
                            Button.success("voting:%s:snooze:360".formatted(botlist.name()), "Snooze 6 Hours"),
                            Button.danger("voting:%s:disable".formatted(botlist.name()), "Disable Reminder")))
                    .complete();
        } catch (Exception e) {
            // ignore
        } finally {
            streak.reminderSent();
        }
    }

    @Override
    public void onButtonInteraction(@NonNull ButtonInteractionEvent event) {
        String customId = event.getButton().getCustomId();
        if (customId == null || !customId.startsWith("voting:")) return;

        String[] split = customId.split(":");
        var listName = split[1];
        var action = split[2];

        Optional<BotListConfig> optConfig = configuration.botlist().byName(listName);

        if (optConfig.isEmpty()) {
            log.error("Could not find botlist {} in configuration", listName);
            return;
        }

        BotListConfig botlist = optConfig.get();

        VoteStreak lastVote = voteRepository.getLastVote(event.getUser().getIdLong(), listName);

        switch (action) {
            case "snooze" -> {
                lastVote.snoozeReminder(Duration.ofMinutes(Integer.parseInt(split[3])));
                MessageEditData edit = MessageEditBuilder.fromMessage(event.getMessage())
                        .setComponents(ActionRow.of(Button.link(botlist.voteUrl(), botlist.name())))
                        .build();
                event.deferReply().setEphemeral(true).complete();
                event.getMessage().editMessage(edit).complete();
                event.getHook()
                        .editOriginal("I will remind you again <t:%s:R>"
                                .formatted(lastVote.reminderTimestamp().getEpochSecond()))
                        .complete();
            }
            case "disable" -> {
                lastVote.reminderState(false);
                event.reply("I will no longer remind you about votes on %s.".formatted(listName))
                        .setEphemeral(true)
                        .complete();
                MessageEditData edit = MessageEditBuilder.fromMessage(event.getMessage())
                        .setComponents(ActionRow.of(Button.link(botlist.voteUrl(), botlist.name())))
                        .build();
                event.getHook().editOriginal(edit).complete();
            }
            case "enable" -> {
                lastVote.reminderState(true);
                event.reply("I will remind you about votes on %s.".formatted(listName))
                        .setEphemeral(true)
                        .complete();
            }
        }
    }
}
