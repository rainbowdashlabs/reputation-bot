/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.roles;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Group;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashProvider;
import de.chojo.repbot.commands.roles.handler.Add;
import de.chojo.repbot.commands.roles.handler.List;
import de.chojo.repbot.commands.roles.handler.Refresh;
import de.chojo.repbot.commands.roles.handler.Remove;
import de.chojo.repbot.commands.roles.handler.StackRoles;
import de.chojo.repbot.commands.roles.handler.donor.AddDonor;
import de.chojo.repbot.commands.roles.handler.donor.RemoveDonor;
import de.chojo.repbot.commands.roles.handler.receiver.AddReceiver;
import de.chojo.repbot.commands.roles.handler.receiver.RemoveReceiver;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.service.RoleAssigner;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;

public class Roles implements SlashProvider<Slash> {
    private final Refresh refresh;
    private final GuildRepository guildRepository;

    public Roles(GuildRepository guildRepository, RoleAssigner roleAssigner) {
        this.guildRepository = guildRepository;
        refresh = new Refresh(roleAssigner);
    }

    @Override
    public Slash slash() {
        return Slash.of("roles", "command.roles.description")
                    .guildOnly()
                    .withPermission(Permission.MANAGE_ROLES)
                    .subCommand(SubCommand.of("add", "command.roles.add.description")
                                          .handler(new Add(refresh, guildRepository))
                                          .argument(Argument.role("role", "command.roles.add.options.role.description").asRequired())
                                          .argument(Argument.integer("reputation", "command.roles.add.options.reputation.description")
                                                            .min(0)
                                                            .max(1_000_000)
                                                            .asRequired()))
                    .subCommand(SubCommand.of("remove", "command.roles.remove.description")
                                          .handler(new Remove(refresh, guildRepository))
                                          .argument(Argument.role("role", "command.roles.remove.options.role.description").asRequired()))
                    .group(Group.of("receiver", "command.roles.receiver.description")
                                .subCommand(SubCommand.of("add", "command.roles.receiver.add.description")
                                                      .handler(new AddReceiver(guildRepository))
                                                      .argument(Argument.role("role", "command.roles.receiver.add.options.role.description")
                                                                        .asRequired()))
                                .subCommand(SubCommand.of("remove", "command.roles.receiver.remove.description")
                                                      .handler(new RemoveReceiver(guildRepository))
                                                      .argument(Argument.role("role", "command.roles.receiver.remove.options.role.description")
                                                                        .asRequired())))
                    .group(Group.of("donor", "command.roles.donor.description")
                                .subCommand(SubCommand.of("add", "command.roles.donor.add.description")
                                                      .handler(new AddDonor(guildRepository))
                                                      .argument(Argument.role("role", "command.roles.donor.add.options.role.description")
                                                                        .asRequired()))
                                .subCommand(SubCommand.of("remove", "command.roles.donor.remove.description")
                                                      .handler(new RemoveDonor(guildRepository))
                                                      .argument(Argument.role("role", "command.roles.donor.remove.options.role.description")
                                                                        .asRequired())))
                    .subCommand(SubCommand.of("refresh", "command.roles.refresh.description")
                                          .handler(refresh))
                    .subCommand(SubCommand.of("list", "command.roles.list.description")
                                          .handler(new List(guildRepository)))
                    .subCommand(SubCommand.of("stackroles", "command.roles.stackroles.description")
                                          .handler(new StackRoles(refresh, guildRepository))
                                          .argument(Argument.bool("stack", "command.roles.stackroles.options.stack.description"))
                    ).build();
    }

    public boolean refreshActive(Guild guild) {
        return refresh.refreshActive(guild);
    }
}
