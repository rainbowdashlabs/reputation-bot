package de.chojo.repbot.commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ThankwordsTest {

    @Test
    void loadContainer() {
        Assertions.assertDoesNotThrow(Thankwords::loadContainer);
    }
}
