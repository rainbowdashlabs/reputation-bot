package de.chojo.repbot.data.wrapper;

import lombok.Getter;
import net.dv8tion.jda.api.entities.User;

@Getter
public class ReputationUser {
    private final long rank;
    private final Long userId;
    private final long reputation;

    public ReputationUser(long rank, Long userId, long reputation) {
        this.rank = rank;
        this.userId = userId;
        this.reputation = reputation;
    }

    public static ReputationUser empty(User user) {
        return new ReputationUser(0, user.getIdLong(), 0);
    }
}
