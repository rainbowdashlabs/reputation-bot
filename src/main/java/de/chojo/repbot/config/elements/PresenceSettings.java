package de.chojo.repbot.config.elements;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.chojo.jdautil.localization.util.Replacement;
import net.dv8tion.jda.api.entities.Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("FieldMayBeFinal")
public class PresenceSettings {
    private boolean active = false;
    private int interval = 5;
    private List<Presence> status = new ArrayList<>() {{
        add(Presence.of(Activity.ActivityType.WATCHING, "%guild_count% guilds!"));
        add(Presence.of(Activity.ActivityType.LISTENING, "%channel_count% channels!"));
        add(Presence.of(Activity.ActivityType.WATCHING, "%total_rep% Reputations!"));
        add(Presence.of(Activity.ActivityType.WATCHING, "%weekly_rep% Reputation this week!"));
        add(Presence.of(Activity.ActivityType.WATCHING, "%today_rep% Reputation today!"));
        add(Presence.of(Activity.ActivityType.WATCHING, "%weekly_avg_rep% Reputation per week!"));
        add(Presence.of(Activity.ActivityType.LISTENING, "%analyzed_messages% messages during the last hour!"));
    }};

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
            for (Replacement replacement : replacements) {
                message = replacement.invoke(message);
            }
            return message;
        }
    }
}
