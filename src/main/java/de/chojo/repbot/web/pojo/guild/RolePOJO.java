/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.guild;

import de.chojo.repbot.util.Colors;
import net.dv8tion.jda.api.entities.Role;

import java.awt.Color;
import java.util.Objects;

public class RolePOJO {
    private final String name;
    private final String id;
    private final int position;
    private final String color;

    public RolePOJO(String name, String id, Color color, int position) {
        this.name = name;
        this.id = id;
        this.position = position;
        this.color = Colors.toHex(Objects.requireNonNullElse(color, Color.WHITE));
    }

    public static RolePOJO generate(Role role) {
        return new RolePOJO(role.getName(), role.getId(), role.getColor(), role.getPosition());
    }
}
