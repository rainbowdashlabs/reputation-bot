/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.repbot.dao.provider.UserRepository;
import de.chojo.repbot.service.kofi.KofiTransaction;

public class KofiService {
    private final UserRepository userRepository;

    public KofiService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void handle(KofiTransaction data) {

    }
}
