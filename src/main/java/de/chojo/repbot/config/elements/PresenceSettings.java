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

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})
public class PresenceSettings {
    private boolean active;
    private int interval = 5;
    private List<Presence> status = List.of(
            Presence.of(Activity.ActivityType.CUSTOM_STATUS, "Watching over %guild_count% guilds!"),
            Presence.of(Activity.ActivityType.CUSTOM_STATUS, "Listening in %channel_count% channels!"),
            Presence.of(Activity.ActivityType.CUSTOM_STATUS, "%total_rep% Reputations given!"),
            Presence.of(Activity.ActivityType.CUSTOM_STATUS, "%weekly_rep% Reputation given this week!"),
            Presence.of(Activity.ActivityType.CUSTOM_STATUS, "%today_rep% Reputation given today!"),
            Presence.of(Activity.ActivityType.CUSTOM_STATUS, "%weekly_avg_rep% Reputation given per week!"),
            Presence.of(Activity.ActivityType.CUSTOM_STATUS, "Analyzed %analyzed_messages% messages during the last hour!")
    );

    public boolean isActive() {
        return active;
    }

    public List<Presence> status() {
        return status;
    }

    public Presence randomStatus() {
        if (status.isEmpty()) return Presence.of(Activity.ActivityType.WATCHING, "something");
        return status.get(ThreadLocalRandom.current().nextInt(status.size()));
    }

    public int interval() {
        return interval;
    }

    @SuppressWarnings("CanBeFinal")
    public static class Presence {
        private Activity.ActivityType type;
        private String text;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public Presence(@JsonProperty("type") Activity.ActivityType type, @JsonProperty("text") String text) {
            this.type = type;
            this.text = text;
        }

        public static Presence of(Activity.ActivityType type, String text) {
            return new Presence(type, text);
        }

        public Activity.ActivityType type() {
            return type;
        }

        public String text(List<Replacement> replacements) {
            var message = text;
            for (var replacement : replacements) {
                message = replacement.invoke(message);
            }
            return message;
        }
    }
}
