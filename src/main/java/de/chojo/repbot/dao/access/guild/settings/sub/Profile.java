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
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Function;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

@JsonSerializeAs(ProfilePOJO.class)
public class Profile extends ProfilePOJO implements GuildHolder {
    private final Settings settings;

    public Profile(Settings settings) {
        this(settings, settings.guild().getSelfMember().getNickname(), settings.guild().getJDA().getSelfUser().getEffectiveAvatarUrl());
    }

    public Profile(Settings settings, String nickname, String profilePictureUrl) {
        super(nickname, profilePictureUrl);
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
            settings.guild().getSelfMember().modifyNickname(nickname).complete();
            this.nickname = nickname;
            return true;
        } catch (InsufficientPermissionException e) {
            return false;
        }
    }

    /**
     * Sets the profile picture.
     *
     * @param bytes the image bytes
     * @return true if successfully uploaded
     */
    public boolean profilePicture(byte[] bytes) {
        try {
            settings.guild().getSelfMember().getManager().setAvatar(Icon.from(bytes)).complete();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void apply(ProfilePOJO pojo) {
        String newNickname = pojo.nickname();
        String currentNickname = settings.guild().getSelfMember().getNickname();
        if (!Objects.equals(newNickname, currentNickname)) nickname(newNickname);
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
