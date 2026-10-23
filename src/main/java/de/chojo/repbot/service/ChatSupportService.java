/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.jdautil.menus.MenuAction;
import de.chojo.jdautil.menus.MenuService;
import de.chojo.jdautil.menus.entries.MenuEntry;
import de.chojo.jdautil.pagination.PageService;
import de.chojo.jdautil.util.Consumers;
import de.chojo.repbot.commands.bot.handler.Debug;
import de.chojo.repbot.commands.bot.handler.SharedGuilds;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static net.dv8tion.jda.api.utils.messages.MessageCreateBuilder.fromMessage;

public class ChatSupportService extends ListenerAdapter {
    private final Configuration configuration;
    private final ShardManager shardManager;
    private final PageService pageService;
    private final GuildRepository guildRepository;
    private final MenuService menuService;

    public ChatSupportService(
            Configuration configuration,
            ShardManager shardManager,
            PageService pageService,
            GuildRepository guildRepository,
            MenuService menuService) {
        this.configuration = configuration;
        this.shardManager = shardManager;
        this.pageService = pageService;
        this.menuService = menuService;
        this.guildRepository = guildRepository;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getAuthor().isBot()) return;
        if (!event.isFromGuild()) {
            if (supportChannel().isEmpty()) return;
            ThreadChannel channel = getThread(event.getAuthor(), event.getMessage());
            if (channel == null) return;
            channel.sendMessage(reconstruct(event.getMessage())).complete();
        } else if (event.getGuild().getIdLong() == configuration.baseSettings().botGuild()
                && event.getChannel() instanceof ThreadChannel thread
                && thread.getParentChannel().getIdLong()
                        == configuration.baseSettings().privateSupportChannel()) {
            getUser(thread).ifPresent(user -> {
                user.openPrivateChannel()
                        .complete()
                        .sendMessage(reconstruct(event.getMessage()))
                        .queue(
                                Consumers.empty(),
                                err -> event.getChannel()
                                        .sendMessage("Message could not be sent.")
                                        .complete());
            });
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String id = event.getButton().getCustomId();
        if (id == null) return;
        if (!id.startsWith("debug:")) return;

        String guildId = id.split(":")[1];
        Guild guild = shardManager.getGuildById(Long.parseLong(guildId));
        if (guild == null) {
            event.reply("Guild not found.").setEphemeral(true).complete();
            return;
        }

        Debug.sendDebug(event, pageService, guildRepository.guild(guild), null);
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

    private ThreadChannel getThread(User user, Message message) {
        var thread = openThread(user);
        if (thread.isEmpty()) {
            promptThread(user, message);
            return null;
        }
        return thread.get();
    }

    /**
     * Finds the open support thread of a user. A locked or archived thread counts as closed: it is locked and no longer
     * linked to the user.
     *
     * @param user the user who wrote to the bot
     * @return the open thread, or empty if the user has none
     */
    private synchronized Optional<ThreadChannel> openThread(User user) {
        var thread = query("SELECT thread_id FROM support_threads WHERE user_id = ?")
                .single(call().bind(user.getIdLong()))
                .mapAs(Long.class)
                .first()
                .flatMap(this::findThread);
        if (thread.isEmpty()) return Optional.empty();

        if (thread.get().isLocked() || thread.get().isArchived()) {
            query("DELETE FROM support_threads WHERE user_id = ?")
                    .single(call().bind(user.getIdLong()))
                    .delete();
            thread.get().getManager().setLocked(true).complete();
            return Optional.empty();
        }
        return thread;
    }

    /**
     * Finds a support thread, including archived threads that are not in the cache. Support threads start from a
     * message in the support channel and share its id.
     *
     * @param threadId the id of the thread
     * @return the thread, or empty if it or its starter message no longer exists
     */
    private Optional<ThreadChannel> findThread(long threadId) {
        var cached = Optional.ofNullable(home()).map(guild -> guild.getThreadChannelById(threadId));
        if (cached.isPresent()) return cached;
        return supportChannel().flatMap(channel -> {
            try {
                return Optional.ofNullable(
                        channel.retrieveMessageById(threadId).complete().getStartedThread());
            } catch (ErrorResponseException e) {
                return Optional.empty();
            }
        });
    }

    private Optional<User> getUser(ThreadChannel channel) {
        return query("SELECT user_id FROM support_threads WHERE thread_id = ?")
                .single(call().bind(channel.getIdLong()))
                .mapAs(Long.class)
                .first()
                .map(id -> shardManager.retrieveUserById(id).complete());
    }

    private void promptThread(User user, Message initMessage) {
        menuService.register(MenuAction.forChannel("Do you need support?", initMessage.getChannel())
                .addComponent(MenuEntry.of(Button.success("yes", "Yes"), ctx -> {
                    ctx.event().editComponents().complete();
                    createThread(user, initMessage);
                }))
                .addComponent(MenuEntry.of(
                        Button.danger("no", "No"),
                        ctx -> ctx.event()
                                .editMessage("Alright, if you need help, drop us a message here.")
                                .setComponents()
                                .queue()))
                .build());
    }

    /**
     * Opens a support thread for a user and posts the first message there. If the user already has an open thread, for
     * example because they answered an earlier prompt, the message goes to that thread instead.
     *
     * @param user        the user who asked for support
     * @param initMessage the message that started the request
     */
    private synchronized void createThread(User user, Message initMessage) {
        var openThread = openThread(user);
        if (openThread.isPresent()) {
            openThread.get().sendMessage(reconstruct(initMessage)).complete();
            return;
        }
        var supportChannel = supportChannel();
        if (supportChannel.isEmpty()) return;
        TextChannel textChannelById = supportChannel.get();

        List<Guild> shared = SharedGuilds.sharedGuilds(user, true, configuration);

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("New request")
                .setAuthor("%s (%s)".formatted(user.getName(), user.getId()), null, user.getEffectiveAvatarUrl())
                .addField(
                        "Shared Guilds",
                        shared.stream().map(g -> SharedGuilds.format(g, user)).collect(Collectors.joining("\n")),
                        false)
                .build();

        List<Button> buttons = shared.stream()
                .map(g -> Button.primary("debug:%s".formatted(g.getIdLong()), g.getName()))
                .toList();

        Message message = textChannelById
                .sendMessage("New request from %s (%s)".formatted(user.getName(), user.getId()))
                .complete();

        ThreadChannel thread = textChannelById
                .createThreadChannel("%s (%s)".formatted(user.getName(), user.getIdLong()), message.getIdLong())
                .complete();
        thread.sendMessageEmbeds(embed)
                .setComponents(ActionRow.partitionOf(buttons))
                .complete();
        thread.sendMessage(reconstruct(initMessage)).complete();

        user.openPrivateChannel()
                .complete()
                .sendMessage("A new support chat was opened. Your request has been sent to the support team.")
                .complete();

        query("""
                INSERT
                INTO
                    support_threads
                    (user_id, thread_id)
                VALUES
                    (?, ?)
                ON CONFLICT(user_id) DO UPDATE SET
                    thread_id = excluded.thread_id;""")
                .single(call().bind(user.getIdLong()).bind(thread.getIdLong()))
                .insert();
    }

    private Guild home() {
        return shardManager.getGuildById(configuration.baseSettings().botGuild());
    }

    /**
     * Finds the channel that holds the support threads.
     *
     * @return the private support channel on the bot guild, or empty if the bot guild or the channel is not set or not
     *     reachable
     */
    private Optional<TextChannel> supportChannel() {
        return Optional.ofNullable(home())
                .map(guild ->
                        guild.getTextChannelById(configuration.baseSettings().privateSupportChannel()));
    }
}
