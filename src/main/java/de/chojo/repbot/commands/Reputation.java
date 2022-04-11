package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.data.wrapper.ReputationRole;
import de.chojo.repbot.data.wrapper.ReputationUser;
import de.chojo.repbot.util.TextGenerator;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.sql.DataSource;
import java.util.Optional;

public class Reputation extends SimpleCommand {
    private static final int BAR_SIZE = 20;
    private final ReputationData reputationData;
    private final GuildData guildData;
    private final Configuration configuration;

    public Reputation(DataSource dataSource, Configuration configuration) {
        super(CommandMeta.builder("rep", "command.reputation.description")
                .addArgument(SimpleArgument.user("user", "command.reputation.description.arg.user")));
        reputationData = new ReputationData(dataSource);
        guildData = new GuildData(dataSource);
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
        var reputation = reputationData.getReputation(event.getGuild(), member.getUser()).orElse(ReputationUser.empty(event.getUser()));
        event.replyEmbeds(getUserRepEmbed(context, member, reputation)).queue();
    }

    private MessageEmbed getUserRepEmbed(SlashCommandContext context, Member member, ReputationUser reputation) {
        var roles = guildData.getCurrentReputationRole(member.getGuild(), reputation.reputation(), false);
        var next = guildData.getNextReputationRole(member.getGuild(), reputation.reputation());

        var current = Optional.ofNullable(roles.isEmpty() ? null : roles.get(0));

        var currentRoleRep = current.map(ReputationRole::reputation).orElse(0L);
        var nextRoleRep = next.map(ReputationRole::reputation).orElse(currentRoleRep);
        var progess = (double) (reputation.reputation() - currentRoleRep) / (double) (nextRoleRep - currentRoleRep);

        var progressBar = TextGenerator.progressBar(progess, BAR_SIZE);

        var level = current.map(r -> r.getRole(member.getGuild())).map(IMentionable::getAsMention).orElse("none");

        var currProgress = String.valueOf(reputation.reputation() - currentRoleRep);
        var nextLevel = nextRoleRep.equals(currentRoleRep) ? "Ꝏ" : String.valueOf(nextRoleRep - currentRoleRep);
        var build = new LocalizedEmbedBuilder(context.localizer())
                .setAuthor((reputation.rank() != 0 ? "#" + reputation.rank() + " " : "")
                           + "$command.reputation.profile.title$", null, member.getUser().getEffectiveAvatarUrl(),
                        Replacement.create("NAME", member.getEffectiveName()))
                .addField("words.level", level, true)
                .addField("words.reputation", Format.BOLD.apply(String.valueOf(reputation.reputation())), true)
                .addField("command.reputation.profile.nextLevel", currProgress + "/" + nextLevel + "  " + progressBar, false)
                .setColor(member.getColor());
        var badge = configuration.badges().badge((int) reputation.rank());
        badge.ifPresent(build::setThumbnail);
        return build.build();
    }
}
