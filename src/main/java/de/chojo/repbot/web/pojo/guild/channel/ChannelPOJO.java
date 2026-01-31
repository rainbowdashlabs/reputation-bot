package de.chojo.repbot.web.pojo.guild.channel;

import net.dv8tion.jda.api.entities.channel.ChannelType;

public class ChannelPOJO {
    String name;
    long id;
    ChannelType type;

    public ChannelPOJO(String name, long id, ChannelType type) {
        this.name = name;
        this.id = id;
        this.type = type;
    }
}
