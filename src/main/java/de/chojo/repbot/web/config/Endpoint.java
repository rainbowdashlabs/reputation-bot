package de.chojo.repbot.web.config;

public final class Endpoint {
    public static String derivePathFromClass(Class<?> clazz){
        return clazz.getName().replace("de.chojo.repbot.web.routes.", "").replace(".", "/");
    }
}
