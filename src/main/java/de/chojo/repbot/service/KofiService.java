/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.user.RepUser;
import de.chojo.repbot.dao.access.user.sub.MailSource;
import de.chojo.repbot.dao.access.user.sub.purchases.KofiPurchase;
import de.chojo.repbot.dao.provider.UserRepository;
import de.chojo.repbot.service.kofi.KofiTransaction;
import io.javalin.http.UnauthorizedResponse;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.List;
import java.util.Optional;

public class KofiService {
    private final UserRepository userRepository;
    private final ShardManager shardManager;
    private final Configuration configuration;

    public KofiService(UserRepository userRepository, ShardManager shardManager, Configuration configuration) {
        this.userRepository = userRepository;
        this.shardManager = shardManager;
        this.configuration = configuration;
    }

    public void handle(KofiTransaction data) {
        if (!configuration.kofi().token().equals(data.verificationToken())) {
            throw new UnauthorizedResponse();
        }
        Optional<User> user = resolveUser(data);
        if (user.isPresent()) {
            // Register email adress for that user if there is a registered discord account.
            RepUser repUser = userRepository.byUser(user.get());
            repUser.mails().addMail(data.email(), MailSource.KOFI);
        }

        List<KofiPurchase> kofiPurchases = KofiPurchase.create(data, configuration);
        for (KofiPurchase kofiPurchase : kofiPurchases) {
            userRepository.registerPurchase(kofiPurchase);
        }
    }

    private Optional<User> resolveUser(KofiTransaction data) {
        if (data.discordUserId() == null) return Optional.empty();
        try {
            return Optional.of(
                    shardManager.retrieveUserById(data.discordUserId()).complete());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
