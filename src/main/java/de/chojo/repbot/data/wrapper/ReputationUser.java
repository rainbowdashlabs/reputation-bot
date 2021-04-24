package de.chojo.repbot.data.wrapper;

import lombok.Getter;

@Getter
public class ReputationUser {
    private Long userId;
    private long reputation;

    public ReputationUser(Long userId, long reputation) {
        this.userId = userId;
        this.reputation = reputation;
    }


}
