/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.jackson.deserializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import de.chojo.jdautil.interactions.premium.SKU;
import de.chojo.repbot.config.elements.sku.SKUEntry;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SKUEntryDeserializer extends JsonDeserializer<SKUEntry> {
    @Override
    public SKUEntry deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        long[] longs = p.readValueAs(long[].class);
        return new SKUEntry(Arrays.stream(longs).mapToObj(SKU::new).collect(Collectors.toList()));
    }
}
