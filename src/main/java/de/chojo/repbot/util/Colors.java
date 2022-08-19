package de.chojo.repbot.util;

import java.awt.Color;

/**
 * Contains static color objects for {@link Pastel} colors and {@link Strong} colors.
 */
public final class Colors {
    private Colors() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * Contains pastel color objects.
     */
    @SuppressWarnings("unused")
    public static final class Pastel {
        public static final Color LIGHT_RED = new Color(235, 145, 145);
        public static final Color RED = new Color(245, 93, 93);
        public static final Color DARK_RED = new Color(168, 69, 69);
        public static final Color ORANGE = new Color(237, 156, 85);
        public static final Color YELLOW = new Color(237, 221, 104);
        public static final Color LIGHT_GREEN = new Color(188, 242, 141);
        public static final Color GREEN = new Color(137, 237, 123);
        public static final Color DARK_GREEN = new Color(83, 158, 73);
        public static final Color AQUA = new Color(183, 247, 247);
        public static final Color LIGHT_BLUE = new Color(132, 204, 240);
        public static final Color BLUE = new Color(132, 161, 240);
        public static final Color DARK_BLUE = new Color(85, 106, 163);
        public static final Color PURPLE = new Color(189, 110, 204);
        public static final Color DARK_PINK = new Color(179, 57, 130);
        public static final Color PINK = new Color(201, 103, 177);
        public static final Color LIGHT_PINK = new Color(209, 138, 192);

        private Pastel() {
            throw new UnsupportedOperationException("This is a utility class.");
        }
    }

    /**
     * Contains colors with strong colors.
     */
    @SuppressWarnings("unused")
    public static final class Strong {
        public static final Color LIGHT_RED = new Color(255, 97, 97);
        public static final Color RED = new Color(255, 0, 0);
        public static final Color DARK_RED = new Color(176, 0, 0);
        public static final Color ORANGE = new Color(255, 132, 0);
        public static final Color YELLOW = new Color(255, 255, 0);
        public static final Color LIGHT_GREEN = new Color(74, 255, 95);
        public static final Color GREEN = new Color(0, 255, 30);
        public static final Color DARK_GREEN = new Color(0, 181, 21);
        public static final Color AQUA = new Color(0, 255, 255);
        public static final Color LIGHT_BLUE = new Color(77, 184, 255);
        public static final Color BLUE = new Color(0, 0, 255);
        public static final Color DARK_BLUE = new Color(0, 0, 191);
        public static final Color PURPLE = new Color(187, 0, 255);
        public static final Color DARK_PINK = new Color(219, 0, 102);
        public static final Color PINK = new Color(255, 0, 132);
        public static final Color LIGHT_PINK = new Color(255, 117, 198);

        private Strong() {
            throw new UnsupportedOperationException("This is a utility class.");
        }
    }
}
