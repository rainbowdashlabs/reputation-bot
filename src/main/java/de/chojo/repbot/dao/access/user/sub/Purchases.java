/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.user.sub;

import de.chojo.repbot.dao.access.user.RepUser;
import de.chojo.repbot.dao.access.user.sub.purchases.KofiPurchase;

import java.util.List;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class Purchases {
    private final RepUser repUser;

    public Purchases(RepUser repUser) {
        this.repUser = repUser;
    }

    public List<KofiPurchase> all() {
        return query("""
                SELECT
                    kp.id,
                    kp.mail_hash,
                    kp.key,
                    kp.sku_id,
                    kp.type,
                    kp.expires_at,
                    kp.transaction_id,
                    kp.guild_id
                FROM
                    user_mails um
                        LEFT JOIN kofi_purchase kp
                        ON um.mail_hash = kp.mail_hash
                WHERE user_id = ? AND verified;""")
                .single(call().bind(repUser.id()))
                .mapAs(KofiPurchase.class)
                .all();
    }

    public Optional<KofiPurchase> byId(long id) {
        return query("""
                SELECT
                    kp.id,
                    kp.mail_hash,
                    kp.key,
                    kp.sku_id,
                    kp.type,
                    kp.expires_at,
                    kp.transaction_id,
                    kp.guild_id
                FROM
                    user_mails um
                        LEFT JOIN kofi_purchase kp
                        ON um.mail_hash = kp.mail_hash
                WHERE um.user_id = ? AND kp.id = ? AND um.verified;""")
                .single(call().bind(repUser.id()).bind(id))
                .mapAs(KofiPurchase.class)
                .first();
    }
}
