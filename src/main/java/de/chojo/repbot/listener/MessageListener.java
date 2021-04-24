package de.chojo.repbot.listener;

import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.parsing.WeightedEntry;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import de.chojo.repbot.manager.RoleAssigner;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class MessageListener extends ListenerAdapter {
    private final Configuration configuration;
    private final GuildData guildData;
    private final ReputationData reputationData;
    private final RoleAssigner roleAssigner;
    private static final int LOOKAROUND = 6;

    public MessageListener(DataSource dataSource, Configuration configuration, RoleAssigner roleAssigner) {
        guildData = new GuildData(dataSource);
        reputationData = new ReputationData(dataSource);
        this.configuration = configuration;
        this.roleAssigner = roleAssigner;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        var optGuildSettings = guildData.getGuildSettings(event.getGuild());
        if (optGuildSettings.isEmpty()) return;
        var guildSettings = optGuildSettings.get();

        if (!guildSettings.isReputationChannel(event.getChannel())) return;

        var thankwordPattern = guildSettings.getThankwordPattern();

        var message = event.getMessage();
        var contentRaw = message.getContentRaw();

        if (message.getType() == MessageType.INLINE_REPLY) {
            if (!guildSettings.isAnswerActive()) return;
            if (!thankwordPattern.matcher(contentRaw).find()) return;

            var referencedMessage = message.getReferencedMessage();
            var minutes = referencedMessage.getTimeCreated().toInstant().until(Instant.now(), ChronoUnit.MINUTES);

            if (minutes > guildSettings.getMaxMessageAge()) return;

            submitRepVote(event.getGuild(), event.getAuthor(), referencedMessage.getAuthor(), message, guildSettings);
            return;
        }

        if (thankwordPattern.matcher(contentRaw).find()) {
            if (!guildSettings.isMentionActive()) return;
            var mentionedMembers = message.getMentionedUsers();
            if (mentionedMembers.size() > 0) {
                if (mentionedMembers.size() > 1) {
                    resolveMessage(event, guildSettings);
                    return;
                }
                submitRepVote(event.getGuild(), event.getAuthor(), mentionedMembers.get(0), message, guildSettings);
            } else {
                resolveMessage(event, guildSettings);
            }
        }
    }

    private void resolveMessage(GuildMessageReceivedEvent event, GuildSettings guildSettings) {
        var message = event.getMessage().getContentRaw();
        var thankwordPattern = guildSettings.getThankwordPattern();

        var words = List.of(message.split("\\s"));

        String match = null;
        for (var word : words) {
            if (thankwordPattern.matcher(word).find()) {
                match = word;
            }
        }
        List<String> resolve = new ArrayList<>();

        var thankwordindex = words.indexOf(match);
        resolve.addAll(words.subList(Math.max(0, thankwordindex - LOOKAROUND), thankwordindex));
        resolve.addAll(words.subList(Math.min(thankwordindex + 1, words.size() - 1), Math.min(words.size(), thankwordindex + LOOKAROUND + 1)));

        List<WeightedEntry<Member>> members = new ArrayList<>();

        for (var word : resolve) {
            var weightedMembers = DiscordResolver.fuzzyGuildUserSearch(event.getGuild(), word);
            if (weightedMembers.isEmpty()) continue;
            members.addAll(weightedMembers);
        }

        members.sort(Comparator.reverseOrder());
        if (members.isEmpty()) return;
        var memberWeightedEntry = members.get(0);

        if (memberWeightedEntry.getWeight() < 0.8) return;

        submitRepVote(event.getGuild(), event.getAuthor(), memberWeightedEntry.getReference().getUser(), event.getMessage(), guildSettings);
    }

    private void submitRepVote(Guild guild, User donator, User receiver, Message scope, GuildSettings settings) {
        var lastRatedDuration = reputationData.getLastRatedDuration(guild, donator, receiver, ChronoUnit.MINUTES);
        if (lastRatedDuration < settings.getCooldown()) return;

        if (reputationData.logReputation(guild, donator, receiver, scope)) {
            markMessage(scope, settings);
            roleAssigner.update(guild.getMember(receiver));
        }
    }


    public void markMessage(Message message, GuildSettings settings) {
        if (settings.reactionIsEmote()) {
            message.getGuild().retrieveEmoteById(settings.getReaction()).queue(e -> {
                message.addReaction(e).queue();
            });
        } else {
            message.addReaction(settings.getReaction()).queue();
        }
    }
}
