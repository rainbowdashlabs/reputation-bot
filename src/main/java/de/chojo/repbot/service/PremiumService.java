/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.jdautil.interactions.dispatching.InteractionContext;
import de.chojo.jdautil.localization.LocalizationContext;
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
import de.chojo.repbot.dao.access.guild.subscriptions.SkuTarget;
import de.chojo.repbot.dao.access.guild.subscriptions.Subscription;
import de.chojo.repbot.dao.access.guild.subscriptions.SubscriptionError;
import de.chojo.repbot.dao.access.guild.subscriptions.SubscriptionSource;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.exceptions.MissingSupportTier;
import de.chojo.repbot.util.SupporterFeature;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Entitlement;
import net.dv8tion.jda.api.entities.Entitlement.EntitlementType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.entitlement.EntitlementCreateEvent;
import net.dv8tion.jda.api.events.entitlement.EntitlementDeleteEvent;
import net.dv8tion.jda.api.events.entitlement.EntitlementUpdateEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.chojo.repbot.util.States.GRANT_ALL_SKU;
import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;
import static org.slf4j.LoggerFactory.getLogger;

public class PremiumService extends ListenerAdapter {
    private static final Logger log = getLogger(PremiumService.class);
    private static final String BLOCKING_TYPES = Arrays.stream(SupporterFeature.values())
            .filter(SupporterFeature::isBlocking)
            .map(feature -> "'%s'".formatted(feature.name()))
            .collect(Collectors.joining(", "));
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
                SubscriptionSource.DISCORD,
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
        resetUnentitledAvatar(shardManager.getGuildById(event.getEntitlement().getGuildIdLong()));
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
                SELECT DISTINCT guild_id FROM subscription_error WHERE last_send < ? AND (NOT notified OR type IN (%s))
                """.formatted(BLOCKING_TYPES))
                .single(call().bind(
                                Instant.now().minus(skus().subscriptionErrorMessageHours(), ChronoUnit.HOURS),
                                INSTANT_TIMESTAMP))
                .mapAs(Long.class)
                .all();

        for (Long id : ids) {
            Guild guildById = shardManager.getGuildById(id);
            if (guildById == null) continue;
            checkGuild(guildById);
        }
    }

    /**
     * Refreshes the entitlements of the guild and notifies it about every supporter feature it uses without being
     * entitled to it.
     *
     * @param guild guild to check
     */
    public void checkGuild(Guild guild) {
        refresh(guild);
        resetUnentitledAvatar(guild);
        notifyUnentitledUse(guild);
    }

    /**
     * Removes the bot's server avatar when the guild is not entitled to a custom profile. The bot can always reset
     * its own avatar, so this needs no notice.
     *
     * @param guild guild to check
     */
    public void resetUnentitledAvatar(Guild guild) {
        if (guild == null || GRANT_ALL_SKU) return;
        if (guild.getSelfMember().getAvatarId() == null) return;
        RepGuild repGuild = guildRepository.guild(guild);
        if (!repGuild.subscriptions().isLoaded()) return;
        if (SupporterFeature.BOT_NICKNAMED.isEntitled(skus(), repGuild)) return;
        if (!repGuild.settings().profile().profilePicture(null)) {
            log.warn("Could not reset the bot avatar in {}.", guild);
        }
    }

    /**
     * Checks whether the guild may use the feature and notifies it about every unentitled use if it may not.
     *
     * @param guild guild to check
     * @param type  feature to check
     * @return true if the guild is entitled to the feature or does not use it
     */
    public boolean checkForTier(Guild guild, SupporterFeature type) {
        if (guild == null) return false;
        if (GRANT_ALL_SKU) return true;
        RepGuild repGuild = guildRepository.guild(guild);
        if (isUnentitledUse(repGuild, type)) {
            notifyUnentitledUse(guild);
            return false;
        }
        repGuild.subscriptions().deleteError(type);
        return true;
    }

    public void handleMissingSupportTier(InteractionContext context, MissingSupportTier ex) {
        notifyUnentitledUse(context.guild());
    }

    /**
     * Collects every supporter feature the guild uses without entitlement and sends one notice listing all of them.
     * <p>
     * Errors of features that are no longer used are removed. A notice is only sent when a feature is due (see
     * {@link SubscriptionError#isDue(Instant)}). It lists every blocking feature and the paused features that are due,
     * so a paused feature is reported once; every listed feature is then marked as reported.
     *
     * @param guild guild to notify
     */
    public void notifyUnentitledUse(Guild guild) {
        if (guild == null || GRANT_ALL_SKU) return;
        Subscriptions subscriptions = guildRepository.guild(guild).subscriptions();
        List<SupporterFeature> problems = collectUnentitledUse(guild);
        if (problems.isEmpty()) return;

        Instant dueBefore = Instant.now().minus(skus().subscriptionErrorMessageHours(), ChronoUnit.HOURS);
        List<SupporterFeature> listed = listedInNotice(problems, subscriptions::getErrorMessage, dueBefore);
        if (listed.isEmpty()) return;

        log.debug("Sending supporter notice for guild {} for {}.", guild, listed);
        boolean notified = deliver(guild, buildNotice(guild, listed));
        for (SupporterFeature problem : listed) {
            subscriptions.resendError(problem, notified);
        }
    }

    /**
     * The problems a notice lists: none if no problem is due, otherwise every blocking problem and the paused
     * problems that are due.
     *
     * @param problems  features the guild uses without entitlement
     * @param errors    the reported error of a feature
     * @param dueBefore errors last sent before this moment are due
     * @return problems to list, empty if no notice is due
     */
    static List<SupporterFeature> listedInNotice(
            List<SupporterFeature> problems, Function<SupporterFeature, SubscriptionError> errors, Instant dueBefore) {
        List<SupporterFeature> due = problems.stream()
                .filter(problem -> errors.apply(problem).isDue(dueBefore))
                .toList();
        if (due.isEmpty()) return List.of();
        return problems.stream()
                .filter(problem -> problem.isBlocking() || due.contains(problem))
                .toList();
    }

    private List<SupporterFeature> collectUnentitledUse(Guild guild) {
        RepGuild repGuild = guildRepository.guild(guild);
        Subscriptions subscriptions = repGuild.subscriptions();
        List<SupporterFeature> problems = new ArrayList<>();
        for (SupporterFeature feature : SupporterFeature.values()) {
            if (isUnentitledUse(repGuild, feature) && !resolveAutomatically(guild, feature)) {
                problems.add(feature);
            } else {
                subscriptions.deleteError(feature);
            }
        }
        return problems;
    }

    private boolean isUnentitledUse(RepGuild repGuild, SupporterFeature feature) {
        return feature.isUnentitledUse(skus(), repGuild);
    }

    private boolean resolveAutomatically(Guild guild, SupporterFeature feature) {
        if (feature != SupporterFeature.BOT_NICKNAMED) return false;
        try {
            guild.getSelfMember().modifyNickname(null).complete();
            return true;
        } catch (Exception e) {
            log.debug("Could not reset the nickname in {}.", guild, e);
            return false;
        }
    }

    private MessageCreateData buildNotice(Guild guild, List<SupporterFeature> problems) {
        RepGuild repGuild = guildRepository.guild(guild);
        var locale = localizer.context(LocaleProvider.guild(guild));

        List<String> lines = new ArrayList<>();
        lines.add(locale.localize(
                "supportererror.server", Replacement.create("GUILD", MarkdownSanitizer.escape(guild.getName()))));
        List<SupporterFeature> blocking =
                problems.stream().filter(SupporterFeature::isBlocking).toList();
        List<SupporterFeature> paused =
                problems.stream().filter(problem -> !problem.isBlocking()).toList();
        blocking.forEach(problem -> lines.add(problemLine(locale, repGuild, problem)));
        repGuild.subscriptions().blocksAt(blocking).ifPresent(blocksAt -> {
            long hoursLeft =
                    Math.max(0, Duration.between(Instant.now(), blocksAt).toHours());
            lines.add(locale.localize(
                    "suppertererror.timeleft",
                    Replacement.create("DAYS", hoursLeft / 24),
                    Replacement.create("HOURS", hoursLeft % 24)));
        });
        paused.forEach(problem -> lines.add(problemLine(locale, repGuild, problem)));
        if (!paused.isEmpty()) lines.add(locale.localize("supportererror.paused"));
        lines.add(locale.localize("supportererror.considersupporting"));
        String content = String.join("\n", lines);
        if (content.length() > Message.MAX_CONTENT_LENGTH) {
            content = content.substring(0, Message.MAX_CONTENT_LENGTH);
        }

        return new MessageCreateBuilder()
                .addContent(content)
                .addComponents(noticeButtons(guild, problems, locale))
                .build();
    }

    private String problemLine(LocalizationContext locale, RepGuild repGuild, SupporterFeature problem) {
        return "- "
                + locale.localize(
                        problem.localeCode(),
                        problem.replacements(skus(), repGuild).toArray(Replacement[]::new));
    }

    private List<ActionRow> noticeButtons(Guild guild, List<SupporterFeature> problems, LocalizationContext locale) {
        List<Button> settingsButtons = problems.stream()
                .map(SupporterFeature::dashboardSection)
                .flatMap(Optional::stream)
                .distinct()
                .map(section -> Button.link(
                        configuration.api().pathUrl(guild.getIdLong(), section.path()),
                        locale.localize(
                                "supportererror.opensettings",
                                Replacement.create("SECTION", locale.localize(section.localeKey())))))
                .toList();
        Set<Long> skuIds = problems.stream()
                .flatMap(problem -> problem.skus(skus()).sku().stream())
                .map(de.chojo.jdautil.interactions.premium.SKU::skuId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<ActionRow> rows = new ArrayList<>(ActionRow.partitionOf(settingsButtons));
        rows.addAll(Premium.buildEntitlementButtons(skuIds));
        return rows.subList(0, Math.min(rows.size(), Message.MAX_COMPONENT_COUNT));
    }

    private boolean deliver(Guild guild, MessageCreateData data) {
        long id = guildRepository.guild(guild).settings().general().systemChannel();
        TextChannel systemChannel = guild.getTextChannelById(id);
        if (systemChannel != null) {
            try {
                systemChannel.sendMessage(data).complete();
                return true;
            } catch (Exception e) {
                log.debug("Could not send supporter notice to system channel of {}.", guild, e);
            }
        }

        Member owner = guild.retrieveOwner().complete();
        if (sendDirect(owner, data)) return true;

        List<Role> adminRoles = guild.getRoles().stream()
                .filter(r -> r.hasPermission(Permission.ADMINISTRATOR))
                .toList();
        for (Role adminRole : adminRoles) {
            List<Member> admins;
            try {
                admins = guild.findMembersWithRoles(adminRole).get();
            } catch (CompletionException e) {
                continue;
            }
            for (Member admin : admins) {
                if (sendDirect(admin, data)) return true;
            }
        }
        // TODO: reach guilds whose system channel, owner and admins all refuse the notice
        return false;
    }

    private boolean sendDirect(Member member, MessageCreateData data) {
        try {
            member.getUser().openPrivateChannel().complete().sendMessage(data).complete();
            return true;
        } catch (Exception e) {
            log.debug("Could not send supporter notice to {}.", member, e);
            return false;
        }
    }

    public void cleanExpiredEntitlements() {
        List<Subscription> all = query("""
                DELETE
                FROM
                    subscriptions
                WHERE ends_at < now()
                RETURNING id, sku, type, ends_at, source, purchase_type, persistent
                """).single().map(Subscription.map()).all();
        for (Subscription subscription : all) {
            guildRepository.byId(subscription.id()).subscriptions().invalidate();
            if (subscription.skuTarget() == SkuTarget.GUILD) {
                resetUnentitledAvatar(shardManager.getGuildById(subscription.id()));
            }
        }
    }

    public void refresh(Guild guild) {
        List<Entitlement> entitlements = guild.getJDA()
                .retrieveEntitlements()
                .guild(guild.getIdLong())
                .excludeEnded(true)
                .complete();
        RepGuild repGuild = guildRepository.guild(guild);
        repGuild.subscriptions().clear(SubscriptionSource.DISCORD);
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
        Subscription sub =
                Subscription.fromEntitlement(entitlement, configuration.skus().isLifetime(entitlement));
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
