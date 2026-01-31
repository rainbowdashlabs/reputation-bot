package de.chojo.repbot.web.pojo.guild;

import net.dv8tion.jda.api.entities.Role;

import java.awt.*;
import java.util.Objects;

public class RolePOJO {
    private final String name;
    private final long id;
    private final String color;

    public RolePOJO(String name, long id, Color color) {
        this.name = name;
        this.id = id;
        color = Objects.requireNonNullElse(color, Color.WHITE);
        this.color = "#%02x%02x%02x".formatted(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static RolePOJO generate(Role role) {
        return new RolePOJO(role.getName(), role.getIdLong(), role.getColor());
    }
}
