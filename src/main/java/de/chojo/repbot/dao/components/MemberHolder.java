package de.chojo.repbot.dao.components;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public interface MemberHolder extends UserHolder {
    Member member();

    default long memberId(){
        return member().getIdLong();
    }

    @Override
    default User user() {
        return member().getUser();
    }
}
