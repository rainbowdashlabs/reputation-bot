/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.repadmin.handler.reputation;

import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.reputation.sub.RepUser;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.service.RoleAssigner;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Remove extends BaseReputationModifier {

    public Remove(RoleAssigner roleAssigner, GuildRepository guildRepository) {
        super(roleAssigner, guildRepository);
    }

    @Override
    void execute(SlashCommandInteractionEvent event, EventContext context, User user, RepUser repUser, long rep) {
        repUser.removeReputation(rep);
        event.reply(context.localize("command.repadmin.reputation.remove.message.removed",
                        Replacement.create("VALUE", rep), Replacement.createMention(user)))
                .setEphemeral(true).complete();
    }
}
