package de.chojo.repbot.web.pojo.guild;

import de.chojo.repbot.util.Colors;
import net.dv8tion.jda.api.entities.Member;

import java.awt.*;
import java.util.Objects;

public class MemberPOJO {
    String color;
    String displayName;
    String id;
    String profilePictureUrl;

    public MemberPOJO(String displayName, String id, String color, String profilePictureUrl) {
        this.displayName = displayName;
        this.id = id;
        this.color = color;
        this.profilePictureUrl = profilePictureUrl;
    }

    public static MemberPOJO generate(Member member) {
        var color = Colors.toHex(Objects.requireNonNullElse(member.getColors().getPrimary(), Color.WHITE));
        return new MemberPOJO(member.getEffectiveName(), member.getId(), color, member.getUser().getEffectiveAvatarUrl());
    }

    public static MemberPOJO generate(String id) {
        return new MemberPOJO("unknown", id, "#ffffff", "https://cdn.discordapp.com/embed/avatars/%s.png".formatted(id.hashCode() % 5));
    }

    public String id() {
        return id;
    }
}
