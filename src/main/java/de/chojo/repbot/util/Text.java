/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.util;

import de.chojo.jdautil.wrapper.EventContext;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.PropertyKey;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for text-related operations.
 */
public final class Text {

    /**
     * Constant representing an empty character.
     */
    private static final String EMPTY = "_";

    /**
     * Constant representing a full block character.
     */
    private static final String FULL = "█";

    /**
     * Constant representing the color orange in ANSI escape codes.
     */
    private static final String ORANGE = "\u001b[0;33m";

    /**
     * Constant representing the color white in ANSI escape codes.
     */
    private static final String WHITE = "\u001b[0;37;47m";

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Text() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * Generates a progress bar string.
     *
     * @param percent the percentage of completion
     * @param tiles the total number of tiles in the progress bar
     * @return the generated progress bar string
     */
    public static String progressBar(double percent, int tiles) {
        var progressBar = StringUtils.repeat(FULL, (int) Math.round(percent * tiles)) + WHITE;
        return ORANGE + StringUtils.rightPad(progressBar, tiles + WHITE.length(), EMPTY);
    }

    /**
     * Date formatter for the pattern "yyyy-MM-dd".
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Date formatter for the pattern "yyyy-MM".
     */
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * Date-time formatter for the pattern "yyyy-MM-dd HH:mm".
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Time formatter for the pattern "HH:mm".
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Formats a LocalDate object to a string.
     *
     * @param date the LocalDate object
     * @return the formatted date string
     */
    public static String date(LocalDate date) {
        return DATE_FORMATTER.format(date);
    }

    /**
     * Formats a LocalDateTime object to a string.
     *
     * @param date the LocalDateTime object
     * @return the formatted date string
     */
    public static String date(LocalDateTime date) {
        return DATE_FORMATTER.format(date);
    }

    /**
     * Formats a LocalDate object to a string representing the month.
     *
     * @param date the LocalDate object
     * @return the formatted month string
     */
    public static String month(LocalDate date) {
        return MONTH.format(date);
    }

    /**
     * Formats a LocalDateTime object to a date-time string.
     *
     * @param dateTime the LocalDateTime object
     * @return the formatted date-time string
     */
    public static String dateTime(LocalDateTime dateTime) {
        return DATE_TIME_FORMATTER.format(dateTime);
    }

    /**
     * Formats a LocalTime object to a time string.
     *
     * @param time the LocalTime object
     * @return the formatted time string
     */
    public static String time(LocalTime time) {
        return TIME_FORMATTER.format(time);
    }

    /**
     * Formats a LocalDateTime object to a time string.
     *
     * @param time the LocalDateTime object
     * @return the formatted time string
     */
    public static String time(LocalDateTime time) {
        return TIME_FORMATTER.format(time);
    }

    /**
     * Retrieves a localized boolean message based on the value.
     *
     * @param context the event context
     * @param value the boolean value
     * @param whenTrue the message when the value is true
     * @param whenFalse the message when the value is false
     * @return the localized boolean message
     */
    public static String getBooleanMessage(EventContext context, boolean value, String whenTrue, String whenFalse) {
        return context.localize(value ? whenTrue : whenFalse);
    }

    /**
     * Retrieves a localized setting message.
     *
     * @param locale the locale key
     * @param object the object to include in the message
     * @return the localized setting message
     */
    public static String getSetting(@PropertyKey(resourceBundle = "locale") String locale, Object object) {
        return String.format("$%s$: %s", locale, object);
    }

    /**
     * Retrieves a localized setting message based on the enabled state.
     *
     * @param locale the locale key
     * @param enabled the enabled state
     * @return the localized setting message
     */
    public static String getSetting(@PropertyKey(resourceBundle = "locale") String locale, boolean enabled) {
        return String.format("$%s$: $%s$", locale, enabled ? "words.enabled" : "words.disabled");
    }
}
