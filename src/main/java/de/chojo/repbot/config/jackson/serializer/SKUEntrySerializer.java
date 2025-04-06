/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.chojo.jdautil.interactions.premium.SKU;
import de.chojo.repbot.config.elements.sku.SKUEntry;

import java.io.IOException;

public class SKUEntrySerializer extends StdSerializer<SKUEntry> {

    public SKUEntrySerializer() {
        super(SKUEntry.class);
    }

    @Override
    public void serialize(SKUEntry value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeObject(value.sku().stream().mapToLong(SKU::getSkuIdLong).toArray());
    }
}
