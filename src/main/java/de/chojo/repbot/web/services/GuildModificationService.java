/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.services;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateParentEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdatePositionEvent;
import net.dv8tion.jda.api.events.emoji.EmojiAddedEvent;
import net.dv8tion.jda.api.events.emoji.EmojiRemovedEvent;
import net.dv8tion.jda.api.events.emoji.update.EmojiUpdateNameEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateIconEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateColorsEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateNameEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdatePositionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

/**
 * Listens for guild modifications that may change data represented in GuildSessionPOJO and marks cached
 * sessions for that guild as dirty in the SessionService, so they regenerate on next access.
 */
public class GuildModificationService extends ListenerAdapter {
    private final SessionService sessionService;

    public GuildModificationService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    private void mark(Guild guild) {
        sessionService.markGuildDirty(guild.getIdLong());
    }

    @Override
    public void onRoleCreate(@NotNull RoleCreateEvent event) {
        mark(event.getGuild());
    }

    @Override
    public void onRoleDelete(@NotNull RoleDeleteEvent event) {
        mark(event.getGuild());
    }

    @Override
    public void onRoleUpdateName(@NotNull RoleUpdateNameEvent event) {
        mark(event.getGuild());
    }

    @Override
    public void onRoleUpdateColors(@NonNull RoleUpdateColorsEvent event) {
        mark(event.getGuild());
    }

    @Override
    public void onRoleUpdatePosition(@NotNull RoleUpdatePositionEvent event) {
        mark(event.getGuild());
    }

    @Override
    public void onChannelCreate(@NotNull ChannelCreateEvent event) {
        mark(event.getGuild());
    }

    @Override
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
        mark(event.getGuild());
    }

    @Override
    public void onChannelUpdateName(@NotNull ChannelUpdateNameEvent event) {
        mark(event.getGuild());
    }

    @Override
    public void onChannelUpdateParent(@NotNull ChannelUpdateParentEvent event) {
        mark(event.getGuild());
    }

    @Override
    public void onChannelUpdatePosition(@NotNull ChannelUpdatePositionEvent event) {
        mark(event.getGuild());
    }

    @Override
    public void onEmojiAdded(@NotNull EmojiAddedEvent event) {
        mark(event.getGuild());
    }

    @Override
    public void onEmojiRemoved(@NotNull EmojiRemovedEvent event) {
        mark(event.getGuild());
    }

    @Override
    public void onEmojiUpdateName(@NotNull EmojiUpdateNameEvent event) {
        mark(event.getGuild());
    }

    @Override
    public void onGuildUpdateName(@NotNull GuildUpdateNameEvent event) {
        mark(event.getGuild());
    }

    @Override
    public void onGuildUpdateIcon(@NotNull GuildUpdateIconEvent event) {
        mark(event.getGuild());
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        if (event.getUser().isBot()) {
            mark(event.getGuild());
        }
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        if (event.getUser().isBot()) {
            mark(event.getGuild());
        }
    }
}
