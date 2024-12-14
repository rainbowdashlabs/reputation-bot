/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.listener;

import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.dao.snapshots.ReputationRank;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.emoji.EmojiRemovedEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Listener for handling various state events in the bot.
 */
public class StateListener extends ListenerAdapter {
    /**
     * Logger for logging events.
     */
    private static final Logger log = getLogger(StateListener.class);
    private final Guilds guilds;
    private final ILocalizer localizer;
    private final Configuration configuration;
    private final Metrics metrics;

    /**
     * Constructs a new StateListener instance.
     *
     * @param guilds the guilds provider
     * @param localizer the localizer for localization
     * @param configuration the bot configuration
     * @param metrics the metrics provider
     */
    private StateListener(Guilds guilds, ILocalizer localizer, Configuration configuration, Metrics metrics) {
        this.guilds = guilds;
        this.localizer = localizer;
        this.configuration = configuration;
        this.metrics = metrics;
    }

    /**
     * Handles generic interaction creation events.
     *
     * @param event the generic interaction create event
     */
    @Override
    public void onGenericInteractionCreate(@NotNull GenericInteractionCreateEvent event) {
        metrics.service().countInteraction();
    }

    /**
     * Creates a new StateListener instance.
     *
     * @param localizer the localizer for localization
     * @param guilds the guilds provider
     * @param configuration the bot configuration
     * @param metrics the metrics provider
     * @return a new StateListener instance
     */
    public static StateListener of(ILocalizer localizer, Guilds guilds, Configuration configuration, Metrics metrics) {
        return new StateListener(guilds, localizer, configuration, metrics);
    }

    /**
     * Handles guild join events.
     *
     * @param event the guild join event
     */
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        guilds.guild(event.getGuild()).gdpr().dequeueDeletion();

        if (configuration.botlist().isBotlistGuild(event.getGuild().getIdLong())) return;

        var selfMember = event.getGuild().getSelfMember();
        for (var channel : event.getGuild().getTextChannels()) {
            if (selfMember.hasPermission(channel, Permission.VIEW_CHANNEL)
                && selfMember.hasPermission(channel, Permission.MESSAGE_SEND)) {
                channel.sendMessage(localizer.localize("message.welcome", event.getGuild()))
                       .queueAfter(5, TimeUnit.SECONDS);
                break;
            }
        }
    }

    /**
     * Handles guild leave events.
     *
     * @param event the guild leave event
     */
    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        // We want to delete all data of a guild after the bot left.
        guilds.guild(event.getGuild()).gdpr().queueDeletion();
    }

    /**
     * Handles guild member join events.
     *
     * @param event the guild member join event
     */
    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        // We want to abort deletion of user data if a user rejoins a guild during grace period
        guilds.guild(event.getGuild()).reputation().user(event.getMember()).gdpr().dequeueDeletion();
    }

    /**
     * Handles guild member remove events.
     *
     * @param event the guild member remove event
     */
    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        // When a user leaves a guild, there is no reason for us to keep their data.
        guilds.guild(event.getGuild()).reputation().user(event.getUser()).gdpr().queueDeletion();
    }

    /**
     * Handles role delete events.
     *
     * @param event the role delete event
     */
    @Override
    public void onRoleDelete(@NotNull RoleDeleteEvent event) {
        guilds.guild(event.getGuild()).settings().ranks().rank(event.getRole()).ifPresent(ReputationRank::remove);
    }

    /**
     * Handles channel delete events.
     *
     * @param event the channel delete event
     */
    @Override
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
        guilds.guild(event.getGuild()).settings().thanking().channels().remove(event.getChannel());
    }

    /**
     * Handles emoji removed events.
     *
     * @param event the emoji removed event
     */
    @Override
    public void onEmojiRemoved(@NotNull EmojiRemovedEvent event) {
        var guildSettings = guilds.guild(event.getGuild()).settings();
        guildSettings.thanking().reactions().remove(event.getEmoji().getId());

        if (!guildSettings.thanking().reactions().reactionIsEmote()) return;
        if (!guildSettings.thanking().reactions().mainReaction().equals(event.getEmoji().getId())) return;
        guildSettings.thanking().reactions().mainReaction("🏅");
    }
}
