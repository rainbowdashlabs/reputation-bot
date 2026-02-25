/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.util;

import static com.google.common.hash.Hashing.sha256;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class Hashing {
    public static String hashMail(String mail) {
        return sha256().hashString(mail, UTF_8).toString();
    }
}
