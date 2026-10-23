/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})
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

    /**
     * Whether the image can be sent at all.
     *
     * @return true when a link is set and the chance is positive
     */
    public boolean isEnabled() {
        return !magicImageLink.isBlank() && magicImagineChance > 0;
    }
}
