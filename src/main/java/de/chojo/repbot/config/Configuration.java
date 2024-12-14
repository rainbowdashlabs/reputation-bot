/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
import de.chojo.repbot.config.exception.ConfigurationException;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Configuration class responsible for loading and managing the bot's configuration settings.
 */
public class Configuration {
    private static final Logger log = getLogger(Configuration.class);
    private final ObjectMapper objectMapper;
    private ConfigFile configFile;

    /**
     * Constructs a Configuration instance and initializes the ObjectMapper.
     */
    private Configuration() {
        objectMapper = JsonMapper.builder()
                .configure(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS, true)
                .build()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
                .setDefaultPrettyPrinter(new DefaultPrettyPrinter());
    }

    /**
     * Creates and initializes a Configuration instance.
     *
     * @return a new Configuration instance
     */
    public static Configuration create() {
        var configuration = new Configuration();
        configuration.reload();
        return configuration;
    }

    /**
     * Reloads the configuration from the file and saves it.
     */
    public void reload() {
        try {
            reloadFile();
        } catch (IOException e) {
            log.info("Could not load config", e);
            throw new ConfigurationException("Could not load config file", e);
        }
        try {
            save();
        } catch (IOException e) {
            log.error("Could not save config.", e);
        }
    }

    /**
     * Saves the current configuration to the file.
     *
     * @throws IOException if an I/O error occurs
     */
    private void save() throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValues(getConfig().toFile()).write(configFile);
    }

    /**
     * Reloads the configuration file and ensures consistency.
     *
     * @throws IOException if an I/O error occurs
     */
    private void reloadFile() throws IOException {
        forceConsistency();
        configFile = objectMapper.readValue(getConfig().toFile(), ConfigFile.class);
    }

    /**
     * Ensures the configuration file and its directories exist.
     *
     * @throws IOException if an I/O error occurs
     */
    private void forceConsistency() throws IOException {
        Files.createDirectories(getConfig().getParent());
        if (!getConfig().toFile().exists()) {
            if (getConfig().toFile().createNewFile()) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValues(getConfig().toFile()).write(new ConfigFile());
                throw new ConfigurationException("Please configure the config.");
            }
        }
    }

    /**
     * Gets the path to the configuration file.
     *
     * @return the path to the configuration file
     */
    private Path getConfig() {
        var home = new File(".").getAbsoluteFile().getParentFile().toPath();
        var property = System.getProperty("bot.config");
        if (property == null) {
            log.error("bot.config property is not set.");
            throw new ConfigurationException("Property -Dbot.config=<config path> is not set.");
        }
        return Paths.get(home.toString(), property);
    }

    /**
     * Gets the database configuration.
     *
     * @return the database configuration
     */
    public Database database() {
        return configFile.database();
    }

    /**
     * Gets the base settings configuration.
     *
     * @return the base settings configuration
     */
    public BaseSettings baseSettings() {
        return configFile.baseSettings();
    }

    /**
     * Gets the analyzer settings configuration.
     *
     * @return the analyzer settings configuration
     */
    public AnalyzerSettings analyzerSettings() {
        return configFile.analyzerSettings();
    }

    /**
     * Gets the magic image configuration.
     *
     * @return the magic image configuration
     */
    public MagicImage magicImage() {
        return configFile.magicImage();
    }

    /**
     * Gets the badges configuration.
     *
     * @return the badges configuration
     */
    public Badges badges() {
        return configFile.badges();
    }

    /**
     * Gets the links configuration.
     *
     * @return the links configuration
     */
    public Links links() {
        return configFile.links();
    }

    /**
     * Gets the botlist configuration.
     *
     * @return the botlist configuration
     */
    public Botlist botlist() {
        return configFile.botlist();
    }

    /**
     * Gets the presence settings configuration.
     *
     * @return the presence settings configuration
     */
    public PresenceSettings presence() {
        return configFile.presence();
    }

    /**
     * Gets the self-cleanup configuration.
     *
     * @return the self-cleanup configuration
     */
    public SelfCleanup selfCleanup() {
        return configFile.selfCleanup();
    }

    /**
     * Gets the API configuration.
     *
     * @return the API configuration
     */
    public Api api() {
        return configFile.api();
    }

    /**
     * Gets the cleanup configuration.
     *
     * @return the cleanup configuration
     */
    public Cleanup cleanup() {
        return configFile.cleanup();
    }
}
