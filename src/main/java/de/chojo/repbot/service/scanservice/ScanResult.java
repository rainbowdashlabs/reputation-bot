/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.scanservice;

import de.chojo.repbot.web.pojo.scan.ScanProgress;

import java.time.Instant;

public record ScanResult(ScanProgress progress, Instant start, Instant end) {}
