/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.provider;

import de.chojo.repbot.dao.access.guild.subscriptions.TokenPurchase;

import java.util.List;

import static de.chojo.sadu.queries.api.query.Query.query;

public class TokenPurchaseRepository {
    public List<TokenPurchase> expiredPurchases() {
        return query("""
                SELECT
                    guild_id,
                    feature_id,
                    expires,
                    auto_renewal
                FROM
                    token_purchases
                WHERE expires < now();""").single().mapAs(TokenPurchase.class).all();
    }
}
