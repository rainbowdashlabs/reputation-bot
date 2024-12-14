/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.repadmin.handler.reputation;

import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.reputation.sub.RepUser;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.service.RoleAssigner;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Handles the removal of reputation from a user.
 */
public class Remove extends BaseReputationModifier {

    /**
     * Constructs a Remove handler with the specified role assigner and guilds provider.
     *
     * @param roleAssigner the role assigner
     * @param guilds the guilds provider
     */
    public Remove(RoleAssigner roleAssigner, Guilds guilds) {
        super(roleAssigner, guilds);
    }

    /**
     * Executes the removal of reputation from a user.
     *
     * @param event the slash command interaction event
     * @param context the event context
     * @param user the user whose reputation is being removed
     * @param repUser the reputation user object
     * @param rep the amount of reputation to remove
     */
    @Override
    void execute(SlashCommandInteractionEvent event, EventContext context, User user, RepUser repUser, long rep) {
        repUser.removeReputation(rep);
        event.reply(context.localize("command.repadmin.reputation.remove.message.removed",
                        Replacement.create("VALUE", rep), Replacement.createMention(user)))
                .setEphemeral(true).queue();
    }
}
