/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub;

import de.chojo.repbot.dao.access.guildsession.SettingsAuditLog;

import java.util.List;

public record AuditLogPagePOJO(int page, long maxPages, List<SettingsAuditLog> content) {}
