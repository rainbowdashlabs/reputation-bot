/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.premium;

/**
 * A supporter feature the guild uses without being entitled to it.
 *
 * @param feature  name of the feature
 * @param path     dashboard path where it can be changed, or null if it is not managed in the dashboard
 * @param blocking whether the guild stops receiving reputation once the grace period ends
 */
public record SupporterIssuePOJO(String feature, String path, boolean blocking) {}
