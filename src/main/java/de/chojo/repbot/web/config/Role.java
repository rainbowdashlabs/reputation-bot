package de.chojo.repbot.web.config;

import io.javalin.security.RouteRole;

public enum Role implements RouteRole {
    ANYONE, GUILD_USER
}
