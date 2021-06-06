package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.data.wrapper.ReputationRole;
import de.chojo.repbot.data.wrapper.ReputationUser;
import de.chojo.repbot.util.TextGenerator;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.sql.DataSource;

public class Reputation extends SimpleCommand {
    private static final int BAR_SIZE = 20;
    private final ReputationData reputationData;
    private final GuildData guildData;
    private final Localizer loc;

    public Reputation(DataSource dataSource, Localizer localizer) {
        super("reputation",
                new String[]{"rep"},
                "command.reputation.description",
                argsBuilder()
                        .add(OptionType.USER, "user", "user")
                        .build(),
                Permission.UNKNOWN);
        reputationData = new ReputationData(dataSource);
        guildData = new GuildData(dataSource);
        loc = localizer;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) {
            var reputation = reputationData.getReputation(eventWrapper.getGuild(), eventWrapper.getAuthor()).orElse(ReputationUser.empty(eventWrapper.getAuthor()));
            eventWrapper.reply(getUserRepEmbed(eventWrapper.getMember(), reputation)).queue();
            return true;
        }

        var optSubComd = context.argString(0);
        if (optSubComd.isEmpty()) {
            return false;
        }
        var subCmd = optSubComd.get();
        var guildMember = DiscordResolver.getGuildMember(eventWrapper.getGuild(), subCmd);
        if (guildMember.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.userNotFound"), 10);
            return true;
        }
        var reputation = reputationData.getReputation(eventWrapper.getGuild(), guildMember.get().getUser()).orElse(ReputationUser.empty(eventWrapper.getAuthor()));
        eventWrapper.reply(getUserRepEmbed(guildMember.get(), reputation)).queue();
        return true;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        var userOption = event.getOption("user");
        var member = userOption != null ? userOption.getAsMember() : event.getMember();
        var reputation = reputationData.getReputation(event.getGuild(), event.getUser()).orElse(ReputationUser.empty(event.getUser()));
        event.reply(wrap(getUserRepEmbed(member, reputation))).queue();
    }

    private MessageEmbed getUserRepEmbed(Member member, ReputationUser reputation) {
        var current = guildData.getCurrentReputationRole(member.getGuild(), reputation.reputation());
        var next = guildData.getNextReputationRole(member.getGuild(), reputation.reputation());

        var currentRoleRep = current.map(ReputationRole::reputation).orElse(0L);
        var nextRoleRep = next.map(ReputationRole::reputation).orElse(currentRoleRep);
        var progess = (double) (reputation.reputation() - currentRoleRep) / (double) (nextRoleRep - currentRoleRep);

        var progressBar = TextGenerator.progressBar(progess, BAR_SIZE);

        var level = current.map(r -> r.getRole(member.getGuild())).map(IMentionable::getAsMention).orElse("none");

        var currProgress = String.valueOf(reputation.reputation() - currentRoleRep);
        var nextLevel = nextRoleRep.equals(currentRoleRep) ? "\uA74E" : String.valueOf(nextRoleRep - currentRoleRep);
        return new LocalizedEmbedBuilder(loc, member.getGuild())
                .setTitle(
                        (reputation.rank() != 0 ? "#" + reputation.rank() + " " : "")
                                + loc.localize("command.reputation.profile.title",
                                member.getGuild(),
                                Replacement.create("NAME", member.getEffectiveName())))
                .addField("words.level", level, true)
                .addField(loc.localize("words.reputation", member.getGuild()), Format.BOLD.apply(String.valueOf(reputation.reputation())), true)
                .addField("command.reputation.profile.nextLevel", currProgress + "/" + nextLevel + "  " + progressBar, false)
                .setThumbnail(member.getUser().getEffectiveAvatarUrl())
                .setColor(member.getColor())
                .build();
    }
}
