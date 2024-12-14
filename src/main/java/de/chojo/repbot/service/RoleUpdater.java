/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for updating roles based on reputation.
 */
public class RoleUpdater extends ListenerAdapter {
    private final Guilds guilds;
    private final Map<Long, Set<Long>> checked = new HashMap<>();
    private final RoleAssigner roleAssigner;
    private final ShardManager shardManager;

    /**
     * Creates a new RoleUpdater and schedules periodic tasks.
     *
     * @param guilds the guilds provider
     * @param roleAssigner the role assigner
     * @param shardManager the shard manager
     * @param executorService the executor service for scheduling tasks
     * @return the created RoleUpdater
     */
    public static RoleUpdater create(Guilds guilds, RoleAssigner roleAssigner, ShardManager shardManager, ScheduledExecutorService executorService) {
        var roleUpdater = new RoleUpdater(guilds, roleAssigner, shardManager);
        executorService.scheduleAtFixedRate(roleUpdater.checked::clear, 30, 30, TimeUnit.MINUTES);
        var now = ZonedDateTime.now(ZoneOffset.UTC);
        var base = now.toLocalDate().atStartOfDay().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS)
                .atOffset(ZoneOffset.UTC);
        var minutes = now.until(base, ChronoUnit.MINUTES);
        executorService.scheduleAtFixedRate(roleUpdater::updateTimed, minutes, 1440, TimeUnit.MINUTES);
        return roleUpdater;
    }

    /**
     * Constructs a new RoleUpdater.
     *
     * @param guilds the guilds provider
     * @param roleAssigner the role assigner
     * @param shardManager the shard manager
     */
    public RoleUpdater(Guilds guilds, RoleAssigner roleAssigner, ShardManager shardManager) {
        this.guilds = guilds;
        this.roleAssigner = roleAssigner;
        this.shardManager = shardManager;
    }

    /**
     * Periodically updates roles based on the current date.
     */
    private void updateTimed() {
        if (ZonedDateTime.now().getDayOfMonth() == 1) {
            for (var guild : guilds.byReputationMode(ReputationMode.MONTH)) {
                updateRoles(guild);
            }
        }

        if (ZonedDateTime.now().getDayOfWeek() == DayOfWeek.MONDAY) {
            for (var guild : guilds.byReputationMode(ReputationMode.WEEK)) {
                updateRoles(guild);
            }
        }
    }

    /**
     * Updates roles for the given guild.
     *
     * @param guild the guild to update roles for
     */
    private void updateRoles(RepGuild guild) {
        guild.load(shardManager);
        if (guild.isById()) return;
        for (var rank : guild.settings().ranks().ranks()) {
            rank.role().ifPresent(role -> {
                for (Member member : guild.guild().getMembersWithRoles(role)) {
                    try {
                        roleAssigner.update(member);
                    } catch (RoleAccessException e) {
                        // ignore
                    }
                }
            });
        }
    }

    /**
     * Handles the MessageReceived event to update roles if necessary.
     *
     * @param event the message received event
     */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        if (!guilds.guild(event.getGuild()).settings().general().reputationMode().isAutoRefresh()) return;
        if (event.getMember() == null || isChecked(event.getMember())) return;
        roleAssigner.updateReporting(event.getMember(), event.getGuildChannel());
        guildSet(event.getGuild()).add(event.getMember().getIdLong());
    }

    /**
     * Checks if the member has already been checked.
     *
     * @param member the member to check
     * @return true if the member has been checked, false otherwise
     */
    public boolean isChecked(Member member) {
        return guildSet(member.getGuild()).contains(member.getIdLong());
    }

    /**
     * Retrieves the set of checked members for the given guild.
     *
     * @param guild the guild
     * @return the set of checked members
     */
    public Set<Long> guildSet(Guild guild) {
        return checked.computeIfAbsent(guild.getIdLong(), k -> new HashSet<>());
    }
}
