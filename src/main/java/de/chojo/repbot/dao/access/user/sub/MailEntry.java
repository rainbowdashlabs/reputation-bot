/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.user.sub;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.wrapper.Row;

import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

/**
 * Represents a mail entry tied to a user.
 */
public class MailEntry {
    private long userId;
    private final MailSource source;
    private final String hash;
    private final String mailShort;
    private boolean verified;
    private Instant verificationRequested;
    private String verificationCode;

    @MappingProvider({
        "id",
        "source",
        "mail_hash",
        "mail_short",
        "verified",
        "verification_requested",
        "verification_code"
    })
    public MailEntry(Row row) throws SQLException {
        this(
                row.getLong("user_id"),
                row.getEnum("source", MailSource.class),
                row.getString("mail_hash"),
                row.getString("mail_short"),
                row.getBoolean("verified"),
                row.get("verification_requested", INSTANT_TIMESTAMP),
                row.getString("verification_code"));
    }

    public MailEntry(
            long userId,
            MailSource source,
            String hash,
            String mailShort,
            boolean verified,
            Instant verificationRequested,
            String verificationCode) {
        this.userId = userId;
        this.source = source;
        this.hash = hash;
        this.mailShort = mailShort;
        this.verified = verified;
        this.verificationRequested = verificationRequested;
        this.verificationCode = verificationCode;
    }

    public void verify() {
        query("""
                UPDATE user_mails SET verified = TRUE WHERE mail_hash = ?
                """).single(call().bind(hash)).update().ifChanged(i -> this.verified = true);
    }

    public void regenerateVerificationCode() {
        var newVerificationCode = UUID.randomUUID().toString();
        var newVerificationRequested = Instant.now();
        query("""
                UPDATE user_mails SET verification_code = ?, verification_requested = ? WHERE mail_hash = ?
                """)
                .single(call().bind(newVerificationCode)
                        .bind(newVerificationRequested, INSTANT_TIMESTAMP)
                        .bind(hash))
                .update()
                .ifChanged(i -> {
                    this.verificationCode = newVerificationCode;
                    this.verificationRequested = newVerificationRequested;
                });
    }

    public long userId() {
        return userId;
    }

    public MailSource source() {
        return source;
    }

    public String hash() {
        return hash;
    }

    public String mailShort() {
        return mailShort;
    }

    public boolean verified() {
        return verified;
    }

    public Instant verificationRequested() {
        return verificationRequested;
    }

    public String verificationCode() {
        return verificationCode;
    }

    /**
     * Update the user tied to this mail entry.
     * Regenerates the verification code.
     * @param user user to update
     */
    public void updateUser(long user) {
        query("""
                UPDATE user_mails SET user_id = ? WHERE mail_hash = ?
                """).single(call().bind(user).bind(hash)).update().ifChanged(i -> {
            this.userId = user;
            regenerateVerificationCode();
        });
    }
}
