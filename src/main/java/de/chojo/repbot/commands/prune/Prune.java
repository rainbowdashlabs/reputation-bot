package de.chojo.repbot.commands.prune;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.prune.handler.Guild;
import de.chojo.repbot.commands.prune.handler.User;
import de.chojo.repbot.service.GdprService;
import net.dv8tion.jda.api.Permission;

public class Prune extends SlashCommand {

    public Prune(GdprService service) {
        super(Slash.of("prune", "command.prune.description")
                .withPermission(Permission.MESSAGE_MANAGE)
                .subCommand(SubCommand.of("user", "command.prune.user.description")
                        .handler(new User(service))
                        .argument(Argument.user("user", "command.prune.user.user.description"))
                        .argument(Argument.text("userid", "command.prune.user.userid.description")))
                .subCommand(SubCommand.of("guild", "command.prune.guild.description")
                        .handler(new Guild(service)))
        );
    }
}
