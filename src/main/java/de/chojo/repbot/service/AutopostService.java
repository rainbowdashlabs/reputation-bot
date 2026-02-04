/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.LocaleProvider;
import de.chojo.repbot.commands.ranking.handler.BaseTop;
import de.chojo.repbot.core.Threading;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.access.guild.settings.sub.autopost.Autopost;
import de.chojo.repbot.dao.access.guild.settings.sub.autopost.RefreshInterval;
import de.chojo.repbot.dao.access.guild.settings.sub.autopost.RefreshType;
import de.chojo.repbot.dao.pagination.Ranking;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.dao.snapshots.RankingEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class AutopostService {
    private static final Logger log = getLogger(AutopostService.class);
    private final ShardManager shardManager;
    private final GuildRepository guildRepository;
    private final ILocalizer localizer;

    public static AutopostService create(ShardManager shardManager, GuildRepository guildRepository, Threading threading, ILocalizer localizer) {
        var service = new AutopostService(shardManager, guildRepository, localizer);
        int delay = 61 - Instant.now().atZone(ZoneId.of("UTC")).getMinute();
        log.debug("Next autopost refresh will be in {} minutes.", delay);
        threading.repBotWorker().scheduleAtFixedRate(service::check, delay, 60, TimeUnit.MINUTES);
        return service;
    }

    public AutopostService(ShardManager shardManager, GuildRepository guildRepository, ILocalizer localizer) {
        this.shardManager = shardManager;
        this.guildRepository = guildRepository;
        this.localizer = localizer;
    }

    private void check() {
        log.info("Refreshing autoposts.");
        LocalDateTime now = LocalDateTime.now().atZone(ZoneId.of("UTC")).toLocalDateTime();
        for (RefreshInterval value : RefreshInterval.values()) {
            if (value.isApplicable(now)) {
                guildRepository.byAutopostRefreshInterval(value).forEach(this::update);
            }
        }
    }

    public void update(Guild guild) {
        update(guildRepository.guild(guild));
    }

    public void update(RepGuild guild) {
        guild.load(shardManager);
        if (guild.isById()) return;
        Ranking guildRanking = guild.reputation().ranking().received().defaultRanking(20);
        List<RankingEntry> ranking = guildRanking.page(0);
        MessageEditData messageEditData = BaseTop.buildRanking(ranking, guildRanking, localizer.context(LocaleProvider.guild(guild.guild())));
        Autopost autopost = guild.settings().autopost();
        RefreshType refreshType = autopost.refreshType();
        TextChannel channel = guild.guild().getTextChannelById(autopost.channelId());

        // TODO: Send message that the channel was deleted or decide to ignore the autopost
        if (channel == null) return;

        switch (refreshType) {
            case DELETE_AND_REPOST:
                delete(guild);
            case REPOST:
                sendMessage(autopost, channel, messageEditData);
                break;
            case UPDATE:
                if (autopost.messageId() == 0L) {
                    sendMessage(autopost, channel, messageEditData);
                    break;
                }
                try {
                    channel.editMessageById(autopost.messageId(), messageEditData).complete();
                } catch (ErrorResponseException e) {
                    if (e.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE) {
                        sendMessage(autopost, channel, messageEditData);
                    }
                }
        }
    }

    public void delete(Guild guild) {
        delete(guildRepository.guild(guild));
    }

    public void delete(RepGuild guild) {
        Autopost autopost = guild.settings().autopost();
        long id = autopost.messageId();
        TextChannel channel = guild.guild().getTextChannelById(autopost.channelId());
        if (channel == null) return;
        if (id != 0) {
            try {
                channel.deleteMessageById(id).complete();
            } catch (ErrorResponseException e) {
                if (e.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE) {
                    // ignore
                    return;
                }
                throw e;
            }
        }
        ;
    }

    private void sendMessage(Autopost autopost, TextChannel channel, MessageEditData data) {
        Message complete = channel.sendMessage(MessageCreateData.fromEditData(data)).complete();
        autopost.message(complete);
    }
}
