/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config;

import de.chojo.repbot.config.elements.AnalyzerSettings;
import de.chojo.repbot.config.elements.Api;
import de.chojo.repbot.config.elements.Badges;
import de.chojo.repbot.config.elements.BaseSettings;
import de.chojo.repbot.config.elements.Botlist;
import de.chojo.repbot.config.elements.Cleanup;
import de.chojo.repbot.config.elements.Database;
import de.chojo.repbot.config.elements.Links;
import de.chojo.repbot.config.elements.MagicImage;
import de.chojo.repbot.config.elements.PresenceSettings;
import de.chojo.repbot.config.elements.SKU;
import de.chojo.repbot.config.elements.SelfCleanup;

@SuppressWarnings({"FieldMayBeFinal", "CanBeFinal"})
public class ConfigFile {
    private BaseSettings baseSettings = new BaseSettings();
    private PresenceSettings presenceSettings = new PresenceSettings();
    private AnalyzerSettings analyzerSettings = new AnalyzerSettings();
    private Database database = new Database();
    private MagicImage magicImage = new MagicImage();
    private Badges badges = new Badges();
    private Links links = new Links();
    private Botlist botlist = new Botlist();
    private Api api = new Api();
    private SelfCleanup selfcleanup = new SelfCleanup();
    private Cleanup cleanup = new Cleanup();
    private SKU skus = new SKU();

    public BaseSettings baseSettings() {
        return baseSettings;
    }

    public PresenceSettings presence() {
        return presenceSettings;
    }

    public AnalyzerSettings analyzerSettings() {
        return analyzerSettings;
    }

    public Database database() {
        return database;
    }

    public MagicImage magicImage() {
        return magicImage;
    }

    public Badges badges() {
        return badges;
    }

    public Links links() {
        return links;
    }

    public Botlist botlist() {
        return botlist;
    }

    public SelfCleanup selfCleanup() {
        return selfcleanup;
    }

    public Api api() {
        return api;
    }

    public Cleanup cleanup() {
        return cleanup;
    }

    public SKU skus() {
        return skus;
    }
}
