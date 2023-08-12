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
            Presence.of("Watching %guild_count% guilds!"),
            Presence.of("Reading %channel_count% channels!"),
            Presence.of("Couting %total_rep% Reputations!"),
            Presence.of("Counted %weekly_rep% Reputation this week!"),
            Presence.of("Counted %today_rep% Reputation today!"),
            Presence.of("Counted %weekly_avg_rep% Reputation per week!"),
            Presence.of("Read %analyzed_messages% messages during the last hour!")
    );

    public boolean isActive() {
        return active;
    }

    public List<Presence> status() {
        return status;
    }

    public Presence randomStatus() {
        if (status.isEmpty()) return Presence.of("something");
        return status.get(ThreadLocalRandom.current().nextInt(status.size()));
    }

    public int interval() {
        return interval;
    }

    @SuppressWarnings("CanBeFinal")
    public static class Presence {
        private String text;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public Presence(@JsonProperty("text") String text) {
            this.text = text;
        }

        public static Presence of(String text) {
            return new Presence(text);
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
