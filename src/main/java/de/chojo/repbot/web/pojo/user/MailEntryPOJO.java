/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.user;

import de.chojo.repbot.dao.access.user.sub.MailEntry;

public record MailEntryPOJO(
        long userId,
        String source,
        String hash,
        String mailShort,
        boolean verified,
        String verificationRequested
) {
    public static MailEntryPOJO of(MailEntry entry) {
        return new MailEntryPOJO(
                entry.userId(),
                entry.source().name(),
                entry.hash(),
                entry.mailShort(),
                entry.verified(),
                entry.verificationRequested() != null ? entry.verificationRequested().toString() : null
        );
    }
}
