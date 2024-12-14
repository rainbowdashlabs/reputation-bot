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
 * Handler for setting a user's reputation.
 */
public class Set extends BaseReputationModifier {

    /**
     * Constructs a new Set handler.
     *
     * @param roleAssigner the role assigner service
     * @param guilds the guilds provider
     */
    public Set(RoleAssigner roleAssigner, Guilds guilds) {
        super(roleAssigner, guilds);
    }

    /**
     * Executes the command to set a user's reputation.
     *
     * @param event the slash command interaction event
     * @param context the event context
     * @param user the user whose reputation is being set
     * @param repUser the reputation user object
     * @param rep the reputation value to set
     */
    @Override
    void execute(SlashCommandInteractionEvent event, EventContext context, User user, RepUser repUser, long rep) {
        // Set the user's reputation to the specified value
        repUser.setReputation(rep);

        // Reply to the event with a localized message indicating the reputation has been set
        event.reply(context.localize("command.repadmin.reputation.set.message.set",
                        Replacement.create("VALUE", rep), Replacement.createMention(user)))
                .setEphemeral(true).queue();
    }
}
