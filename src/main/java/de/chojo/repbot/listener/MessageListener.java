package de.chojo.repbot.listener;

import de.chojo.jdautil.parsing.Verifier;
import de.chojo.repbot.analyzer.MessageAnalyzer;
import de.chojo.repbot.analyzer.ResultType;
import de.chojo.repbot.config.ConfigFile;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import de.chojo.repbot.manager.RoleAssigner;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.time.temporal.ChronoUnit;

@Slf4j
public class MessageListener extends ListenerAdapter {
    private final Configuration configuration;
    private final GuildData guildData;
    private final ReputationData reputationData;
    private final RoleAssigner roleAssigner;

    public MessageListener(DataSource dataSource, Configuration configuration, RoleAssigner roleAssigner) {
        guildData = new GuildData(dataSource);
        reputationData = new ReputationData(dataSource);
        this.configuration = configuration;
        this.roleAssigner = roleAssigner;
    }

    @Override
    public void onGuildMessageDelete(@NotNull GuildMessageDeleteEvent event) {
        reputationData.removeMessage(event.getMessageIdLong());
    }

    @Override
    public void onMessageBulkDelete(@NotNull MessageBulkDeleteEvent event) {
        event.getMessageIds().stream().map(Long::valueOf).forEach(reputationData::removeMessage);
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        var guild = event.getGuild();
        var optGuildSettings = guildData.getGuildSettings(guild);
        if (optGuildSettings.isEmpty()) return;
        var settings = optGuildSettings.get();

        if (!settings.isReputationChannel(event.getChannel())) return;

        var thankwordPattern = settings.getThankwordPattern();

        var message = event.getMessage();

        if (message.getContentRaw().startsWith(settings.getPrefix().orElse(configuration.get(ConfigFile::getDefaultPrefix)))) {
            return;
        }

        var result = MessageAnalyzer.processMessage(thankwordPattern, message);

        if (result.getType() != ResultType.NO_MATCH) {
            if (Verifier.equalSnowflake(result.getDonator(), result.getReceiver())) return;
        }

        switch (result.getType()) {
            case FUZZY -> {
                if (!settings.isFuzzyActive()) return;
                if (result.getConfidenceScore() < 0.85) return;
                submitRepVote(guild, result.getDonator(), result.getReceiver(), message, settings);
            }
            case MENTION -> {
                if (!settings.isMentionActive()) return;
                submitRepVote(guild, result.getDonator(), result.getReceiver(), message, settings);
            }
            case ANSWER -> {
                if (!settings.isAnswerActive()) return;
                if (!settings.isFreshMessage(result.getReferenceMessage())) return;
                submitRepVote(guild, result.getDonator(), result.getReceiver(), result.getReferenceMessage(), settings);
            }
            case NO_MATCH -> {
            }
        }
    }

    private void submitRepVote(Guild guild, User donator, User receiver, Message scope, GuildSettings settings) {
        if (receiver.isBot()) return;
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
                message.addReaction(e).queue(emote -> {
                }, err -> log.error("Could not add reaction emote", err));
            }, err -> {
                log.error("Could not resolve emoji.", err);
            });
        } else {
            message.addReaction(settings.getReaction()).queue(e -> {
            }, err -> log.error("Could not add reaction emoji.", err));
        }
    }
}
