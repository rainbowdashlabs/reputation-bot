package de.chojo.repbot.listener.voting;

import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.repbot.analyzer.ThankType;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.service.ReputationService;
import de.chojo.repbot.util.EmojiDebug;
import de.chojo.repbot.util.Messages;
import de.chojo.repbot.util.PermissionErrorHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ReputationVoteListener extends ListenerAdapter {
    private static final ActionComponent DELETE = Button.of(ButtonStyle.DANGER, "vote:delete", Emoji.fromUnicode("🗑️"));
    private static final Pattern VOTE = Pattern.compile("vote:(?<id>[0-9]*?)");
    private Guilds guilds;
    private final ReputationService reputationService;
    private final ILocalizer loc;
    private final Configuration configuration;
    private final Map<Long, VoteRequest> voteRequests = new HashMap<>();

    public ReputationVoteListener(Guilds guilds, ReputationService reputationService, ILocalizer localizer, Configuration configuration) {
        this.guilds = guilds;
        this.reputationService = reputationService;
        loc = localizer;
        this.configuration = configuration;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!voteRequests.containsKey(event.getMessageIdLong())) return;
        if (event.getButton().getId() == null) return;

        var matcher = VOTE.matcher(event.getButton().getId());
        if (!matcher.find()) return;
        if (!event.isAcknowledged()) {
            event.deferEdit().queue();
        }
        var voteRequest = voteRequests.get(event.getMessageIdLong());
        if (!Verifier.equalSnowflake(voteRequest.member(), event.getMember())) {
            event.getHook().sendMessage(loc.localize("error.notYourEmbed", event.getGuild())).setEphemeral(true)
                    .queue(RestAction.getDefaultSuccess(), ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE));
            return;
        }
        if ("vote:delete".equals(event.getButton().getId())) {
            event.getMessage().delete().queue(RestAction.getDefaultSuccess(), ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE));
            voteRequests.remove(event.getMessageIdLong());
            return;
        }

        var target = voteRequest.getTarget(event.getButton().getId());

        if (!voteRequest.canVote()) return;

        if (reputationService.submitReputation(event.getGuild(), event.getMember(), target.get(), voteRequest.refMessage(), null, ThankType.EMBED)) {
            voteRequest.voted();
            voteRequest.remove(event.getButton().getId());
            voteRequest.voteMessage().
                    editMessageEmbeds(voteRequest.getNewEmbed(loc.localize("listener.messages.request.descrThank"
                            , event.getGuild(), Replacement.create("MORE", voteRequest.remainingVotes()))))
                    .setActionRows(ActionRow.partitionOf(voteRequest.components()))
                    .queue(suc -> {
                    }, ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE));
            if (voteRequest.remainingVotes() == 0) {
                voteRequest.voteMessage().delete().queueAfter(5, TimeUnit.SECONDS,
                        suc -> voteRequests.remove(voteRequest.voteMessage().getIdLong()), ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE));
            }
        }
    }

    public void registerVote(Message message, List<Member> members, Settings settings) {
        if (PermissionErrorHandler.assertAndHandle(message.getGuildChannel(), loc, configuration, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS)) {
            return;
        }

        var builder = new LocalizedEmbedBuilder(loc, message.getGuild())
                .setTitle("listener.messages.request.title")
                .setDescription("listener.messages.request.descr")
                .setColor(Color.orange)
                .setFooter(loc.localize("messages.destruction", message.getGuild(), Replacement.create("MIN", 1)));

        Map<String, VoteComponent> components = new LinkedHashMap<>();

        for (var member : members) {
            var id = "vote:" + member.getIdLong();
            components.put(id, new VoteComponent(member, Button.of(ButtonStyle.PRIMARY, id, member.getEffectiveName())));
        }

        if (settings.general().isEmojiDebug()) Messages.markMessage(message, EmojiDebug.PROMPTED);

        var componentRows = ActionRow.partitionOf(components.values().stream().map(VoteComponent::component).toList());

<<<<<<< HEAD
        var remaining = Math.min(3, settings.abuseProtection().maxGivenHours() - settings.repGuild().reputation().user(message.getMember()).countReceived());

        if (remaining == 0) {
            if (settings.general().isEmojiDebug()) Messages.markMessage(message, EmojiDebug.DONOR_LIMIT);
            return;
        }

        message.replyEmbeds(builder.build())
                .setActionRows(componentRows).queue(voteMessage -> {
                    voteRequests.put(voteMessage.getIdLong(), new VoteRequest(message.getMember(), builder, voteMessage, message, components, Math.min(remaining, members.size())));
=======
        var maxMessageReputation = guilds.guild(message.getGuild()).settings().abuseProtection().maxMessageReputation();

        var remaining = Math.min(maxMessageReputation, settings.abuseProtection().maxGivenHours() - settings.repGuild().reputation().user(message.getMember()).countReceived());

        if (remaining == 0) {
            if (settings.general().isEmojiDebug()) Messages.markMessage(message, EmojiDebug.DONOR_LIMIT);
            return;
        }

        message.replyEmbeds(builder.build())
                .setActionRows(componentRows).queue(voteMessage -> {
<<<<<<< HEAD
                    voteRequests.put(voteMessage.getIdLong(),
                            new VoteRequest(message.getMember(), builder, voteMessage, message, components, Math.min(maxMessageReputation, members.size())));
>>>>>>> 412b2cf (Make the max reputation per message configurable #149)
=======
                    voteRequests.put(voteMessage.getIdLong(), new VoteRequest(message.getMember(), builder, voteMessage, message, components, Math.min(remaining, members.size())));
>>>>>>> 5c73a17 (implement max reputation per hour/s #15 (#327))
                    voteMessage.delete().queueAfter(1, TimeUnit.MINUTES,
                            submit -> voteRequests.remove(voteMessage.getIdLong()),
                            ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE, ErrorResponse.UNKNOWN_CHANNEL));
                });
    }
}
