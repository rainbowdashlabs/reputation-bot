/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProfilePOJO {
    protected String nickname;
    protected String profilePictureUrl;
    protected String reputationName;

    /**
     * All values are required. A partial body is rejected instead of silently resetting the omitted settings, because
     * the settings are applied by difference.
     */
    @JsonCreator
    public ProfilePOJO(
            @JsonProperty(value = "nickname", required = true) String nickname,
            @JsonProperty(value = "profilePictureUrl", required = true) String profilePictureUrl,
            @JsonProperty(value = "reputationName", required = true) String reputationName) {
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
