/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.user.sub;

import de.chojo.repbot.dao.access.user.RepUser;
import de.chojo.sadu.queries.converter.StandardValueConverter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class UserMails {
    private final RepUser user;
    private Map<String, MailEntry> mails;

    public UserMails(RepUser user) {
        this.user = user;
    }

    public Map<String, MailEntry> mails() {
        if (mails == null) {
            mails = new HashMap<>();
            query("""
                    SELECT
                        user_id,
                        source,
                        mail_hash,
                        mail_short,
                        verified,
                        verification_requested,
                        verification_code
                    FROM
                        user_mails
                    WHERE user_id = ?;
                    """)
                    .single(call().bind(user.id()))
                    .mapAs(MailEntry.class)
                    .all()
                    .forEach(m -> mails.put(m.hash(), m));
        }
        return mails;
    }

    public Optional<MailEntry> getMail(String hash) {
        return Optional.ofNullable(mails().get(hash));
    }

    public void addMail(MailEntry mailEntry) {
        query("""
                INSERT INTO user_mails (user_id, source, mail_hash, mail_short, verified, verification_requested, verification_code) VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING;
                """)
                .single(call().bind(user.id())
                        .bind(mailEntry.source())
                        .bind(mailEntry.hash())
                        .bind(mailEntry.mailShort())
                        .bind(mailEntry.verified())
                        .bind(mailEntry.verificationRequested(), StandardValueConverter.INSTANT_TIMESTAMP)
                        .bind(mailEntry.verificationCode()))
                .insert()
                .ifChanged(i -> mails().put(mailEntry.hash(), mailEntry));
    }

    public boolean removeMail(String mailHash) {
        return query("""
                        DELETE FROM user_mails WHERE user_id = ? AND mail_hash = ?;
                        """).single(call().bind(user.id()).bind(mailHash)).update().ifChanged(ii -> mails().remove(
                        mailHash));
    }
}
