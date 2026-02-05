/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.chojo.sadu.mapper.reader.ValueReader;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.adapter.Adapter;
import de.chojo.sadu.queries.converter.ValueConverter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public final class CustomValueConverter {
    public static final ObjectMapper MAPPER = new ObjectMapper()
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    private static final Adapter<Object> JSON_ADAPTER =
            Adapter.create(Object.class, CustomValueConverter::mapObjectToString, Types.VARCHAR);
    private static final ValueReader<Object, String> JSON_READER =
            ValueReader.create(CustomValueConverter::mapStringToObject, Row::getString, Row::getString);
    public static final ValueConverter<Object, String> OBJECT_JSON = ValueConverter.create(JSON_ADAPTER, JSON_READER);

    private static void mapObjectToString(PreparedStatement stmt, int index, Object value) throws SQLException {
        try {
            stmt.setString(index, MAPPER.writeValueAsString(value));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not parse object", e);
        }
    }

    private static Object mapStringToObject(String value) {
        try {
            return MAPPER.readValue(value, Object.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not parse string", e);
        }
    }
}
