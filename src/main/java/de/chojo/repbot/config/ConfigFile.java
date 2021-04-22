package de.chojo.repbot.config;

import de.chojo.repbot.config.elements.Database;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ConfigFile {
    private String token = "";
    private Database database = new Database();
}
