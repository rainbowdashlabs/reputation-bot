/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.components;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public interface MemberHolder extends UserHolder, GuildHolder {
    Member member();

    default long memberId() {
        return member().getIdLong();
    }

    @Override
    default User user() {
        return member().getUser();
    }

    @Override
    default Guild guild() {
        return member().getGuild();
    }

    @Override
    default long guildId(){
        return guild().getIdLong();
    }
}
