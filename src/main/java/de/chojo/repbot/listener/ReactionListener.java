package de.chojo.repbot.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.analyzer.ThankType;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.service.ReputationService;
import de.chojo.repbot.util.PermissionErrorHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEmoteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class ReactionListener extends ListenerAdapter {
    private static final Logger log = getLogger(ReactionListener.class);
    public static final int REACTION_COOLDOWN = 30;
    private final GuildData guildData;
    private final ReputationData reputationData;
    private final Localizer localizer;
    private final ReputationService reputationService;
    private final Configuration configuration;
    private final Cache<Long, Instant> lastReaction = CacheBuilder.newBuilder().expireAfterAccess(60, TimeUnit.SECONDS).build();

    public ReactionListener(DataSource dataSource, Localizer localizer, ReputationService reputationService, Configuration configuration) {
        guildData = new GuildData(dataSource);
        reputationData = new ReputationData(dataSource);
        this.localizer = localizer;
        this.reputationService = reputationService;
        this.configuration = configuration;
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot()) return;
        var optGuildSettings = guildData.getGuildSettings(event.getGuild());
        if (optGuildSettings.isEmpty()) return;
        var guildSettings = optGuildSettings.get();

        if (!guildSettings.isReputationChannel(event.getChannel())) return;
        if (!guildSettings.isReactionActive()) return;
        if (!guildSettings.isReaction(event.getReaction().getReactionEmote())) return;

        if (isCooldown(event.getMember())) return;

        Message message;
        try {
            message = event.getChannel()
                    .retrieveMessageById(event.getMessageId())
                    .timeout(10, TimeUnit.SECONDS)
                    .onErrorMap(err -> null)
                    .complete();
        } catch (InsufficientPermissionException e) {
            PermissionErrorHandler.handle(e, event.getGuild(), localizer, configuration);
            return;
        }

        if (message == null) return;

        var receiver = message.getAuthor();

        var logEntry = reputationData.getLogEntry(message);
        if (logEntry.isPresent()) {
            Member newReceiver;
            try {
                newReceiver = event.getGuild().retrieveMemberById(logEntry.get().receiverId()).complete();
            } catch (RuntimeException e) {
                return;
            }
            if (newReceiver == null) return;
            receiver = newReceiver.getUser();
        }

        if (PermissionErrorHandler.assertAndHandle(event.getChannel(), localizer, configuration, Permission.MESSAGE_WRITE)) {
            return;
        }


        if (reputationService.submitReputation(event.getGuild(), event.getUser(), receiver, message, null, ThankType.REACTION)) {
            reacted(event.getMember());
            event.getChannel().sendMessage(localizer.localize("listener.reaction.confirmation", event.getGuild(),
                    Replacement.create("DONOR", event.getUser().getAsMention()), Replacement.create("RECEIVER", receiver.getAsMention())))
                    .mention(event.getUser())
                    .onErrorFlatMap(err -> null)
                    .delay(30, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .onErrorMap(err -> null)
                    .queue();
        }
    }

    @Override
    public void onGuildMessageReactionRemoveEmote(@NotNull GuildMessageReactionRemoveEmoteEvent event) {
        var optGuildSettings = guildData.getGuildSettings(event.getGuild());
        if (optGuildSettings.isEmpty()) return;
        var guildSettings = optGuildSettings.get();
        if (!guildSettings.isReaction(event.getReactionEmote())) return;
        reputationData.removeMessage(event.getMessageIdLong());
    }

    @Override
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
        var optGuildSettings = guildData.getGuildSettings(event.getGuild());
        if (optGuildSettings.isEmpty()) return;
        var guildSettings = optGuildSettings.get();
        if (!guildSettings.isReaction(event.getReactionEmote())) return;
        if (reputationData.removeReputation(event.getUserIdLong(), event.getMessageIdLong(), ThankType.REACTION)) {
            event.getChannel().sendMessage(localizer.localize("listener.reaction.removal", event.getGuild(),
                    Replacement.create("DONOR", User.fromId(event.getUserId()).getAsMention())))
                    .delay(30, TimeUnit.SECONDS).flatMap(Message::delete).queue();
        }
    }

    @Override
    public void onGuildMessageReactionRemoveAll(@NotNull GuildMessageReactionRemoveAllEvent event) {
        reputationData.removeMessage(event.getMessageIdLong());
    }

    public boolean isCooldown(Member member) {
        try {
            return lastReaction.get(member.getIdLong(), () -> Instant.MIN)
                    .isAfter(Instant.now().minus(REACTION_COOLDOWN, ChronoUnit.SECONDS));
        } catch (ExecutionException e) {
            log.error("Could not compute instant", e);
        }
        return true;
    }

    public void reacted(Member member) {
        lastReaction.put(member.getIdLong(), Instant.now());
    }
}
