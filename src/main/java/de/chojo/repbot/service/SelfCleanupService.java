package de.chojo.repbot.service;

import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.Cleanup;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.util.LogNotify;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

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
    private final Guilds guilds;
    private final Configuration configuration;
    private final Cleanup cleanup;

    private SelfCleanupService(ShardManager shardManager, ILocalizer localizer, Guilds guilds, Cleanup cleanup, Configuration configuration) {
        this.shardManager = shardManager;
        this.localizer = localizer;
        this.guilds = guilds;
        this.cleanup = cleanup;
        this.configuration = configuration;
    }

    public static void create(ShardManager shardManager, ILocalizer localizer, Guilds guilds, Cleanup cleanup, Configuration configuration, ScheduledExecutorService service) {
        var selfCleanupService = new SelfCleanupService(shardManager, localizer, guilds, cleanup, configuration);
        service.scheduleAtFixedRate(selfCleanupService, 1, 60, TimeUnit.MINUTES);
    }


    @Override
    public void run() {
        if (!configuration.selfCleanup().isActive()) return;

        for (var guild : shardManager.getGuilds()) {
            var settings = guilds.guild(guild).settings();
            var inactive = guilds.guild(guild).reputation().log().getLatestReputation()
                    .map(r -> r.received().isBefore(configuration.selfCleanup().getInactiveDaysOffset()))
                    .orElse(guild.getSelfMember().getTimeJoined().isBefore(LocalDateTime.now().minusDays(30).atOffset(ZoneOffset.UTC)));
            var noChannel = settings.thanking().channels().channels().isEmpty() && settings.thanking().channels().isWhitelist();
            if (noChannel || inactive) {
                promptCleanup(guild);
                continue;
            }
            guilds.guild(guild).cleanup().cleanupDone();
        }

        for (var guildId : cleanup.getCleanupList()) {
            var guild = shardManager.getGuildById(guildId);
            if (guild != null) notifyCleanup(guild);
        }
    }

    private void promptCleanup(Guild guild) {
        var selfMember = guild.getSelfMember();
        if (configuration.botlist().isBotlistGuild(guild.getIdLong())) return;
        if (guilds.guild(guild).cleanup().getCleanupPromptTime().isPresent()) return;
        if (selfMember.getTimeJoined().isAfter(configuration.selfCleanup().getPromptDaysOffset())) {
            log.debug("Bot is unconfigured  or unused for {} days",
                    Math.abs(Duration.between(selfMember.getTimeJoined(), LocalDateTime.now().atZone(ZoneOffset.UTC)).toDays()));
            return;
        }
        guilds.guild(guild).cleanup().selfCleanupPrompt();

        var embed = new LocalizedEmbedBuilder(localizer, guild)
                .setTitle("selfCleanup.prompt.title")
                .setDescription(localizer.localize("selfCleanup.prompt", guild,
                        Replacement.create("DAYS", configuration.selfCleanup().leaveDays()),
                        Replacement.create("INACTIVE_DAYS", configuration.selfCleanup().inactiveDays()),
                        Replacement.create("DAYS_UNCONFIGURED", configuration.selfCleanup().promptDays())))
                .build();

        notifyGuild(guild, embed);
        log.info(LogNotify.STATUS, "Prompted guild self cleanup.");
    }

    private void notifyCleanup(Guild guild) {
        if (guilds.guild(guild).cleanup().getCleanupPromptTime().get().isAfter(configuration.selfCleanup().getLeaveDaysOffset())) {
            log.debug("Prompt was send {} days ago",
                    Math.abs(Duration.between(guilds.guild(guild).cleanup().getCleanupPromptTime().get(), LocalDateTime.now().atZone(ZoneOffset.UTC)).toDays()));
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
        guilds.guild(guild).cleanup().cleanupDone();
    }

    private void notifyGuild(Guild guild, MessageEmbed embed) {
        var selfMember = guild.getSelfMember();
        guild.retrieveMemberById(guild.getOwnerIdLong())
                .flatMap(member -> member.getUser().openPrivateChannel())
                .flatMap(privateChannel -> privateChannel.sendMessageEmbeds(embed))
                .onErrorMap(err -> null)
                .complete();

        for (var channel : guild.getTextChannels()) {
            if (selfMember.hasPermission(channel, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)) {
                channel.sendMessageEmbeds(embed).complete();
                break;
            }
        }
    }
}
