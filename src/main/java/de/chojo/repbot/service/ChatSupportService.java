/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.jdautil.util.Consumers;
import de.chojo.repbot.config.Configuration;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Collectors;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static net.dv8tion.jda.api.utils.messages.MessageCreateBuilder.fromMessage;

public class ChatSupportService extends ListenerAdapter {
    private final Configuration configuration;
    private final ShardManager shardManager;

    public ChatSupportService(Configuration configuration, ShardManager shardManager) {
        this.configuration = configuration;
        this.shardManager = shardManager;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getAuthor().isBot()) return;
        if (!event.isFromGuild()) {
            ThreadChannel channel = getThread(event.getAuthor());
            if (channel == null) return;
            channel.sendMessage(reconstruct(event.getMessage())).queue();
        } else if (event.getGuild().getIdLong() == configuration.baseSettings().botGuild()
                && event.getChannel() instanceof ThreadChannel thread
                && thread.getParentChannel().getIdLong() == configuration.baseSettings().privateSupportChannel()) {
            getUser(thread).ifPresent(user -> {

                user.openPrivateChannel().complete().sendMessage(reconstruct(event.getMessage()))
                    .queue(Consumers.empty(), err -> getThread(event.getAuthor()).sendMessage("Message could not be sent.").queue());
            });
        }
    }

    private MessageCreateData reconstruct(Message message) {
        String list = message.getAttachments().stream()
                             .filter(Attachment::isImage)
                             .map(Attachment::getUrl)
                             .collect(Collectors.joining(", "));

        var builder = fromMessage(message);
        if (!list.isBlank()) builder.addContent("\nAttachments: " + list);
        return builder.build();
    }

    private ThreadChannel getThread(User user) {
        Long id = query("SELECT thread_id FROM support_threads WHERE user_id = ?")
                .single(call().bind(user.getIdLong()))
                .mapAs(Long.class)
                .first()
                .orElseGet(() -> createThread(user));
        ThreadChannel thread = home().getThreadChannelById(id);
        if (thread == null) return home().getThreadChannelById(createThread(user));

        if (thread.isLocked() || thread.isArchived()) {
            query("DELETE FROM support_threads WHERE user_id = ?")
                    .single(call().bind(user.getIdLong()))
                    .delete();
            thread.getManager().setLocked(true).complete();
            return getThread(user);
        }

        return thread;
    }

    private Optional<User> getUser(ThreadChannel channel) {
        return query("SELECT user_id FROM support_threads WHERE thread_id = ?")
                .single(call().bind(channel.getIdLong()))
                .mapAs(Long.class)
                .first()
                .map(id -> shardManager.retrieveUserById(id).complete());
    }

    private long createThread(User user) {
        TextChannel textChannelById = home().getTextChannelById(configuration.baseSettings().privateSupportChannel());
        if (textChannelById == null) return -1;

        Message message = textChannelById.sendMessage("New request by: %s (%s)".formatted(user.getName(), user.getId())).complete();
        ThreadChannel thread = textChannelById.createThreadChannel("%s (%s)".formatted(user.getName(), user.getIdLong()), message.getIdLong()).complete();

        user.openPrivateChannel().complete().sendMessage("A new support chat was opened. Your request has been sent to the support team.").queue();

        query("INSERT INTO support_threads (user_id, thread_id) VALUES (?, ?) ON CONFLICT(user_id) DO UPDATE SET thread_id = excluded.thread_id;")
                .single(call().bind(user.getIdLong()).bind(thread.getIdLong()))
                .insert();

        return thread.getIdLong();
    }

    private Guild home() {
        return shardManager.getGuildById(configuration.baseSettings().botGuild());
    }
}
