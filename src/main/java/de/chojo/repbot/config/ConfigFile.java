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
import de.chojo.repbot.config.elements.SelfCleanup;

/**
 * Configuration file class that holds various settings for the application.
 */
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

    /**
     * Creates a new configuration file with default values.
     */
    public ConfigFile(){
    }

    /**
     * Returns the base settings.
     *
     * @return the base settings
     */
    public BaseSettings baseSettings() {
        return baseSettings;
    }

    /**
     * Returns the presence settings.
     *
     * @return the presence settings
     */
    public PresenceSettings presence() {
        return presenceSettings;
    }

    /**
     * Returns the analyzer settings.
     *
     * @return the analyzer settings
     */
    public AnalyzerSettings analyzerSettings() {
        return analyzerSettings;
    }

    /**
     * Returns the database settings.
     *
     * @return the database settings
     */
    public Database database() {
        return database;
    }

    /**
     * Returns the magic image settings.
     *
     * @return the magic image settings
     */
    public MagicImage magicImage() {
        return magicImage;
    }

    /**
     * Returns the badges settings.
     *
     * @return the badges settings
     */
    public Badges badges() {
        return badges;
    }

    /**
     * Returns the links settings.
     *
     * @return the links settings
     */
    public Links links() {
        return links;
    }

    /**
     * Returns the botlist settings.
     *
     * @return the botlist settings
     */
    public Botlist botlist() {
        return botlist;
    }

    /**
     * Returns the self-cleanup settings.
     *
     * @return the self-cleanup settings
     */
    public SelfCleanup selfCleanup() {
        return selfcleanup;
    }

    /**
     * Returns the API settings.
     *
     * @return the API settings
     */
    public Api api() {
        return api;
    }

    /**
     * Returns the cleanup settings.
     *
     * @return the cleanup settings
     */
    public Cleanup cleanup() {
        return cleanup;
    }
}
