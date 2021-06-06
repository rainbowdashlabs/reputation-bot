package de.chojo.repbot.config.elements;


public class MagicImage {
    private String magicImageLink = "";
    private int magicImagineChance = 10;
    private int magicImageCooldown = 30;
    private int magicImageDeleteSchedule = 60;

    public String magicImageLink() {
        return magicImageLink;
    }

    public int magicImagineChance() {
        return magicImagineChance;
    }

    public int magicImageCooldown() {
        return magicImageCooldown;
    }

    public int magicImageDeleteSchedule() {
        return magicImageDeleteSchedule;
    }
}
