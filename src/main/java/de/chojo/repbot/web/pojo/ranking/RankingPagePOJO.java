/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.ranking;

import java.util.List;

public record RankingPagePOJO(int pages, int page, List<RankingEntryPOJO> entries) {}
