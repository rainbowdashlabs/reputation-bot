package de.chojo.repbot.config;

import de.chojo.repbot.config.elements.AnalyzerSettings;
import de.chojo.repbot.config.elements.Badges;
import de.chojo.repbot.config.elements.BaseSettings;
import de.chojo.repbot.config.elements.Botlist;
import de.chojo.repbot.config.elements.Database;
import de.chojo.repbot.config.elements.Links;
import de.chojo.repbot.config.elements.MagicImage;
import de.chojo.repbot.config.elements.Migration;
import de.chojo.repbot.config.elements.PresenceSettings;
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
    private SelfCleanup selfcleanup = new SelfCleanup();
    private Migration migration = new Migration();

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

    public Migration migration() {
        return migration;
    }
}
