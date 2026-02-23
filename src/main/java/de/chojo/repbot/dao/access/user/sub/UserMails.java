/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.user.sub;

import de.chojo.repbot.dao.access.user.RepUser;

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
        return Optional.ofNullable(mails.get(hash));
    }

    public void addMail(String mail, MailSource source) {
        MailEntry mailEntry = MailEntry.of(user.id(), mail, source);
        mails.put(mailEntry.hash(), mailEntry);
    }
}
