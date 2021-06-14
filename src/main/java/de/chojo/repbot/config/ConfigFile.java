package de.chojo.repbot.config;

import de.chojo.repbot.config.elements.Badges;
import de.chojo.repbot.config.elements.Botlist;
import de.chojo.repbot.config.elements.Database;
import de.chojo.repbot.config.elements.Links;
import de.chojo.repbot.config.elements.MagicImage;
import de.chojo.repbot.config.elements.TestMode;

@SuppressWarnings("FieldMayBeFinal")
public class ConfigFile {
    private String token = "";
    private String defaultPrefix = "!";
    private Database database = new Database();
    private boolean exclusiveHelp = false;
    private MagicImage magicImage = new MagicImage();
    private TestMode testMode = new TestMode();
    private Badges badges = new Badges();
    private Links links = new Links();
    private Botlist botlist = new Botlist();

    public String token() {
        return token;
    }

    public String defaultPrefix() {
        return defaultPrefix;
    }

    public Database database() {
        return database;
    }

    public boolean isExclusiveHelp() {
        return exclusiveHelp;
    }

    public MagicImage magicImage() {
        return magicImage;
    }

    public TestMode testMode() {
        return testMode;
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
}
