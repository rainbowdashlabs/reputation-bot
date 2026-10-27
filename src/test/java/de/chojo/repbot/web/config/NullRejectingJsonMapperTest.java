/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.config;

import de.chojo.repbot.core.Web;
import de.chojo.repbot.web.error.ApiException;
import io.javalin.http.HttpStatus;
import io.javalin.json.JsonMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NullRejectingJsonMapperTest {
    private static final JsonMapper MAPPER = new NullRejectingJsonMapper(Web.jacksonMapper());

    @Test
    void readsValue() {
        Integer value = MAPPER.fromJsonString("30", Integer.class);
        Assertions.assertEquals(30, value);
    }

    @Test
    void rejectsNullBody() {
        var exception = Assertions.assertThrows(ApiException.class, () -> MAPPER.fromJsonString("null", Integer.class));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, exception.status());
    }

    @Test
    void rejectsEmptyBody() {
        Assertions.assertThrows(ApiException.class, () -> MAPPER.fromJsonString("", Integer.class));
    }
}
