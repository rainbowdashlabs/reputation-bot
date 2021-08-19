package de.chojo.repbot.service;

import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.util.LogNotify;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class SelfCleanupService implements Runnable {
    private static final Logger log = getLogger(SelfCleanupService.class);
    private final ShardManager shardManager;
    private final ILocalizer localizer;
    private final GuildData guildData;
    private final Configuration configuration;

    private SelfCleanupService(ShardManager shardManager, ILocalizer localizer, DataSource dataSource, Configuration configuration) {
        this.shardManager = shardManager;
        this.localizer = localizer;
        this.guildData = new GuildData(dataSource);
        this.configuration = configuration;
    }

    public static SelfCleanupService create(ShardManager shardManager, ILocalizer localizer, DataSource dataSource, Configuration configuration, ScheduledExecutorService service) {
        var selfCleanupService = new SelfCleanupService(shardManager, localizer, dataSource, configuration);
        service.scheduleAtFixedRate(selfCleanupService, 1, 60, TimeUnit.MINUTES);
        return selfCleanupService;
    }


    @Override
    public void run() {
        if (!configuration.selfCleanup().isActive()) return;

        for (var guild : shardManager.getGuilds()) {
            var settings = guildData.getGuildSettings(guild);
            if (settings.thankSettings().activeChannel().isEmpty() && settings.thankSettings().isChannelWhitelist()) {
                promptCleanup(guild);
                continue;
            }
            guildData.cleanupDone(guild);
        }

        for (var guildId : guildData.getCleanupList()) {
            var guild = shardManager.getGuildById(guildId);
            if (guild != null) notifyCleanup(guild);
        }
    }

    private void promptCleanup(Guild guild) {
        var selfMember = guild.getSelfMember();
        if (configuration.botlist().isBotlistGuild(guild.getIdLong())) return;
        if (guildData.getCleanupPromptTime(guild).isPresent()) return;
        if (selfMember.getTimeJoined().isAfter(configuration.selfCleanup().getPromptDaysOffset())) {
            log.debug("Bot is unconfigured for {} days",
                    Math.abs(Duration.between(selfMember.getTimeJoined(), LocalDateTime.now().atZone(ZoneOffset.UTC)).toDays()));
            return;
        }
        guildData.selfCleanupPrompt(guild);

        var embed = new LocalizedEmbedBuilder(localizer, guild)
                .setTitle("selfCleanup.prompt.title")
                .setDescription(localizer.localize("selfCleanup.prompt", guild,
                        Replacement.create("DAYS", configuration.selfCleanup().leaveDays())))
                .build();

        notifyGuild(guild, embed);
        log.info(LogNotify.STATUS, "Promptet guild self cleanup.");
    }

    private void notifyCleanup(Guild guild) {
        if (guildData.getCleanupPromptTime(guild).get().isAfter(configuration.selfCleanup().getLeaveDaysOffset())) {
            log.debug("Prompt was send {} days ago",
                    Math.abs(Duration.between(guildData.getCleanupPromptTime(guild).get(), LocalDateTime.now().atZone(ZoneOffset.UTC)).toDays()));
            return;
        }

        var embed = new LocalizedEmbedBuilder(localizer, guild)
                .setTitle("selfCleanup.leave.title")
                .setDescription(localizer.localize("selfCleanup.leave", guild,
                        Replacement.create("INVITE", configuration.links().invite())))
                .build();

        notifyGuild(guild, embed);
        guild.leave().queue();
        log.info(LogNotify.STATUS, "Left guild caused by self cleanup.");
        guildData.cleanupDone(guild);
    }


    private void notifyGuild(Guild guild, MessageEmbed embed) {
        var selfMember = guild.getSelfMember();
        guild.retrieveMemberById(guild.getOwnerIdLong())
                .flatMap(member -> member.getUser().openPrivateChannel())
                .flatMap(privateChannel -> privateChannel.sendMessageEmbeds(embed))
                .onErrorMap(err -> null)
                .complete();

        for (var channel : guild.getTextChannels()) {
            if (selfMember.hasPermission(channel, Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE)) {
                channel.sendMessageEmbeds(embed).complete();
                break;
            }
        }
    }
}
