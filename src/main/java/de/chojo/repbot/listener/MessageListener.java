package de.chojo.repbot.listener;

import de.chojo.jdautil.parsing.Verifier;
import de.chojo.repbot.analyzer.MessageAnalyzer;
import de.chojo.repbot.analyzer.ThankType;
import de.chojo.repbot.config.ConfigFile;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import de.chojo.repbot.manager.MemberCacheManager;
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
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.time.temporal.ChronoUnit;

@Slf4j
public class MessageListener extends ListenerAdapter {
    private final Configuration configuration;
    private final GuildData guildData;
    private final ReputationData reputationData;
    private final RoleAssigner roleAssigner;
    private final MemberCacheManager memberCacheManager;

    public MessageListener(DataSource dataSource, Configuration configuration, RoleAssigner roleAssigner, MemberCacheManager memberCacheManager) {
        guildData = new GuildData(dataSource);
        reputationData = new ReputationData(dataSource);
        this.configuration = configuration;
        this.roleAssigner = roleAssigner;
        this.memberCacheManager = memberCacheManager;
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
        if (event.getAuthor().isBot() || event.isWebhookMessage()) return;
        memberCacheManager.seen(event.getMember());
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

        var receiver = result.getReceiver();
        var donator = result.getDonator();
        var resultType = result.getType();
        if (resultType != ThankType.NO_MATCH) {
            if (Verifier.equalSnowflake(donator, receiver)) return;
        }

        var refMessage = result.getReferenceMessage();

        switch (resultType) {
            case FUZZY -> {
                if (!settings.isFuzzyActive()) return;
                if (result.getConfidenceScore() < 0.85) return;
                submitRepVote(guild, donator, receiver, message, refMessage, settings, resultType);
            }
            case MENTION -> {
                if (!settings.isMentionActive()) return;
                submitRepVote(guild, donator, receiver, message, refMessage, settings, resultType);
            }
            case ANSWER -> {
                if (!settings.isAnswerActive()) return;
                if (!settings.isFreshMessage(refMessage)) return;
                submitRepVote(guild, donator, receiver, message, refMessage, settings, resultType);
            }
            case NO_MATCH -> {
            }
        }
    }

    private void submitRepVote(Guild guild, User donator, User receiver, Message scope, Message refMessage, GuildSettings settings, ThankType type) {
        if (receiver.isBot()) return;
        var lastRatedDuration = reputationData.getLastRatedDuration(guild, donator, receiver, ChronoUnit.MINUTES);
        if (lastRatedDuration < settings.getCooldown()) return;

        if (reputationData.logReputation(guild, donator, receiver, scope, refMessage, type)) {
            markMessage(scope, refMessage, settings);
            roleAssigner.update(guild.getMember(receiver));
        }
    }


    public void markMessage(Message message, @Nullable Message refMessage, GuildSettings settings) {
        if (settings.reactionIsEmote()) {
            message.getGuild().retrieveEmoteById(settings.getReaction()).queue(e -> {
                message.addReaction(e).queue(emote -> {
                }, err -> log.error("Could not add reaction emote", err));
                if (refMessage != null) {
                    refMessage.addReaction(e).queue(emote -> {
                    }, err -> log.error("Could not add reaction emote", err));
                }
            }, err -> log.error("Could not resolve emoji.", err));
        } else {
            if (refMessage != null) {
                message.addReaction(settings.getReaction()).queue(e -> {
                }, err -> log.error("Could not add reaction emoji.", err));
            }
            message.addReaction(settings.getReaction()).queue(e -> {
            }, err -> log.error("Could not add reaction emoji.", err));
        }
    }
}
