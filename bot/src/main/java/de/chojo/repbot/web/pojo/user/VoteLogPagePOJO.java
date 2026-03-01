/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.user;

import de.chojo.repbot.dao.access.vote.VoteLog;

import java.util.List;

public record VoteLogPagePOJO(int page, long maxPages, List<VoteLog> content) {}
