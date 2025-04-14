/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.components;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

/**
 * Interface representing a holder for a member, providing access to the member, user, and guild information.
 */
public interface MemberHolder extends UserHolder, GuildHolder {
    /**
     * Retrieves the member associated with this holder.
     *
     * @return the member
     */
    Member member();

    /**
     * Retrieves the ID of the member associated with this holder.
     *
     * @return the member ID
     */
    default long memberId() {
        return member().getIdLong();
    }

    /**
     * Retrieves the user associated with this holder.
     *
     * @return the user
     */
    @Override
    default User user() {
        return member().getUser();
    }

    /**
     * Retrieves the guild associated with this holder.
     *
     * @return the guild
     */
    @Override
    default Guild guild() {
        return member().getGuild();
    }

    /**
     * Retrieves the ID of the guild associated with this holder.
     *
     * @return the guild ID
     */
    @Override
    default long guildId(){
        return guild().getIdLong();
    }
}
