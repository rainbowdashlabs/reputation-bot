package de.chojo.repbot.config.elements;

import java.util.Optional;

@SuppressWarnings("FieldMayBeFinal")
public class Badges {
    private boolean enables = true;
    private String[] badges = new String[0];

    /**
     * Retrieve the badge for the rank.
     *
     * @param rank rank to get the badge
     * @return badge if a badge is present for this rank.
     */
    public Optional<String> badge(int rank) {
        if (rank == 0) return Optional.empty();
        if (!enables) return Optional.empty();
        if (rank > badges.length) {
            return Optional.empty();
        }
        return Optional.ofNullable(badges[rank - 1]);
    }
}
