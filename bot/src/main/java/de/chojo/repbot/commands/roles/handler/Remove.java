/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.roles.handler;

import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.dao.snapshots.ReputationRank;
import de.chojo.repbot.util.WebPromo;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.function.Consumer;

public class Remove extends BaseRoleModifier {

    public Remove(Refresh refresh, GuildRepository guildRepository) {
        super(refresh, guildRepository);
    }

    @Override
    public void modify(SlashCommandInteractionEvent event, EventContext context, Consumer<MessageEmbed> refresh) {
        var ranks = guilds().guild(event.getGuild()).settings().ranks();
        var role = event.getOption("role").getAsRole();

        if (ranks.rank(role).map(ReputationRank::remove).orElse(false)) {
            var menu = new LocalizedEmbedBuilder(context.guildLocalizer())
                    .setTitle("command.roles.remove.title.removed")
                    .appendDescription(WebPromo.promoString(context) + "\n\n")
                    .setDescription("command.roles.remove.message.removed", Replacement.createMention("ROLE", role))
                    .build();
            refresh.accept(menu);
            return;
        }
        event.reply(WebPromo.promoString(context) + "\n" + context.localize("command.roles.remove.message.noreprole"))
                .setEphemeral(true)
                .complete();
    }
}
