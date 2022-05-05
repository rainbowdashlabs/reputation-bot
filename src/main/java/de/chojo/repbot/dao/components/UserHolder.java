package de.chojo.repbot.dao.components;

import net.dv8tion.jda.api.entities.User;

public interface UserHolder {
    User user();

    default long userId(){
        return user().getIdLong();
    }
}
