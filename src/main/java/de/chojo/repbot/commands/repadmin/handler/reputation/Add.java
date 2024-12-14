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
 * Handler for the "add reputation" slash command, which adds reputation to a user.
 */
public class Add extends BaseReputationModifier {

    /**
     * Constructs an Add handler with the specified role assigner and guilds provider.
     *
     * @param roleAssigner the role assigner
     * @param guilds the guilds provider
     */
    public Add(RoleAssigner roleAssigner, Guilds guilds) {
        super(roleAssigner, guilds);
    }

    /**
     * Executes the slash command interaction event to add reputation to a user.
     *
     * @param event the slash command interaction event
     * @param context the event context
     * @param user the user to add reputation to
     * @param repUser the reputation user object
     * @param rep the amount of reputation to add
     */
    @Override
    void execute(SlashCommandInteractionEvent event, EventContext context, User user, RepUser repUser, long rep) {
        repUser.addReputation(rep);
        event.reply(context.localize("command.repadmin.reputation.add.message.added",
                        Replacement.create("VALUE", rep), Replacement.createMention(user)))
                .setEphemeral(true).queue();
    }
}
