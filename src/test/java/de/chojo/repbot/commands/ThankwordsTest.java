package de.chojo.repbot.commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ThankwordsTest {

    @Test
    void loadContainer() {
        Assertions.assertDoesNotThrow(Thankwords::loadContainer);
    }
}
