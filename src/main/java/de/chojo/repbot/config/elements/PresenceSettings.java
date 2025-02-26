/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.config.elements;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.chojo.jdautil.localization.util.Replacement;
import net.dv8tion.jda.api.entities.Activity;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents the settings for the bot's presence, including activity type and status messages.
 */
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})
public class PresenceSettings {
    private boolean active;
    private int interval = 5;
    private List<Presence> status = List.of(
            Presence.of(Activity.ActivityType.WATCHING, "%guild_count% guilds!"),
            Presence.of(Activity.ActivityType.LISTENING, "%channel_count% channels!"),
            Presence.of(Activity.ActivityType.WATCHING, "%total_rep% Reputations!"),
            Presence.of(Activity.ActivityType.WATCHING, "%weekly_rep% Reputation this week!"),
            Presence.of(Activity.ActivityType.WATCHING, "%today_rep% Reputation today!"),
            Presence.of(Activity.ActivityType.WATCHING, "%weekly_avg_rep% Reputation per week!"),
            Presence.of(Activity.ActivityType.LISTENING, "%analyzed_messages% messages during the last hour!")
    );

    /**
     * Creates a new presence settings with default values.
     */
    public PresenceSettings(){
    }

    /**
     * Checks if the presence settings are active.
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Gets the list of presence statuses.
     *
     * @return the list of presence statuses
     */
    public List<Presence> status() {
        return status;
    }

    /**
     * Gets a random presence status from the list.
     *
     * @return a random presence status
     */
    public Presence randomStatus() {
        if (status.isEmpty()) return Presence.of(Activity.ActivityType.WATCHING, "something");
        return status.get(ThreadLocalRandom.current().nextInt(status.size()));
    }

    /**
     * Gets the interval for updating the presence status.
     *
     * @return the interval in minutes
     */
    public int interval() {
        return interval;
    }

    /**
     * Represents a single presence status with an activity type and text.
     */
    @SuppressWarnings("CanBeFinal")
    public static class Presence {
        private Activity.ActivityType type;
        private String text;

        /**
         * Constructs a Presence instance with the specified type and text.
         *
         * @param type the activity type
         * @param text the text to display
         */
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public Presence(@JsonProperty("type") Activity.ActivityType type, @JsonProperty("text") String text) {
            this.type = type;
            this.text = text;
        }

        /**
         * Creates a new Presence instance with the specified type and text.
         *
         * @param type the activity type
         * @param text the text to display
         * @return a new Presence instance
         */
        public static Presence of(Activity.ActivityType type, String text) {
            return new Presence(type, text);
        }

        /**
         * Gets the activity type of the presence.
         *
         * @return the activity type
         */
        public Activity.ActivityType type() {
            return type;
        }

        /**
         * Gets the text of the presence with replacements applied.
         *
         * @param replacements the list of replacements to apply
         * @return the text with replacements applied
         */
        public String text(List<Replacement> replacements) {
            var message = text;
            for (var replacement : replacements) {
                message = replacement.invoke(message);
            }
            return message;
        }
    }
}
