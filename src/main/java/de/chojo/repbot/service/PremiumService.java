/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.logutil.marker.LogNotify;
import de.chojo.repbot.core.Threading;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.access.guild.subscriptions.Subscription;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.entities.Entitlement;
import net.dv8tion.jda.api.entities.Entitlement.EntitlementType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.entitlement.EntitlementCreateEvent;
import net.dv8tion.jda.api.events.entitlement.EntitlementUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static de.chojo.sadu.queries.api.query.Query.query;
import static org.slf4j.LoggerFactory.getLogger;

public class PremiumService extends ListenerAdapter {
    private static final Logger log = getLogger(PremiumService.class);
    private final GuildRepository guildRepository;

    public static PremiumService of(GuildRepository guildRepository, Threading threading) {
        PremiumService service = new PremiumService(guildRepository);
        threading.repBotWorker().scheduleAtFixedRate(service::cleanExpiredEntitlements, 2, 60, TimeUnit.MINUTES);
        return service;
    }

    private PremiumService(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onEntitlementCreate(@NotNull EntitlementCreateEvent event) {
        updateEntitlement(event.getEntitlement());
    }

    @Override
    public void onEntitlementUpdate(@NotNull EntitlementUpdateEvent event) {
        updateEntitlement(event.getEntitlement());
    }

    private void updateEntitlement(Entitlement entitlement) {
        EntitlementType type = entitlement.getType();
        Subscription sub = Subscription.fromEntitlement(entitlement);
        switch (type) {
            case APPLICATION_SUBSCRIPTION, DEVELOPER_GIFT ->
                    guildRepository.byId(sub.id()).subscriptions().addSubscription(sub);
            default -> log.error(LogNotify.NOTIFY_ADMIN, "Unknown entitlement type {} for sku {} for {} {}",
                    entitlement.getType(),
                    sub.skuId(),
                    sub.skuTarget(),
                    sub.id());
        }
    }

    public void cleanExpiredEntitlements() {
        List<Subscription> all = query("""
                DELETE
                FROM
                    subscriptions
                WHERE ends_at < now()
                RETURNING id, sku, type, ends_at
                """)
                .single()
                .map(Subscription.map())
                .all();
        for (Subscription subscription : all) {
            guildRepository.byId(subscription.id()).subscriptions().invalidate();
        }
    }

    public void refresh(Guild guild) {
        List<Entitlement> entitlements = guild.getJDA().retrieveEntitlements().guild(guild.getIdLong()).excludeEnded(true).complete();
        RepGuild repGuild = guildRepository.byId(guild.getIdLong());
        repGuild.subscriptions().clear();
        for (Entitlement entitlement : entitlements) {
            updateEntitlement(entitlement);
        }
    }
}
