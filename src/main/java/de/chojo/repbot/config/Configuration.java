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
import de.chojo.repbot.config.elements.SKU;
import de.chojo.repbot.config.elements.SelfCleanup;
import de.chojo.repbot.config.exception.ConfigurationException;
import de.chojo.repbot.config.jackson.BotModule;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.slf4j.LoggerFactory.getLogger;

public class Configuration {
    private static final Logger log = getLogger(Configuration.class);
    private final ObjectMapper objectMapper;
    private ConfigFile configFile;

    private Configuration() {
        objectMapper = JsonMapper.builder()
                .configure(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS, true)
                .build()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
                .setDefaultPrettyPrinter(new DefaultPrettyPrinter())
                .registerModule(new BotModule());
    }

    public static Configuration create() {
        var configuration = new Configuration();
        configuration.reload();
        return configuration;
    }

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

    private void save() throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValues(getConfig().toFile()).write(configFile);
    }

    private void reloadFile() throws IOException {
        forceConsistency();
        configFile = objectMapper.readValue(getConfig().toFile(), ConfigFile.class);
    }

    private void forceConsistency() throws IOException {
        Files.createDirectories(getConfig().getParent());
        if (!getConfig().toFile().exists()) {
            if (getConfig().toFile().createNewFile()) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValues(getConfig().toFile()).write(new ConfigFile());
                throw new ConfigurationException("Please configure the config.");
            }
        }
    }

    private Path getConfig() {
        var home = new File(".").getAbsoluteFile().getParentFile().toPath();
        var property = System.getProperty("bot.config");
        if (property == null) {
            log.error("bot.config property is not set.");
            throw new ConfigurationException("Property -Dbot.config=<config path> is not set.");
        }
        return Paths.get(home.toString(), property);
    }

    public Database database() {
        return configFile.database();
    }

    public BaseSettings baseSettings() {
        return configFile.baseSettings();
    }

    public AnalyzerSettings analyzerSettings() {
        return configFile.analyzerSettings();
    }

    public MagicImage magicImage() {
        return configFile.magicImage();
    }

    public Badges badges() {
        return configFile.badges();
    }

    public Links links() {
        return configFile.links();
    }

    public Botlist botlist() {
        return configFile.botlist();
    }

    public PresenceSettings presence() {
        return configFile.presence();
    }

    public SelfCleanup selfCleanup() {
        return configFile.selfCleanup();
    }

    public Api api() {
        return configFile.api();
    }

    public Cleanup cleanup() {
        return configFile.cleanup();
    }

    public SKU skus() {
        return configFile.skus();
    }
}
