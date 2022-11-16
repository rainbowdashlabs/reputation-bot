package de.chojo.repbot.commands.repadmin;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Group;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.repadmin.handler.Profile;
import de.chojo.repbot.commands.repadmin.handler.reputation.Add;
import de.chojo.repbot.commands.repadmin.handler.reputation.Remove;
import de.chojo.repbot.commands.repadmin.handler.reputation.Set;
import de.chojo.repbot.commands.repadmin.handler.resetdate.CurrentResetDate;
import de.chojo.repbot.commands.repadmin.handler.resetdate.RemoveResetDate;
import de.chojo.repbot.commands.repadmin.handler.resetdate.SetResetDate;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.service.RoleAssigner;

import java.time.LocalDate;

public class RepAdmin extends SlashCommand {

    public RepAdmin(Guilds guilds, Configuration configuration, RoleAssigner roleAssigner) {
        super(Slash.of("repadmin", "command.repadmin.description")
                .guildOnly()
                .adminCommand()
                .group(Group.of("reputation", "command.repadmin.reputation.description")
                        .subCommand(SubCommand.of("add", "command.repadmin.reputation.add.description")
                                .handler(new Add(roleAssigner, guilds))
                                .argument(Argument.user("user", "command.repadmin.reputation.add.user.description")
                                                  .asRequired())
                                .argument(Argument.integer("amount", "command.repadmin.reputation.add.amount.description")
                                                  .min(1)
                                                  .max(1_000_000)
                                                  .asRequired()))
                        .subCommand(SubCommand.of("remove", "command.repadmin.reputation.remove.description")
                                .handler(new Remove(roleAssigner, guilds))
                                .argument(Argument.user("user", "command.repadmin.reputation.remove.user.description")
                                                  .asRequired())
                                .argument(Argument.integer("amount", "command.repadmin.reputation.remove.amount.description")
                                                  .min(1)
                                                  .max(1_000_000)
                                                  .asRequired()))
                        .subCommand(SubCommand.of("set", "command.repadmin.reputation.set.description")
                                .handler(new Set(roleAssigner, guilds))
                                .argument(Argument.user("user", "command.repadmin.reputation.set.user.description")
                                                  .asRequired())
                                .argument(Argument.integer("amount", "command.repadmin.reputation.set.amount.description")
                                                  .min(0)
                                                  .max(1_000_000)
                                                  .asRequired())))
                .group(Group.of("resetdate", "command.repadmin.resetdate.description")
                        .subCommand(SubCommand.of("set", "command.repadmin.resetdate.set.description")
                                .handler(new SetResetDate(guilds))
                                .argument(Argument.integer("year", "command.repadmin.resetdate.set.year.description")
                                                  .asRequired()
                                                  .min(2016)
                                                  .max(LocalDate.now().getYear()))
                                .argument(Argument.integer("month", "command.repadmin.resetdate.set.month.description")
                                                  .asRequired()
                                                  .min(1)
                                                  .max(12))
                                .argument(Argument.integer("day", "command.repadmin.resetdate.set.day.description")
                                                  .asRequired()
                                                  .min(1)
                                                  .max(31)))
                        .subCommand(SubCommand.of("remove", "command.repadmin.resetdate.remove.description")
                                .handler(new RemoveResetDate(guilds)))
                        .subCommand(SubCommand.of("current", "command.repadmin.resetdate.current.description")
                                .handler(new CurrentResetDate(guilds))))
                .subCommand(SubCommand.of("profile", "command.repadmin.profile.description")
                        .handler(new Profile(guilds, configuration))
                        .argument(Argument.user("user", "command.repadmin.profile.user.description").asRequired()))
        );
    }
}
