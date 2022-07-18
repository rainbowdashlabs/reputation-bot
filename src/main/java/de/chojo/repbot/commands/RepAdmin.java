package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class RepAdmin extends SimpleCommand {
    private final Guilds guilds;
    private final Configuration configuration;

    public RepAdmin(Guilds guilds, Configuration configuration) {
        super(CommandMeta.builder("repadmin", "command.repadmin.description")
                .addSubCommand("reputation", "command.repadmin.sub.reputation.descr", argsBuilder()
                        .add(SimpleArgument.user("user", "command.repadmin.sub.reputation.arg.user").asRequired())
                        .add(SimpleArgument.integer("add", "command.repadmin.sub.reputation.arg.add"))
                        .add(SimpleArgument.integer("remove", "command.repadmin.sub.reputation.arg.remove"))
                        .add(SimpleArgument.integer("set", "command.repadmin.sub.reputation.arg.set")))
                .addSubCommand("profile", "command.repadmin.sub.profile.descr", argsBuilder()
                        .add(SimpleArgument.user("user", "command.repadmin.sub.profile.arg.user").asRequired()))
                .adminCommand());
        this.guilds = guilds;
        this.configuration = configuration;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var subcmd = event.getSubcommandName();
        if ("reputation".equalsIgnoreCase(subcmd)) {
            reputation(event, context);
        }
        if ("profile".equalsIgnoreCase(subcmd)) {
            profile(event, context);
        }
    }

    private void profile(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var user = guilds.guild(event.getGuild()).reputation().user(event.getOption("user").getAsMember());
        var profile = user.profile().adminProfile(configuration, context.localizer());
        event.replyEmbeds(profile).setEphemeral(true).queue();
    }

    private void reputation(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var user = event.getOption("user").getAsMember();
        var repUser = guilds.guild(event.getGuild()).reputation().user(user);
        var add = event.getOption("add");
        if (add != null) {
            repUser.addReputation(add.getAsLong());
            event.reply(context.localize("command.repadmin.sub.reputation.added",
                            Replacement.create("VALUE", add.getAsLong()), Replacement.createMention(user)))
                    .setEphemeral(true).queue();
            return;
        }

        var set = event.getOption("set");
        if (set != null) {
            repUser.setReputation(set.getAsLong());
            event.reply(context.localize("command.repadmin.sub.reputation.set",
                            Replacement.create("VALUE", set.getAsLong()), Replacement.createMention(user)))
                    .setEphemeral(true).queue();
            return;
        }

        var remove = event.getOption("remove");
        if (remove != null) {
            repUser.removeReputation(remove.getAsLong());
            event.reply(context.localize("command.repadmin.sub.reputation.removed",
                            Replacement.create("VALUE", remove.getAsLong()), Replacement.createMention(user)))
                    .setEphemeral(true).queue();
            return;
        }

        event.reply(context.localize("error.noAction")).setEphemeral(true).queue();
    }
}
