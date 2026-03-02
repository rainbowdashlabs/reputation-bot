/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.chojo.jdautil.interactions.premium.SKU;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.config.elements.Kofi;
import de.chojo.repbot.config.elements.sku.Subscription;
import de.chojo.repbot.core.Threading;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.access.guild.Subscriptions;
import de.chojo.repbot.dao.access.user.RepUser;
import de.chojo.repbot.dao.access.user.sub.purchases.KofiPurchase;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.dao.provider.UserRepository;
import de.chojo.repbot.service.kofi.KofiShopItem;
import de.chojo.repbot.service.kofi.KofiTransaction;
import de.chojo.repbot.service.kofi.SubscriptionResult;
import de.chojo.repbot.service.kofi.Type;
import de.chojo.repbot.util.Result;
import io.javalin.http.UnauthorizedResponse;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KofiServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private GuildRepository guildRepository;
    @Mock
    private ShardManager shardManager;
    @Mock
    private Configuration configuration;
    @Mock
    private MailService mailService;
    @Mock
    private Threading threading;
    @Mock
    private ScheduledExecutorService scheduledExecutorService;
    @Mock
    private Kofi kofiConfig;
    @Mock
    private de.chojo.repbot.config.elements.SKU skuConfig;

    private KofiService kofiService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(threading.repBotWorker()).thenReturn(scheduledExecutorService);
        when(configuration.kofi()).thenReturn(kofiConfig);
        when(configuration.skus()).thenReturn(skuConfig);
        when(kofiConfig.token()).thenReturn("test_token");

        Subscription bronzeSub = mock(Subscription.class);
        when(bronzeSub.kofiSubscriptionId()).thenReturn("Bronze");
        when(bronzeSub.subscriptionSku()).thenReturn(111L);
        when(skuConfig.subscriptions()).thenReturn(List.of(bronzeSub));

        kofiService =
                new KofiService(userRepository, guildRepository, shardManager, configuration, mailService, threading);
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void handleShopOrder() {
        KofiTransaction authorizedData = new KofiTransaction(
                "test_token",
                "msg_123",
                Instant.now(),
                Type.SHOP_ORDER,
                true,
                "Donor",
                "Enjoy!",
                "10.00",
                "https://ko-fi.com/home",
                "donor@example.com",
                "USD",
                false,
                false,
                "trans_123",
                List.of(
                        new KofiShopItem("1a2b3c4d5e", "Blue Item", 1),
                        new KofiShopItem("a1b2c3d4e5", "Large Item", 1)
                ),
                null,
                null,
                "donor#1234",
                "987654321");

        Subscription blueSub = mock(Subscription.class);
        when(blueSub.kofiLifetimeId()).thenReturn("1a2b3c4d5e");
        when(blueSub.lifetimeSku()).thenReturn(222L);

        Subscription largeSub = mock(Subscription.class);
        when(largeSub.kofiLifetimeId()).thenReturn("a1b2c3d4e5");
        when(largeSub.lifetimeSku()).thenReturn(333L);

        when(skuConfig.subscriptions()).thenReturn(List.of(blueSub, largeSub));
        when(mailService.mailHash(authorizedData.email())).thenReturn("hashed_mail");
        when(userRepository.byMailHash("hashed_mail")).thenReturn(Optional.of(mock(RepUser.class)));

        kofiService.handle(authorizedData);

        verify(userRepository, org.mockito.Mockito.times(2)).registerPurchase(any());
    }

    @Test
    void handleUnauthorized() {
        KofiTransaction unauthorizedData = new KofiTransaction(
                "wrong_token", "msg_123", Instant.now(), Type.SUBSCRIPTION, true,
                "Donor", "Enjoy!", "5.00", "https://ko-fi.com/home", "donor@example.com",
                "USD", true, true,
                "trans_123", List.of(), null, "Bronze",
                "donor#1234", "987654321"
        );

        assertThrows(UnauthorizedResponse.class, () -> kofiService.handle(unauthorizedData));
    }

    @Test
    void handleKnownUserSubscription() {
        KofiTransaction authorizedData = new KofiTransaction(
                "test_token", "msg_123", Instant.now(), Type.SUBSCRIPTION, true,
                "Donor", "Enjoy!", "5.00", "https://ko-fi.com/home", "donor@example.com",
                "USD", true, true,
                "trans_123", List.of(), null, "Bronze",
                "donor#1234", "987654321"
        );

        when(mailService.mailHash(authorizedData.email())).thenReturn("hashed_mail");
        when(userRepository.byMailHash("hashed_mail")).thenReturn(Optional.of(mock(RepUser.class)));

        kofiService.handle(authorizedData);

        verify(userRepository).registerPurchase(any());
        verify(mailService, never()).registerVerifiedMail(anyLong(), anyString(), any());
    }

    @Test
    void handleUnknownUserSubscriptionWithDiscordId() {
        KofiTransaction authorizedData = new KofiTransaction(
                "test_token", "msg_123", Instant.now(), Type.SUBSCRIPTION, true,
                "Donor", "Enjoy!", "5.00", "https://ko-fi.com/home", "donor@example.com",
                "USD", true, true,
                "trans_123", List.of(), null, "Bronze",
                "donor#1234", "987654321"
        );

        when(mailService.mailHash(authorizedData.email())).thenReturn("hashed_mail");
        when(userRepository.byMailHash("hashed_mail")).thenReturn(Optional.empty());

        User jdaUser = mock(User.class);
        when(jdaUser.getIdLong()).thenReturn(12345L);
        RestAction<User> restAction = mock(RestAction.class);
        when(restAction.complete()).thenReturn(jdaUser);
        when(shardManager.retrieveUserById(authorizedData.discordUserId())).thenReturn(restAction);
        when(mailService.registerVerifiedMail(eq(12345L), eq(authorizedData.email()), any())).thenReturn(Result.success(null));

        kofiService.handle(authorizedData);

        verify(mailService).registerVerifiedMail(eq(12345L), eq(authorizedData.email()), any());
        verify(userRepository).registerPurchase(any());
    }

    @Test
    void enableSubscriptionSuccess() {
        KofiPurchase purchase = mock(KofiPurchase.class);
        when(purchase.type()).thenReturn(Type.SUBSCRIPTION);
        when(purchase.isValid()).thenReturn(true);
        when(purchase.skuId()).thenReturn(123L);
        when(purchase.guildId()).thenReturn(0L);
        when(purchase.assignPurchaseToGuild(anyLong())).thenReturn(true);

        Guild guild = mock(Guild.class);
        when(guild.getIdLong()).thenReturn(456L);

        RepGuild repGuild = mock(RepGuild.class);
        Subscriptions subs = mock(Subscriptions.class);
        when(guildRepository.guild(guild)).thenReturn(repGuild);
        when(repGuild.subscriptions()).thenReturn(subs);
        when(subs.isEntitled(any(SKU.class))).thenReturn(false);

        SubscriptionResult result = kofiService.enableSubscription(purchase, guild);

        assertEquals(SubscriptionResult.SUCCESS, result);
        verify(subs).addSubscription(any());
    }

    @Test
    void disableSubscription() {
        KofiPurchase purchase = mock(KofiPurchase.class);
        when(purchase.type()).thenReturn(Type.SUBSCRIPTION);
        when(purchase.guildId()).thenReturn(456L);
        when(purchase.skuId()).thenReturn(123L);
        when(purchase.unassignPurchaseFromGuild()).thenReturn(true);

        RepGuild repGuild = mock(RepGuild.class);
        Subscriptions subs = mock(Subscriptions.class);
        when(guildRepository.byId(456L)).thenReturn(repGuild);
        when(repGuild.subscriptions()).thenReturn(subs);

        boolean result = kofiService.disableSubscription(purchase);

        assertEquals(true, result);
        verify(subs).deleteSubscription(any());
    }
}
