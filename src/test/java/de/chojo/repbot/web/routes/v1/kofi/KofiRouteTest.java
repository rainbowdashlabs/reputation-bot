/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.kofi;

import de.chojo.repbot.service.KofiService;
import de.chojo.repbot.service.kofi.KofiTransaction;
import io.javalin.http.Context;
import io.javalin.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KofiRouteTest {
    @Mock
    private KofiService kofiService;
    @Mock
    private Context ctx;
    @Mock
    private JsonMapper jsonMapper;

    private KofiRoute kofiRoute;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        kofiRoute = new KofiRoute(kofiService);
        when(ctx.jsonMapper()).thenReturn(jsonMapper);
    }

    @Test
    void handleKofiPayment() {
        String body = "data=%7B%22verification_token%22%3A%22test_token%22%7D";
        when(ctx.body()).thenReturn(body);
        KofiTransaction transaction = mock(KofiTransaction.class);
        when(jsonMapper.fromJsonString(eq("{\"verification_token\":\"test_token\"}"), eq(KofiTransaction.class)))
                .thenReturn(transaction);

        kofiRoute.handleKofiPayment(ctx);

        verify(kofiService).handle(transaction);
        verify(ctx).status(200);
    }
}
