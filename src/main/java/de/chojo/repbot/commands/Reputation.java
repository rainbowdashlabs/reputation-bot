package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Reputation extends SimpleCommand {
    private final Guilds guilds;
    private final Configuration configuration;

    public Reputation(Guilds guilds, Configuration configuration) {
        super(CommandMeta.builder("rep", "command.reputation.description")
                .addArgument(SimpleArgument.user("user", "command.reputation.description.arg.user")));
        this.guilds = guilds;
        this.configuration = configuration;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var userOption = event.getOption("user");
        var member = userOption != null ? userOption.getAsMember() : event.getMember();
        if (member == null) {
            event.reply(context.localize("error.userNotFound")).queue();
            return;
        }
        var reputation = guilds.guild(event.getGuild())
                .reputation()
                .user(member)
                .profile()
                .publicProfile(configuration, context.localizer());
        event.replyEmbeds(reputation).queue();
    }
}
