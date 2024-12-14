/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.repadmin.handler.reputation;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.reputation.sub.RepUser;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.service.RoleAssigner;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Abstract base class for handling reputation modification commands.
 */
public abstract class BaseReputationModifier implements SlashHandler {

    private final RoleAssigner roleAssigner;
    private final Guilds guilds;

    /**
     * Constructs a BaseReputationModifier with the specified role assigner and guild provider.
     *
     * @param roleAssigner the role assigner service
     * @param guilds       the guild provider
     */
    public BaseReputationModifier(RoleAssigner roleAssigner, Guilds guilds) {
        this.roleAssigner = roleAssigner;
        this.guilds = guilds;
    }

    /**
     * Handles the slash command interaction event for modifying reputation.
     *
     * @param event   the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var user = event.getOption("user").getAsUser();
        var repUser = guilds.guild(event.getGuild()).reputation().user(user);
        var add = event.getOption("amount").getAsLong();
        execute(event, context, user, repUser, add);
        var member = event.getGuild().retrieveMember(user).complete();
        roleAssigner.updateReporting(member, event.getChannel().asGuildMessageChannel());
    }

    /**
     * Executes the reputation modification logic.
     *
     * @param event   the slash command interaction event
     * @param context the event context
     * @param user    the user whose reputation is being modified
     * @param repUser the reputation user object
     * @param rep     the amount of reputation to modify
     */
    abstract void execute(SlashCommandInteractionEvent event, EventContext context, User user, RepUser repUser, long rep);
}
