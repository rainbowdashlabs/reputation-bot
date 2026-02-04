/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub;

public class ProfilePOJO {
    protected String nickname;
    protected String profilePictureUrl;
    protected String reputationName;

    public ProfilePOJO(String nickname, String profilePictureUrl, String reputationName) {
        this.nickname = nickname;
        this.profilePictureUrl = profilePictureUrl;
        this.reputationName = reputationName;
    }

    public String nickname() {
        return nickname;
    }

    public String profilePictureUrl() {
        return profilePictureUrl;
    }

    public String reputationName() {
        return reputationName;
    }
}
