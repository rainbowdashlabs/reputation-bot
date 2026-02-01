package de.chojo.repbot.web.pojo.settings.sub;

public class ProfilePOJO {
    protected String nickname;
    protected String profilePictureUrl;

    public ProfilePOJO(String nickname, String profilePictureUrl) {
        this.nickname = nickname;
        this.profilePictureUrl = profilePictureUrl;
    }

    public String nickname() {
        return nickname;
    }

    public String profilePictureUrl() {
        return profilePictureUrl;
    }
}
