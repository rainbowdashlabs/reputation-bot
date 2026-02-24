/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements;

import com.google.common.hash.Hashing;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class Mailing {
    private String mailSalt = Hashing.sha256()
            .hashString(UUID.randomUUID().toString(), java.nio.charset.StandardCharsets.UTF_8)
            .toString();
    private MailSettings smtp = new MailSettings();
    private MailSettings imap = new MailSettings();
    private String user = "";
    private String password = "";
    private Map<String, String> properties = Collections.emptyMap();

    public String mailSalt() {
        return mailSalt;
    }

    public String mailHash(String mail) {
        return Hashing.sha256()
                .hashString(mail + mailSalt, java.nio.charset.StandardCharsets.UTF_8)
                .toString();
    }

    public MailSettings smtp() {
        return smtp;
    }

    public MailSettings imap() {
        return imap;
    }

    public String user() {
        return user;
    }

    public String password() {
        return password;
    }

    public Properties properties() {
        Properties props = new Properties();
        props.putAll(smtp().properties("smtp"));
        props.putAll(imap().properties("imap"));
        props.putAll(properties);
        return props;
    }
}
