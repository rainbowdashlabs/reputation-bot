/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.util;

import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.core.Bot;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class WebPromo {
    public static String promoString(EventContext ctx) {
        return "**%s**\n".formatted(ctx.localize("temp.webpromo", Replacement.create("web", Bot.WEB_COMMAND_MENTION)));
    }

    public static MessageEmbed promoEmbed(EventContext ctx) {
        return new LocalizedEmbedBuilder(ctx.guildLocalizer())
                .setDescription(promoString(ctx))
                .setColor(Color.RED)
                .build();
    }
}
