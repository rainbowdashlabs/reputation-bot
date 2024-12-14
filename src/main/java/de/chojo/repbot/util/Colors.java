/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
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
        /**
         * Light red pastel color.
         */
        public static final Color LIGHT_RED = new Color(235, 145, 145);
        /**
         * Red pastel color.
         */
        public static final Color RED = new Color(245, 93, 93);
        /**
         * Dark red pastel color.
         */
        public static final Color DARK_RED = new Color(168, 69, 69);
        /**
         * Orange pastel color.
         */
        public static final Color ORANGE = new Color(237, 156, 85);
        /**
         * Yellow pastel color.
         */
        public static final Color YELLOW = new Color(237, 221, 104);
        /**
         * Light green pastel color.
         */
        public static final Color LIGHT_GREEN = new Color(188, 242, 141);
        /**
         * Green pastel color.
         */
        public static final Color GREEN = new Color(137, 237, 123);
        /**
         * Dark green pastel color.
         */
        public static final Color DARK_GREEN = new Color(83, 158, 73);
        /**
         * Aqua pastel color.
         */
        public static final Color AQUA = new Color(183, 247, 247);
        /**
         * Light blue pastel color.
         */
        public static final Color LIGHT_BLUE = new Color(132, 204, 240);
        /**
         * Blue pastel color.
         */
        public static final Color BLUE = new Color(132, 161, 240);
        /**
         * Dark blue pastel color.
         */
        public static final Color DARK_BLUE = new Color(85, 106, 163);
        /**
         * Purple pastel color.
         */
        public static final Color PURPLE = new Color(189, 110, 204);
        /**
         * Dark pink pastel color.
         */
        public static final Color DARK_PINK = new Color(179, 57, 130);
        /**
         * Pink pastel color.
         */
        public static final Color PINK = new Color(201, 103, 177);
        /**
         * Light pink pastel color.
         */
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
        /**
         * Light red strong color.
         */
        public static final Color LIGHT_RED = new Color(255, 97, 97);
        /**
         * Red strong color.
         */
        public static final Color RED = new Color(255, 0, 0);
        /**
         * Dark red strong color.
         */
        public static final Color DARK_RED = new Color(176, 0, 0);
        /**
         * Orange strong color.
         */
        public static final Color ORANGE = new Color(255, 132, 0);
        /**
         * Yellow strong color.
         */
        public static final Color YELLOW = new Color(255, 255, 0);
        /**
         * Light green strong color.
         */
        public static final Color LIGHT_GREEN = new Color(74, 255, 95);
        /**
         * Green strong color.
         */
        public static final Color GREEN = new Color(0, 255, 30);
        /**
         * Dark green strong color.
         */
        public static final Color DARK_GREEN = new Color(0, 181, 21);
        /**
         * Aqua strong color.
         */
        public static final Color AQUA = new Color(0, 255, 255);
        /**
         * Light blue strong color.
         */
        public static final Color LIGHT_BLUE = new Color(77, 184, 255);
        /**
         * Blue strong color.
         */
        public static final Color BLUE = new Color(0, 0, 255);
        /**
         * Dark blue strong color.
         */
        public static final Color DARK_BLUE = new Color(0, 0, 191);
        /**
         * Purple strong color.
         */
        public static final Color PURPLE = new Color(187, 0, 255);
        /**
         * Dark pink strong color.
         */
        public static final Color DARK_PINK = new Color(219, 0, 102);
        /**
         * Pink strong color.
         */
        public static final Color PINK = new Color(255, 0, 132);
        /**
         * Light pink strong color.
         */
        public static final Color LIGHT_PINK = new Color(255, 117, 198);

        private Strong() {
            throw new UnsupportedOperationException("This is a utility class.");
        }
    }
}
