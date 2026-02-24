/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.user.RepUser;
import de.chojo.repbot.dao.access.user.sub.MailEntry;
import de.chojo.repbot.dao.access.user.sub.MailSource;
import de.chojo.repbot.dao.access.user.sub.purchases.KofiPurchase;
import de.chojo.repbot.dao.provider.UserRepository;
import de.chojo.repbot.service.kofi.KofiTransaction;
import de.chojo.repbot.service.mailservice.FailureReason;
import de.chojo.repbot.service.mailservice.Mail;
import de.chojo.repbot.util.LogNotify;
import de.chojo.repbot.util.Result;
import io.javalin.http.UnauthorizedResponse;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class KofiService {
    private static final Logger log = getLogger(KofiService.class);
    private final UserRepository userRepository;
    private final ShardManager shardManager;
    private final Configuration configuration;
    private final MailService mailService;

    public KofiService(
            UserRepository userRepository,
            ShardManager shardManager,
            Configuration configuration,
            MailService mailService) {
        this.userRepository = userRepository;
        this.shardManager = shardManager;
        this.configuration = configuration;
        this.mailService = mailService;
    }

    public void handle(KofiTransaction data) {
        if (!configuration.kofi().token().equals(data.verificationToken())) {
            throw new UnauthorizedResponse();
        }

        // Check whether we know the user already that purchased something.
        Optional<RepUser> repUser = userRepository.byMailHash(mailService.mailHash(data.email()));
        if (repUser.isEmpty()) {
            Optional<User> user = resolveUser(data);
            if (user.isPresent()) {
                // Register email adress for that user if there is a registered discord account.
                Result<MailEntry, FailureReason> mailEntryFailureReasonResult =
                        mailService.registerVerifiedMail(user.get().getIdLong(), data.email(), MailSource.KOFI);
                if (mailEntryFailureReasonResult.isFailure()) {
                    log.error(
                            LogNotify.NOTIFY_ADMIN,
                            "Could not register verified mail {} for user {}: {}",
                            data.email(),
                            user.get().getIdLong(),
                            mailEntryFailureReasonResult.failureReason());
                }
            } else {
                mailService.sendMail(
                        Mail.kofiUserNotFound(data.email(), configuration.api().url()));
            }
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

    public boolean enableSubscription(KofiPurchase purchase, Guild guild) {
        purchase.assignPurchaseToGuild(guild.getIdLong());
        return true;
    }

    public boolean disableSubscription(KofiPurchase purchase) {
        purchase.unassignPurchaseFromGuild();
        return true;
    }
}
