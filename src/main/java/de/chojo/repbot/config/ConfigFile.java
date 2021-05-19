package de.chojo.repbot.config;

import de.chojo.repbot.config.elements.Database;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigFile {
    private String token = "";
    private String defaultPrefix = "!";
    private Database database = new Database();
    private boolean exclusiveHelp = false;
    private String magicImageLink = "";
    private int magicImagineChance = 10;
    private int magicImageCooldown = 30;
    private int magicImageDeleteSchedule = 60;
}
