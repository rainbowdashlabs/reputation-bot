/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.jdautil.pagination.PageService;
import de.chojo.jdautil.util.Consumers;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.commands.bot.handler.Debug;
import de.chojo.repbot.commands.bot.handler.SharedGuilds;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.util.Guilds;
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

    public ChatSupportService(Configuration configuration, ShardManager shardManager, PageService pageService, GuildRepository guildRepository) {
        this.configuration = configuration;
        this.shardManager = shardManager;
        this.pageService = pageService;
        this.guildRepository = guildRepository;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getAuthor().isBot()) return;
        if (!event.isFromGuild()) {
            ThreadChannel channel = getThread(event.getAuthor());
            if (channel == null) return;
            channel.sendMessage(reconstruct(event.getMessage())).complete();
        } else if (event.getGuild().getIdLong() == configuration.baseSettings().botGuild()
                && event.getChannel() instanceof ThreadChannel thread
                && thread.getParentChannel().getIdLong() == configuration.baseSettings().privateSupportChannel()) {
            getUser(thread).ifPresent(user -> {

                user.openPrivateChannel().complete().sendMessage(reconstruct(event.getMessage()))
                    .queue(Consumers.empty(), err -> getThread(event.getAuthor()).sendMessage("Message could not be sent.").complete());
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

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String id = event.getButton().getId();
        if (id == null) return;
        if (!id.startsWith("debug:")) return;

        String guildId = id.split(":")[1];
        Guild guild = shardManager.getGuildById(Long.parseLong(guildId));
        if(guild ==null){
            event.reply("Guild not found.").setEphemeral(true).complete();
            return;
        }

        Debug.sendDebug(event, pageService, guildRepository.guild(guild), null);
    }

    private long createThread(User user) {
        TextChannel textChannelById = home().getTextChannelById(configuration.baseSettings().privateSupportChannel());
        if (textChannelById == null) return -1;

        List<Guild> shared = SharedGuilds.sharedGuilds(user, true, configuration);

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("New request")
                .setAuthor("%s (%s)".formatted(user.getName(), user.getId()), null, user.getEffectiveAvatarUrl())
                .addField("Shared Guilds", shared.stream().map(g -> SharedGuilds.format(g, user)).collect(Collectors.joining("\n")), false)
                .build();

        List<Button> buttons = shared.stream().map(g -> Button.primary("debug:%s".formatted(g.getIdLong()), g.getName())).toList();

        Message message = textChannelById.sendMessage("New request from %s (%s)".formatted(user.getName(), user.getId()))
                                         .complete();

        ThreadChannel thread = textChannelById.createThreadChannel("%s (%s)".formatted(user.getName(), user.getIdLong()), message.getIdLong()).complete();
        thread.sendMessageEmbeds(embed)
                                .setComponents(ActionRow.partitionOf(buttons))
                                .complete();

        user.openPrivateChannel().complete().sendMessage("A new support chat was opened. Your request has been sent to the support team.")
            .complete();

        query("INSERT INTO support_threads (user_id, thread_id) VALUES (?, ?) ON CONFLICT(user_id) DO UPDATE SET thread_id = excluded.thread_id;")
                .single(call().bind(user.getIdLong()).bind(thread.getIdLong()))
                .insert();

        return thread.getIdLong();
    }

    private Guild home() {
        return shardManager.getGuildById(configuration.baseSettings().botGuild());
    }
}
