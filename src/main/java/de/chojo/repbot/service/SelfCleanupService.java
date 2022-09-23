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
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static de.chojo.repbot.util.Guilds.prettyName;
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
            var repGuild = guilds.guild(guild);

            var joined = guild.getSelfMember().getTimeJoined();
            if (joined.isAfter(configuration.selfCleanup().getInactiveDaysOffset().atOffset(ZoneOffset.UTC))) {
                // The bot just joined. We give them some days.
                continue;
            }

            Set<InactivityMarker> markers = EnumSet.noneOf(InactivityMarker.class);
            // Check for latest reputation
            var lastReputation = repGuild.reputation().log().getLatestReputation();
            if (lastReputation.isEmpty()) {
                if (lastReputation.get().received().isBefore(configuration.selfCleanup().getInactiveDaysOffset())) {
                    markers.add(InactivityMarker.NO_REPUTATION);
                }
            } else {
                // Reputation on guild. We don't care about the rest
                repGuild.cleanup().cleanupDone();
                continue;
            }


            var channels = repGuild.settings().thanking().channels();
            if (channels.isWhitelist()) {
                // Check if channel or categories are registered.
                if (channels.channels().isEmpty()) {
                    markers.add(InactivityMarker.NO_CHANNEL);
                }

                if (channels.categories().isEmpty()) {
                    markers.add(InactivityMarker.NO_CATEGORIES);
                }
            }

            if (markers.contains(InactivityMarker.NO_CHANNEL) && markers.contains(InactivityMarker.NO_CATEGORIES)) {
                log.debug("No channels and categories registered on guild {}", prettyName(guild));
                log.debug("Bot is unconfigured for {} days",
                        Math.abs(Duration.between(joined, LocalDateTime.now().atZone(ZoneOffset.UTC)).toDays()));
                promptCleanup(guild);
                continue;
            }

            if (markers.contains(InactivityMarker.NO_REPUTATION)) {
                log.debug("No reputation on guild {}", prettyName(guild));
                log.debug("Bot is unused for {} days",
                        Math.abs(Duration.between(lastReputation.get().received(), LocalDateTime.now()
                                                                                                .atZone(ZoneOffset.UTC))
                                         .toDays()));
                promptCleanup(guild);
                continue;
            }

            repGuild.cleanup().cleanupDone();
        }

        for (var guildId : cleanup.getCleanupList()) {
            var guild = shardManager.getGuildById(guildId);
            if (guild != null) notifyCleanup(guild);
        }
    }

    private void promptCleanup(Guild guild) {
        var repGuild = guilds.guild(guild);

        // Check if a prompt was already send
        if (repGuild.cleanup().getCleanupPromptTime().isPresent()) return;

        repGuild.cleanup().selfCleanupPrompt();

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
        var clean = guilds.guild(guild).cleanup();
        if (clean.getCleanupPromptTime().get().isAfter(configuration.selfCleanup().getLeaveDaysOffset())) {
            log.debug("Prompt was send {}/{} days ago on {}",
                    Math.abs(Duration.between(clean.getCleanupPromptTime().get(),
                            LocalDateTime.now().atZone(ZoneOffset.UTC)).toDays()),
                    configuration.selfCleanup().leaveDays(),
                    prettyName(guild));
            return;
        }

        var embed = new LocalizedEmbedBuilder(localizer, guild)
                .setTitle("selfCleanup.leave.title")
                .setDescription(localizer.localize("selfCleanup.leave", guild,
                        Replacement.create("INVITE", configuration.links().invite())))
                .build();

        notifyGuild(guild, embed);
        guild.leave().queue();
        log.info(LogNotify.STATUS, "Leave on {} caused by self cleanup.", prettyName(guild));
        clean.cleanupDone();
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

    private enum InactivityMarker {
        NO_REPUTATION,
        NO_CHANNEL,
        NO_CATEGORIES
    }
}
