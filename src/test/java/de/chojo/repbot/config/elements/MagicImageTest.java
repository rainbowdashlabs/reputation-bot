/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MagicImageTest {
    private static final ObjectMapper MAPPER =
            new ObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    private static MagicImage read(String json) throws JsonProcessingException {
        return MAPPER.readValue(json, MagicImage.class);
    }

    @Test
    void disabledByDefault() {
        Assertions.assertFalse(new MagicImage().isEnabled());
    }

    @Test
    void enabledWithLink() throws JsonProcessingException {
        Assertions.assertTrue(
                read("{\"magicImageLink\": \"https://example.com/image.png\"}").isEnabled());
    }

    @Test
    void disabledWithoutChance() throws JsonProcessingException {
        Assertions.assertFalse(
                read("{\"magicImageLink\": \"https://example.com/image.png\", \"magicImagineChance\": 0}")
                        .isEnabled());
    }
}
