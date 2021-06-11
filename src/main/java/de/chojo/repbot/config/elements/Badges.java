package de.chojo.repbot.config.elements;

import java.util.Optional;

public class Badges {
    private boolean enables = true;
    private String[] badges = new String[0];

    public Badges() {
    }

    public Badges(boolean enables, String[] badges) {
        this.enables = enables;
        this.badges = badges;
    }

    public Optional<String> badge(int rank) {
        if (rank == 0) return Optional.empty();
        if (!enables) return Optional.empty();
        if (rank >= badges.length) {
            return Optional.empty();
        }
        return Optional.ofNullable(badges[rank - 1]);
    }
}
