package de.chojo.repbot.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.analyzer.results.match.ThankType;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.dao.snapshots.ReputationLogEntry;
import de.chojo.repbot.service.ReputationService;
import de.chojo.repbot.util.PermissionErrorHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEmojiEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class ReactionListener extends ListenerAdapter {
    private static final int REACTION_COOLDOWN = 30;
    private static final Logger log = getLogger(ReactionListener.class);
    private final Guilds guilds;
    private final ILocalizer localizer;
    private final ReputationService reputationService;
    private final Configuration configuration;
    private final Cache<Long, Instant> lastReaction = CacheBuilder.newBuilder().expireAfterAccess(60, TimeUnit.SECONDS)
                                                                  .build();

    public ReactionListener(Guilds guilds, ILocalizer localizer, ReputationService reputationService, Configuration configuration) {
        this.guilds = guilds;
        this.localizer = localizer;
        this.reputationService = reputationService;
        this.configuration = configuration;
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser().isBot() || !event.isFromGuild()) return;
        var repGuild = guilds.guild(event.getGuild());
        var guildSettings = repGuild.settings();

        if (!guildSettings.thanking().channels().isEnabled(event.getGuildChannel())) return;
        if (!guildSettings.reputation().isReactionActive()) return;
        if (!guildSettings.thanking().reactions().isReaction(event.getReaction())) return;

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

        var receiver = event.getGuild().retrieveMember(message.getAuthor()).complete();

        var logEntry = repGuild.reputation().log().getLogEntry(message);
        if (logEntry.isPresent()) {
            Member newReceiver;
            try {
                newReceiver = event.getGuild().retrieveMemberById(logEntry.get().receiverId()).complete();
            } catch (RuntimeException e) {
                return;
            }
            if (newReceiver == null) return;
            receiver = newReceiver;
        }

        if (PermissionErrorHandler.assertAndHandle(event.getGuildChannel(), localizer, configuration, Permission.MESSAGE_SEND)) {
            return;
        }


        if (reputationService.submitReputation(event.getGuild(), event.getMember(), receiver, message, null, ThankType.REACTION)) {
            reacted(event.getMember());
            if (guildSettings.messages().isReactionConfirmation()) {
                event.getChannel().sendMessage(localizer.localize("listener.reaction.confirmation", event.getGuild(),
                             Replacement.createMention("DONOR", event.getUser()),
                             Replacement.createMention("RECEIVER", receiver)))
                     .mention(event.getUser())
                     .onErrorFlatMap(err -> null)
                     .delay(30, TimeUnit.SECONDS)
                     .flatMap(Message::delete)
                     .onErrorMap(err -> null)
                     .queue();
            }
        }
    }


    @Override
    public void onMessageReactionRemoveEmoji(@NotNull MessageReactionRemoveEmojiEvent event) {
        if (!event.isFromGuild()) return;
        var guildSettings = guilds.guild(event.getGuild()).settings();
        if (!guildSettings.thanking().reactions().isReaction(event.getReaction())) return;
        guilds.guild(event.getGuild()).reputation().log().messageLog(event.getMessageIdLong(), 50).stream()
              .filter(entry -> entry.type() == ThankType.REACTION)
              .forEach(ReputationLogEntry::delete);
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (!event.isFromGuild()) return;
        var guildSettings = guilds.guild(event.getGuild()).settings();
        if (!guildSettings.thanking().reactions().isReaction(event.getReaction())) return;
        var entries = guilds.guild(event.getGuild()).reputation().log().messageLog(event.getMessageIdLong(), 50)
                            .stream()
                            .filter(entry -> entry.type() == ThankType.REACTION && entry.donorId() == event.getUserIdLong())
                            .toList();
        entries.forEach(ReputationLogEntry::delete);
        if (!entries.isEmpty() && guildSettings.messages().isReactionConfirmation()) {
            event.getChannel().sendMessage(localizer.localize("listener.reaction.removal", event.getGuild(),
                         Replacement.create("DONOR", User.fromId(event.getUserId()).getAsMention())))
                 .delay(30, TimeUnit.SECONDS).flatMap(Message::delete)
                 .queue(RestAction.getDefaultSuccess(), ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE));
        }
    }

    @Override
    public void onMessageReactionRemoveAll(@NotNull MessageReactionRemoveAllEvent event) {
        guilds.guild(event.getGuild()).reputation().log().messageLog(event.getMessageIdLong(), 50).stream()
              .filter(entry -> entry.type() == ThankType.REACTION)
              .forEach(ReputationLogEntry::delete);
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
