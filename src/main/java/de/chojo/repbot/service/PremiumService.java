/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.jdautil.interactions.dispatching.InteractionContext;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.LocaleProvider;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.util.Premium;
import de.chojo.logutil.marker.LogNotify;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.config.elements.BaseSettings;
import de.chojo.repbot.config.elements.SKU;
import de.chojo.repbot.core.Threading;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.access.guild.Subscriptions;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.access.guild.subscriptions.SkuTarget;
import de.chojo.repbot.dao.access.guild.subscriptions.Subscription;
import de.chojo.repbot.dao.access.guild.subscriptions.SubscriptionError;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.exceptions.MissingSupportTier;
import de.chojo.repbot.util.SupporterFeature;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.Entitlement;
import net.dv8tion.jda.api.entities.Entitlement.EntitlementType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.entitlement.EntitlementCreateEvent;
import net.dv8tion.jda.api.events.entitlement.EntitlementDeleteEvent;
import net.dv8tion.jda.api.events.entitlement.EntitlementUpdateEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import static de.chojo.repbot.util.States.GRANT_ALL_SKU;
import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;
import static org.slf4j.LoggerFactory.getLogger;

public class PremiumService extends ListenerAdapter {
    private static final Logger log = getLogger(PremiumService.class);
    private final GuildRepository guildRepository;
    private final Configuration configuration;
    private final Localizer localizer;
    private final ShardManager shardManager;

    private PremiumService(
            GuildRepository guildRepository,
            Configuration configuration,
            Localizer localizer,
            ShardManager shardManager) {
        this.guildRepository = guildRepository;
        this.configuration = configuration;
        this.localizer = localizer;
        this.shardManager = shardManager;
    }

    public static PremiumService of(
            GuildRepository guildRepository,
            Threading threading,
            Configuration configuration,
            Localizer localizer,
            ShardManager shardManager) {
        PremiumService service = new PremiumService(guildRepository, configuration, localizer, shardManager);
        threading.repBotWorker().scheduleAtFixedRate(service::cleanExpiredEntitlements, 10, 60, TimeUnit.MINUTES);
        threading.repBotWorker().scheduleAtFixedRate(service::checkErrors, 2, 60, TimeUnit.MINUTES);
        threading.repBotWorker().scheduleAtFixedRate(service::checkGuilds, 40, 1440, TimeUnit.MINUTES);
        return service;
    }

    public boolean activateLifetime(Guild guild, de.chojo.repbot.config.elements.sku.Subscription subscription) {
        Subscription sub = new Subscription(
                subscription.subscriptionSku(),
                guild.getIdLong(),
                SkuTarget.GUILD,
                EntitlementType.PURCHASE,
                null,
                true);
        return guildRepository.guild(guild).subscriptions().addSubscription(sub);
    }

    @Override
    public void onEntitlementCreate(@NotNull EntitlementCreateEvent event) {
        updateEntitlement(event.getEntitlement());
    }

    @Override
    public void onEntitlementUpdate(@NotNull EntitlementUpdateEvent event) {
        updateEntitlement(event.getEntitlement());
    }

    @Override
    public void onEntitlementDelete(@NotNull EntitlementDeleteEvent event) {
        guildRepository
                .byId(event.getEntitlement().getGuildIdLong())
                .subscriptions()
                .deleteSubscription(Subscription.fromEntitlement(event.getEntitlement()));
    }

    @Override
    public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent event) {
        if (event.getNewNickname() != null
                && event.getUser().equals(event.getGuild().getSelfMember().getUser())) {
            if (checkForTier(event.getGuild(), SupporterFeature.BOT_NICKNAMED)) {
                BaseSettings settings = configuration.baseSettings();
                TextChannel review = event.getJDA()
                        .getShardManager()
                        .getGuildById(settings.botGuild())
                        .getTextChannelById(settings.reviewChannel());
                if (review != null && event.getNewNickname() != null) {
                    review.sendMessage("Guild `%s` changed bot nickname to `%s`"
                                    .formatted(event.getGuild(), event.getNewNickname()))
                            .queue();
                }
            }
        }
    }

    public void checkErrors() {
        List<Long> ids = query("""
                SELECT DISTINCT guild_id FROM subscription_error WHERE last_send  < ?
                """)
                .single(call().bind(
                                Instant.now().minus(skus().subscriptionErrorMessageHours(), ChronoUnit.HOURS),
                                INSTANT_TIMESTAMP))
                .mapAs(Long.class)
                .all();

        for (Long id : ids) {
            Guild guildById = shardManager.getGuildById(id);
            if (guildById == null) continue;
            RepGuild repGuild = guildRepository.guild(guildById);
            refresh(guildById);
            Subscriptions subscriptions = repGuild.subscriptions();
            List<SubscriptionError> errors = subscriptions.getExpiredErrors(skus().subscriptionErrorMessageHours());
            for (SubscriptionError error : errors) {
                checkForTier(guildById, error.type());
            }
        }
    }

    public void checkGuild(Guild guild) {
        RepGuild repGuild = guildRepository.guild(guild);
        refresh(guild);
        for (SupporterFeature feature : SupporterFeature.values()) {
            checkForTier(repGuild.load(shardManager).guild(), feature);
        }
    }

    public boolean checkForTier(Guild guild, SupporterFeature type) {
        if (guild == null) return false;
        if (GRANT_ALL_SKU) return true;
        RepGuild repGuild = guildRepository.guild(guild);
        Subscriptions subscriptions = repGuild.subscriptions();
        if (type.isEntitled(skus(), repGuild)) {
            subscriptions.deleteError(type);
            return true;
        }
        if (!type.isApplicable(skus(), repGuild)) {
            subscriptions.deleteError(type);
            return true;
        }
        handleMissingSupportTier(guild, type);
        return false;
    }

    public void handleMissingSupportTier(InteractionContext context, MissingSupportTier ex) {
        handleMissingSupportTier(context.guild(), ex.type());
    }

    public void handleMissingSupportTier(Guild guild, SupporterFeature type) {
        Settings settings = guildRepository.guild(guild).settings();
        SKU skus = skus();
        Subscriptions subscriptions = settings.repGuild().subscriptions();

        if (type == SupporterFeature.BOT_NICKNAMED) {
            try {
                guild.getSelfMember().modifyNickname(null).complete();
                subscriptions.deleteError(type);
                return;
            } catch (Exception e) {
                // ignore
            }
        }

        SubscriptionError errorMessage = subscriptions.getErrorMessage(type);
        if (errorMessage
                .lastSend()
                .isAfter(Instant.now().minus(skus.subscriptionErrorMessageHours(), ChronoUnit.HOURS))) {
            return;
        }

        List<Replacement> replacements = new ArrayList<>(type.replacements(skus, settings.repGuild()));

        int current = subscriptions.maxErrorCount();
        int timeLeft = (skus.errorThresholdBlock() - current) * skus.subscriptionErrorMessageHours();
        int timeLeftDays = Math.floorDiv(timeLeft, 24);
        int timeLeftHours = timeLeft % 24;
        replacements.add(Replacement.create("DAYS", timeLeftDays));
        replacements.add(Replacement.create("HOURS", timeLeftHours));

        var locale = localizer.context(LocaleProvider.guild(guild));
        String message = locale.localize(
                "$%s$\n$suppertererror.timeleft$\n$supportererror.considersupporting$".formatted(type.localeCode()),
                replacements.toArray(Replacement[]::new));

        List<ActionRow> buttons = Premium.buildEntitlementButtons(type.skus(skus));
        MessageCreateData data = new MessageCreateBuilder()
                .addContent(message)
                .addComponents(buttons)
                .build();

        long id = settings.general().systemChannel();
        TextChannel textChannelById = guild.getTextChannelById(id);
        log.debug("Sending message for guild {} for {}.", guild, type.name());
        var notified = false;
        if (textChannelById != null) {
            try {
                textChannelById.sendMessage(data).complete();
                notified = true;
            } catch (Exception e) {
                // ignore
            }
        }
        if (!notified) {
            Member owner = guild.retrieveOwner().complete();
            try {
                owner.getUser()
                        .openPrivateChannel()
                        .complete()
                        .sendMessage(data)
                        .complete();
                notified = true;
            } catch (Exception e) {
                log.debug("Ignoring exception while trying to send message to {} for {}.", owner, type.name());
                // TODO: find other way to communicate
            }
        }

        if (!notified) {
            // Check for other admins.
            List<Role> adminRoles = guild.getRoles().stream()
                    .filter(r -> r.hasPermission(Permission.ADMINISTRATOR))
                    .toList();
            for (Role adminRole : adminRoles) {
                if (notified) break;
                List<Member> admin;
                try {
                    admin = guild.findMembersWithRoles(adminRole).get();
                } catch (CompletionException e) {
                    continue;
                }
                for (Member member : admin) {
                    try {
                        member.getUser()
                                .openPrivateChannel()
                                .complete()
                                .sendMessage(data)
                                .complete();
                        notified = true;
                        break;
                    } catch (Exception ignored) {
                        // Ignored. We don't want to spam the user with messages.
                        log.debug("Ignoring exception while trying to send message to {} for {}.", member, type.name());
                    }
                }
            }
        }

        subscriptions.resendError(type, notified);
    }

    public void cleanExpiredEntitlements() {
        List<Subscription> all = query("""
                DELETE
                FROM
                    subscriptions
                WHERE ends_at < now()
                RETURNING id, sku, type, ends_at, purchase_type, persistent
                """).single().map(Subscription.map()).all();
        for (Subscription subscription : all) {
            guildRepository.byId(subscription.id()).subscriptions().invalidate();
        }
    }

    public void refresh(Guild guild) {
        List<Entitlement> entitlements = guild.getJDA()
                .retrieveEntitlements()
                .guild(guild.getIdLong())
                .excludeEnded(true)
                .complete();
        RepGuild repGuild = guildRepository.byId(guild.getIdLong());
        repGuild.subscriptions().clear();
        for (Entitlement entitlement : entitlements) {
            updateEntitlement(entitlement);
        }
    }

    public SKU skus() {
        return configuration.skus();
    }

    private void checkGuilds() {
        for (List<RepGuild> guild : guildRepository.guilds(200)) {
            for (RepGuild repGuild : guild) {
                repGuild.load(shardManager);
                if (repGuild.isById()) continue;
                checkGuild(repGuild.guild());
            }
        }
    }

    private void updateEntitlement(Entitlement entitlement) {
        EntitlementType type = entitlement.getType();
        Subscription sub = Subscription.fromEntitlement(entitlement, configuration.skus().isLifetime(entitlement));
        switch (type) {
            case APPLICATION_SUBSCRIPTION, DEVELOPER_GIFT, PURCHASE, FREE_PURCHASE ->
                guildRepository.byId(sub.id()).subscriptions().addSubscription(sub);
            default ->
                log.error(
                        LogNotify.NOTIFY_ADMIN,
                        "Unknown entitlement type {} for sku {} for {} {}",
                        entitlement.getType(),
                        sub.skuId(),
                        sub.skuTarget(),
                        sub.id());
        }
    }
}
