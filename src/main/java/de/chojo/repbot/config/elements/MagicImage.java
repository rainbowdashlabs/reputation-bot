/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements;

/**
 * The magic image configuration.
 */
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})
public class MagicImage {
    /**
     * The link to the magic image.
     */
    private String magicImageLink = "";

    /**
     * The chance of imagining a magic image.
     */
    private int magicImagineChance = 10;

    /**
     * The cooldown period for the magic image.
     */
    private int magicImageCooldown = 30;

    /**
     * The schedule for deleting the magic image.
     */
    private int magicImageDeleteSchedule = 60;

    /**
     * Creates a new magic image configuration with default values.
     */
    public MagicImage(){
    }

    /**
     * Returns the link to the magic image.
     *
     * @return the magic image link
     */
    public String magicImageLink() {
        return magicImageLink;
    }

    /**
     * Returns the chance of imagining a magic image.
     *
     * @return the magic imagine chance
     */
    public int magicImagineChance() {
        return magicImagineChance;
    }

    /**
     * Returns the cooldown period for the magic image.
     *
     * @return the magic image cooldown
     */
    public int magicImageCooldown() {
        return magicImageCooldown;
    }

    /**
     * Returns the schedule for deleting the magic image.
     *
     * @return the magic image delete schedule
     */
    public int magicImageDeleteSchedule() {
        return magicImageDeleteSchedule;
    }
}
