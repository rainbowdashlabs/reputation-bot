package de.chojo.repbot.web.pojo.guild;

import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

public class ReactionPOJO {
    String name;
    long id;
    String url;

    public ReactionPOJO(String name, long id, String url) {
        this.name = name;
        this.id = id;
        this.url = url;
    }

    public static ReactionPOJO generate(RichCustomEmoji emoji) {
        return new ReactionPOJO(emoji.getName(), emoji.getIdLong(), emoji.getImageUrl());
    }
}
