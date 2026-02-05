/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub;

import com.fasterxml.jackson.annotation.JsonSerializeAs;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.web.pojo.settings.sub.ProfilePOJO;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.util.Objects;

@JsonSerializeAs(ProfilePOJO.class)
public class Profile extends ProfilePOJO implements GuildHolder {
    private final Settings settings;

    public Profile(Settings settings) {
        this(
                settings,
                settings.guild().getSelfMember().getNickname(),
                settings.guild().getSelfMember().getEffectiveAvatarUrl(),
                settings.repGuild()
                        .localeOverrides()
                        .getOverride("words.reputation")
                        .orElse(null));
    }

    public Profile(Settings settings, String nickname, String profilePictureUrl, String reputationName) {
        super(nickname, profilePictureUrl, reputationName);
        this.settings = settings;
    }

    /**
     * Sets the bot's nickname in this guild.
     *
     * @param nickname the new nickname, or null to reset to default
     * @return true if the nickname was successfully changed
     */
    public boolean nickname(String nickname) {
        try {
            settings.guild().getSelfMember().getManager().setNickname(nickname).complete();
            this.nickname = nickname;
            return true;
        } catch (InsufficientPermissionException e) {
            return false;
        }
    }

    /**
     * Sets the profile picture.
     *
     * @param bytes the image bytes, or null to reset to default
     * @return true if successfully uploaded
     */
    public boolean profilePicture(byte[] bytes) {
        try {
            if (bytes == null) {
                settings.guild().getSelfMember().getManager().setAvatar(null).complete();
            } else {
                settings.guild()
                        .getSelfMember()
                        .getManager()
                        .setAvatar(Icon.from(bytes))
                        .complete();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sets the reputation name for this guild.
     *
     * @param name the new reputation name, or null to reset to default
     */
    public void reputationName(String name) {
        if (name == null || name.isEmpty()) {
            settings.repGuild().localeOverrides().removeOverride("words.reputation");
            this.reputationName = null;
        } else {
            settings.repGuild().localeOverrides().setOverride("words.reputation", name);
            this.reputationName = name;
        }
    }

    public void apply(ProfilePOJO pojo) {
        String newNickname = pojo.nickname();
        String currentNickname = settings.guild().getSelfMember().getNickname();
        if (!Objects.equals(newNickname, currentNickname)) nickname(newNickname);

        String newReputationName = pojo.reputationName();
        String currentReputationName = settings.repGuild()
                .localeOverrides()
                .getOverride("words.reputation")
                .orElse(null);
        if (!Objects.equals(newReputationName, currentReputationName)) reputationName(newReputationName);
    }

    @Override
    public Guild guild() {
        return settings.guild();
    }

    @Override
    public long guildId() {
        return settings.guildId();
    }
}
