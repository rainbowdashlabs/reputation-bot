/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class ReflectionEventExtractor {
    public static final Map<Class<?>, Function<GenericEvent, User>> USER_EXTRACTION = new HashMap<>();
    public static final Map<Class<?>, Function<GenericEvent, Guild>> GUILD_EXTRACTION = new HashMap<>();

    public static User extractUser(GenericEvent event) {
        if (USER_EXTRACTION.containsKey(event.getClass())) {
            return USER_EXTRACTION.get(event.getClass()).apply(event);
        }

        try {
            Method getUser = event.getClass().getMethod("getUser");
            User user = invoke(event, getUser);
            USER_EXTRACTION.put(event.getClass(), e -> invoke(e, getUser));
            return user;
        } catch (NoSuchMethodException | RuntimeException ignored) {
        }

        try {
            Method getUser = event.getClass().getMethod("getAuthor");
            User user = invoke(event, getUser);
            USER_EXTRACTION.put(event.getClass(), e -> invoke(e, getUser));
            return user;
        } catch (NoSuchMethodException | RuntimeException ignored) {
        }
        try {
            Method getUser = event.getClass().getMethod("getMember");
            User user = ((Member) invoke(event, getUser)).getUser();
            USER_EXTRACTION.put(event.getClass(), e -> ((Member) invoke(e, getUser)).getUser());
            return user;
        } catch (NoSuchMethodException | RuntimeException ignored) {
        }
        USER_EXTRACTION.put(event.getClass(), e -> null);
        return null;
    }

    public static Guild extractGuild(GenericEvent event) {
        if (GUILD_EXTRACTION.containsKey(event.getClass())) {
            return GUILD_EXTRACTION.get(event.getClass()).apply(event);
        }

        try {
            Method isFromGuild = event.getClass().getMethod("isFromGuild");
            Method getGuild = event.getClass().getMethod("getGuild");
            Guild guild = null;
            if (invoke(event, isFromGuild)) {
                guild = invoke(event, getGuild);
            }
            GUILD_EXTRACTION.put(event.getClass(), e -> {
                if (invoke(e, isFromGuild)) return invoke(e, getGuild);
                return null;
            });
            return guild;
        } catch (NoSuchMethodException | RuntimeException ignored) {
        }

        try {
            Method getGuild = event.getClass().getMethod("getGuild");
            Guild guild = invoke(event, getGuild);
            GUILD_EXTRACTION.put(event.getClass(), e -> invoke(e, getGuild));
            return guild;
        } catch (NoSuchMethodException | RuntimeException ignored) {
        }
        GUILD_EXTRACTION.put(event.getClass(), e -> null);
        return null;
    }

    public static InsufficientPermissionException extractPermissionException(Throwable throwable) {
        while (throwable != null) {
            if (throwable instanceof InsufficientPermissionException exception) {
                return exception;
            }
            throwable = throwable.getCause();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> T invoke(Object object, Method method, Object... args) {
        try {
            return (T) method.invoke(object, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
