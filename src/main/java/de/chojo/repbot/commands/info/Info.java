package de.chojo.repbot.commands.info;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.info.handler.Show;
import de.chojo.repbot.config.Configuration;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.slf4j.LoggerFactory.getLogger;

public class Info extends SlashCommand {
    private static final Logger log = getLogger(Info.class);

    private Info(String version, Configuration configuration) {
        super(Slash.of("info", "command.info.description")
                .command(new Show(version, configuration)));
    }

    public static Info create(Configuration configuration) {
        var version = "undefined";
        try (var input = Info.class.getClassLoader().getResourceAsStream("version")) {
            version = new String(input.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            log.error("Could not determine version.");
        }
        return new Info(version, configuration);
    }
}
