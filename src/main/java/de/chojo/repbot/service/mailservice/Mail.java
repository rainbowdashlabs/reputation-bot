/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.mailservice;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public record Mail(String address, String subject, String text) {
    public static Mail kofiUserNotFound(String address, String host) {
        String subject = "Thank you for your purchase! - Link your Reputation Bot Account to your Ko-fi Account";
        ;
        String html =
                loadTemplate("link_account.html").replace("{{ mail }}", address).replace("{{ host }}", host);
        return new Mail(address, subject, html);
    }

    public static Mail accountConfirmation(String address, String host, String mailHash, String code) {
        String subject = "Confirm your email address";
        String url = "{{ host }}/user/settings?code={{ code }}&hash={{ mailhash }}"
                .replace("{{ host }}", host)
                .replace("{{ mailhash }}", mailHash)
                .replace("{{ code }}", code);

        String html = loadTemplate("account_confirmation.html")
                .replace("{{ url }}", url)
                .replace("{{ mail }}", address);
        return new Mail(address, subject, html);
    }

    private static String loadTemplate(String name) {
        try (InputStream is = Mail.class.getClassLoader().getResourceAsStream("mails/" + name)) {
            if (is == null) {
                throw new RuntimeException("Template not found: " + name);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not load mail template", e);
        }
    }
}
