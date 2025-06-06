/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.profile.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.util.Premium;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.service.RoleAssigner;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Optional;

public class Profile implements SlashHandler {
    private final GuildRepository guildRepository;
    private final Configuration configuration;
    private final RoleAssigner roleAssigner;

    public Profile(GuildRepository guildRepository, Configuration configuration, RoleAssigner roleAssigner) {
        this.guildRepository = guildRepository;
        this.configuration = configuration;
        this.roleAssigner = roleAssigner;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var userOption = event.getOption("user");
        var detailed = Optional.ofNullable(event.getOption("detailed")).map(OptionMapping::getAsBoolean).orElse(false);
        var member = userOption != null ? userOption.getAsMember() : event.getMember();
        if (member == null) {
            event.reply(context.localize("error.userNotFound")).queue();
            return;
        }

        if (detailed) {
            if (Premium.isNotEntitled(event, configuration.skus().features().detailedProfile().detailedProfile())) {
                Premium.replyPremium(event, context, configuration.skus().features().detailedProfile().detailedProfile());
                return;
            }
        }

        event.deferReply().queue();

        var reputation = guildRepository.guild(event.getGuild())
                                        .reputation()
                                        .user(member)
                                        .profile()
                                        .publicProfile(configuration, context.guildLocalizer(), detailed);
        event.getHook().editOriginalEmbeds(reputation).queue();
        roleAssigner.updateReporting(member, event.getGuildChannel());
    }
}
