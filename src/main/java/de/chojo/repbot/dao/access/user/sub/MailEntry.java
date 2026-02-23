/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.user.sub;

import com.google.common.hash.Hashing;
import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.wrapper.Row;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public class MailEntry {
    private static final Pattern MAIL_SHORTER = Pattern.compile("(.{2}).+?@.+?(.{2}\\..+)");
    private final long userId;
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

    public static MailEntry of(long userId, String mail, MailSource source) {
        Matcher matcher = MAIL_SHORTER.matcher(mail);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Mail is not valid");
        }

        var mailShort = "%s***@***%s".formatted(matcher.group(1), matcher.group(2));
        var mailHash =
                Hashing.sha256().hashString(mail, Charset.defaultCharset()).toString();

        return new MailEntry(
                userId,
                source,
                mailHash,
                mailShort,
                false,
                Instant.now(),
                UUID.randomUUID().toString());
    }

    public void verify() {
        query("""
                UPDATE user_mails SET verified = TRUE WHERE mail_hash = ?
                """).single(call().bind(hash)).update().ifChanged(i -> this.verified = true);
    }

    public void regenerateVerificationCode() {
        verificationCode = UUID.randomUUID().toString();
        verificationRequested = Instant.now();
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
}
