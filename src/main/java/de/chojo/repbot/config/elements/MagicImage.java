package de.chojo.repbot.config.elements;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MagicImage {
    private String magicImageLink = "";
    private int magicImagineChance = 10;
    private int magicImageCooldown = 30;
    private int magicImageDeleteSchedule = 60;
}
