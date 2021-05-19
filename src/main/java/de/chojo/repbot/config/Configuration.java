package de.chojo.repbot.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.chojo.repbot.config.elements.Database;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@Slf4j
public class Configuration {
    private final ObjectMapper objectMapper;
    private ConfigFile configFile;

    private Configuration() {
        objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.setDefaultPrettyPrinter(new DefaultPrettyPrinter());
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
        }
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
                throw new RuntimeException("Please configure the config.");
            }
        }
    }

    private Path getConfig() {
        var home = new File(".").getAbsoluteFile().getParentFile().toPath();
        var property = System.getProperty("bot.config");
        if (property == null) {
            log.error("bot.config property is not set.");
        }
        return Paths.get(home.toString(), property);
    }

    // DELEGATES
    public String getToken() {
        return configFile.getToken();
    }

    public void setToken(String token) {
        configFile.setToken(token);
    }

    public String getDefaultPrefix() {
        return configFile.getDefaultPrefix();
    }

    public void setDefaultPrefix(String defaultPrefix) {
        configFile.setDefaultPrefix(defaultPrefix);
    }

    public Database getDatabase() {
        return configFile.getDatabase();
    }

    public void setDatabase(Database database) {
        configFile.setDatabase(database);
    }

    public boolean isExclusiveHelp() {
        return configFile.isExclusiveHelp();
    }

    public void setExclusiveHelp(boolean exclusiveHelp) {
        configFile.setExclusiveHelp(exclusiveHelp);
    }

    public boolean canEqual(Object other) {
        return configFile.canEqual(other);
    }
    
    public String getMagicImageLink() {
        return configFile.getMagicImageLink();
    }
    
    public void setMagicImageLink(String magicImageLink) {
        configFile.setMagicImageLink(magicImageLink);
    }
    
    public int getMagicImagineChance() {
        return configFile.getMagicImagineChance();
    }
    
    public void setMagicImagineChance(int magicImagineChance) {
        configFile.setMagicImagineChance(magicImagineChance);
    }
    
    public int getMagicImageCooldown() {
        return configFile.getMagicImageCooldown();
    }
    
    public void setMagicImageCooldown(int magicImageCooldown) {
        configFile.setMagicImageCooldown(magicImageCooldown);
    }
    
    public int getMagicImageDeleteSchedule() {
        return configFile.getMagicImageDeleteSchedule();
    }
    
    public void setMagicImageDeleteSchedule(int magicImageDeleteSchedule) {
        configFile.setMagicImageDeleteSchedule(magicImageDeleteSchedule);
    }
}
