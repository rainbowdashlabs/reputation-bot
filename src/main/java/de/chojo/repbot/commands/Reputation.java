package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.reputation.sub.RepUser;
import de.chojo.repbot.dao.access.guild.settings.sub.Ranks;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.dao.snapshots.ReputationRank;
import de.chojo.repbot.dao.snapshots.RepProfile;
import de.chojo.repbot.util.TextGenerator;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Optional;

public class Reputation extends SimpleCommand {
    private static final int BAR_SIZE = 20;
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
        var reputation = guilds.guild(event.getGuild()).reputation().user(member);
        event.replyEmbeds(getUserRepEmbed(context, member, reputation)).queue();
    }

    private MessageEmbed getUserRepEmbed(SlashCommandContext context, Member member, RepUser user) {
        var ranks = guilds.guild(member.getGuild()).settings().ranks();
        var roles = ranks.currentRank(user);
        var next = ranks.nextRank(user);

        var profile = user.profile();

        var current = Optional.ofNullable(roles.isEmpty() ? null : roles.get(0));

        var currentRoleRep = current.map(ReputationRank::reputation).orElse(0L);
        var nextRoleRep = next.map(ReputationRank::reputation).orElse(currentRoleRep);
        var progess = (double) (profile.reputation() - currentRoleRep) / (double) (nextRoleRep - currentRoleRep);

        var progressBar = TextGenerator.progressBar(progess, BAR_SIZE);

        var level = current.map(r -> r.getRole(member.getGuild())).map(IMentionable::getAsMention).orElse("none");

        var currProgress = String.valueOf(profile.reputation() - currentRoleRep);
        var nextLevel = nextRoleRep.equals(currentRoleRep) ? "Ꝏ" : String.valueOf(nextRoleRep - currentRoleRep);
        var build = new LocalizedEmbedBuilder(context.localizer())
                .setAuthor((profile.rank() != 0 ? "#" + profile.rank() + " " : "")
                           + "$command.reputation.profile.title$", null, member.getUser().getEffectiveAvatarUrl(),
                        Replacement.create("NAME", member.getEffectiveName()))
                .addField("words.level", level, true)
                .addField("words.reputation", Format.BOLD.apply(String.valueOf(profile.reputation())), true)
                .addField("command.reputation.profile.nextLevel", currProgress + "/" + nextLevel + "  " + progressBar, false)
                .setColor(member.getColor());
        var badge = configuration.badges().badge((int) profile.rank());
        badge.ifPresent(build::setThumbnail);
        return build.build();
    }
}
