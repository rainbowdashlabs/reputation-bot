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
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.Guilds;

public class RepAdmin extends SlashCommand {

    public RepAdmin(Guilds guilds, Configuration configuration) {
        super(Slash.of("repadmin", "command.repadmin.description")
                .adminCommand()
                .group(Group.of("reputation", "command.repadmin.reputation.description")
                        .subCommand(SubCommand.of("add", "command.repadmin.reputation.add.description")
                                .handler(new Add(guilds))
                                .argument(Argument.user("user", "command.repadmin.reputation.add.user.description").asRequired())
                                .argument(Argument.integer("amount", "command.repadmin.reputation.add.amount.description").asRequired()))
                        .subCommand(SubCommand.of("remove", "command.repadmin.reputation.remove.description")
                                .handler(new Remove(guilds))
                                .argument(Argument.user("user", "command.repadmin.reputation.remove.user.description").asRequired())
                                .argument(Argument.integer("amount", "command.repadmin.reputation.remove.amount.description").asRequired()))
                        .subCommand(SubCommand.of("set", "command.repadmin.reputation.set.description")
                                .handler(new Set(guilds))
                                .argument(Argument.user("user", "command.repadmin.reputation.set.user.description").asRequired())
                                .argument(Argument.integer("amount", "command.repadmin.reputation.set.amount.description").asRequired())))
                .subCommand(SubCommand.of("profile", "command.repadmin.profile.description")
                        .handler(new Profile(guilds, configuration))
                        .argument(Argument.user("user", "command.repadmin.profile.user.description").asRequired()))
        );
    }
}
