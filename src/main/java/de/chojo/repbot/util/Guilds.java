/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public final class Guilds {
    private static final Logger log = getLogger(Guilds.class);

    private Guilds() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static String prettyName(Guild guild) {
        return String.format("%s (%s)", guild.getName(), guild.getIdLong());
    }

    /**
     * Retrieves a member of a guild.
     * A user that is not a member is expected and returns {@code null} silently; any other failure is logged.
     *
     * @param guild  guild to look up the member in
     * @param userId id of the user
     * @return the member or {@code null} if it could not be retrieved
     */
    public static @Nullable Member retrieveMember(Guild guild, long userId) {
        try {
            return guild.retrieveMemberById(userId).complete();
        } catch (ErrorResponseException e) {
            if (e.getErrorResponse() != ErrorResponse.UNKNOWN_MEMBER
                    && e.getErrorResponse() != ErrorResponse.UNKNOWN_USER) {
                log.error("Could not retrieve member {} of guild {}", userId, prettyName(guild), e);
            }
        } catch (Exception e) {
            log.error("Could not retrieve member {} of guild {}", userId, prettyName(guild), e);
        }
        return null;
    }
}
