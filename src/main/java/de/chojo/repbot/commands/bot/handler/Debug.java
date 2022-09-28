package de.chojo.repbot.commands.bot.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.pagination.bag.ListPageBag;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.dao.snapshots.ReputationLogEntry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.chojo.jdautil.util.Guilds.prettyName;

public class Debug implements SlashHandler {
    private final Guilds guilds;

    public Debug(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guildId = event.getOption("guild_id").getAsLong();
        var guild = event.getJDA().getShardManager().getGuildById(guildId);
        if (guild == null) {
            event.reply("Guild not found").setEphemeral(true).queue();
            return;
        }

        var selfMember = guild.getSelfMember();
        var repGuild = guilds.guild(guild);
        var settings = repGuild.settings();
        var reputation = repGuild.reputation();

        List<MessageEmbed> embeds = new ArrayList<>();

        embeds.add(new EmbedBuilder()
                .setTitle("Information about guild " + prettyName(guild))
                .addField("Owner", guild.retrieveOwner().complete().getUser().getAsTag(), true)
                .addField("Member", String.valueOf(guild.getMemberCount()), true)
                .addField("Joined", timestamp(selfMember.getTimeJoined()), true)
                .addField("Total Reputation", String.valueOf(reputation.stats().totalReputation()), true)
                .addField("Week Reputation", String.valueOf(reputation.stats().weekReputation()), true)
                .addField("Today Reputation", String.valueOf(reputation.stats().todayReputation()), true)
                .addField("Latest reputation", timestamp(reputation.log().getLatestReputation()
                                                                   .map(ReputationLogEntry::received)
                                                                   .orElse(LocalDateTime.ofEpochSecond(0L, 0, ZoneOffset.UTC))), true)
                .build());

        embeds.add(new EmbedBuilder()
                .setTitle("Permissions")
                .setDescription(Arrays.stream(Permission.values())
                                      .filter(Predicate.not(Permission.UNKNOWN::equals))
                                      .map(perm -> (selfMember.hasPermission(perm) ? "✅ " : "❌ ") + perm.getName())
                                      .collect(Collectors.joining("\n")))
                .build());

        embeds.add(new EmbedBuilder()
                .setTitle("Settings")
                .addField("Reputation Settings", settings.reputation().prettyString(), true)
                .addField("General", settings.general().prettyString(), true)
                .addBlankField(false)
                .addField("Abuse Protection", settings.abuseProtection().prettyString(), true)
                .addField("Announcements", settings.announcements().prettyString(), true)
                .addBlankField(false)
                .addField("Message States", settings.messages().prettyString(), true)
                .build());

        var thanks = settings.thanking();

        embeds.add(new EmbedBuilder()
                .setTitle("Thank settings")
                .addField("Donor Roles", thanks.donorRoles().prettyString(), true)
                .addField("Receiver Roles", thanks.receiverRoles().prettyString(), true)
                .addBlankField(false)
                .addField("Channel count", String.valueOf(thanks.channels().channels().size()), true)
                .addField("Category Count", String.valueOf(thanks.channels().categories().size()), true)
                .addField("Thankwords", thanks.thankwords().prettyString(), false)
                .addField("Main Reaction", thanks.reactions().reactionMention().orElse("None"), true)
                .addField("Additional Reactions", String.join(" ", thanks.reactions()
                                                                         .getAdditionalReactionMentions()), true)
                .build());

        embeds.add(new EmbedBuilder()
                .setTitle("Ranks")
                .setDescription(settings.ranks().prettyString())
                .build());

        var pages = new ListPageBag<>(embeds) {
            @Override
            public CompletableFuture<MessageEmbed> buildPage() {
                return CompletableFuture.completedFuture(currentElement());
            }
        };

        context.registerPage(pages, true);
    }

    private String timestamp(LocalDateTime dateTime) {
        return TimeFormat.DATE_TIME_SHORT.format(dateTime.toEpochSecond(ZoneOffset.UTC) * 1000);
    }

    private String timestamp(OffsetDateTime dateTime) {
        return TimeFormat.DATE_TIME_SHORT.format(dateTime.toEpochSecond() * 1000);
    }
}
