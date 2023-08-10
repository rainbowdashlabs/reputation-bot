/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.reputation.sub;

import de.chojo.repbot.service.reputation.SubmitResult;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AnalyzerTest {

    @Test
    void deserialize() {
        @Language("json")
        var str = """
                {
                  "type": "SUBMITTING",
                  "replacements": [
                    {
                      "key": "%type%",
                      "value": "$thankType.direct.name$",
                      "caseSensitive": false
                    },
                    {
                      "key": "%USER%",
                      "value": "<@816604004497752145>",
                      "caseSensitive": false
                    }
                  ]
                }
                """;
        Assertions.assertDoesNotThrow(() -> Analyzer.MAPPER.readValue(str, SubmitResult.class));
    }

}
