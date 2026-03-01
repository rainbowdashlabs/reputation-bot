/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.chojo.repbot.config.elements.sku.SKUEntry;
import de.chojo.repbot.config.jackson.deserializer.SKUEntryDeserializer;
import de.chojo.repbot.config.jackson.serializer.SKUEntrySerializer;

public class BotModule extends SimpleModule {
    public BotModule() {
        super("RepBotModule", Version.unknownVersion());
        addSerializer(SKUEntry.class, new SKUEntrySerializer());
        addDeserializer(SKUEntry.class, new SKUEntryDeserializer());
    }
}
