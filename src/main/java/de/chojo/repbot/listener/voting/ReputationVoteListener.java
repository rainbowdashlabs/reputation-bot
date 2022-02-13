package de.chojo.repbot.listener.voting;

import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.repbot.analyzer.ThankType;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.data.wrapper.GuildSettings;
import de.chojo.repbot.service.ReputationService;
import de.chojo.repbot.util.EmojiDebug;
import de.chojo.repbot.util.Messages;
import de.chojo.repbot.util.PermissionErrorHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ReputationVoteListener extends ListenerAdapter {
    private static final ActionComponent DELETE = Button.of(ButtonStyle.DANGER, "vote:delete", Emoji.fromUnicode("🗑️"));
    private static final Pattern VOTE = Pattern.compile("vote:(?<id>[0-9]*?)");
    private final ReputationService reputationService;
    private final ILocalizer loc;
    private final Configuration configuration;
    private final Map<Long, VoteRequest> voteRequests = new HashMap<>();

    public ReputationVoteListener(ReputationService reputationService, ILocalizer localizer, Configuration configuration) {
        this.reputationService = reputationService;
        loc = localizer;
        this.configuration = configuration;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!voteRequests.containsKey(event.getMessageIdLong())) return;
        if (event.getButton() == null || event.getButton().getId() == null) return;
        event.deferEdit().queue();

        var matcher = VOTE.matcher(event.getButton().getId());
        if (!matcher.find()) return;
        var voteRequest = voteRequests.get(event.getMessageIdLong());
        if (!Verifier.equalSnowflake(voteRequest.member(), event.getMember())) {
            event.getHook().sendMessage(loc.localize("error.notYourEmbed", event.getGuild())).setEphemeral(true)
                    .queue(message -> {
                    }, throwable -> ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE));
            return;
        }
        if ("vote:delete".equals(event.getButton().getId())) {
            event.getMessage().delete().queue();
            voteRequests.remove(event.getMessageIdLong());
            return;
        }

        var target = voteRequest.getTarget(event.getButton().getId());

        if (!voteRequest.canVote()) return;

        if (reputationService.submitReputation(event.getGuild(), event.getUser(), target.get().getUser(), voteRequest.refMessage(), null, ThankType.EMBED)) {
            voteRequest.voted();
            voteRequest.remove(event.getButton().getId());
            voteRequest.voteMessage().
                    editMessageEmbeds(voteRequest.getNewEmbed(loc.localize("listener.messages.request.descrThank"
                            , event.getGuild(), Replacement.create("MORE", voteRequest.remainingVotes()))))
                    .setActionRows(getComponentRows(voteRequest.components()))
                    .queue(suc -> {}, ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE));
            if (voteRequest.remainingVotes() == 0) {
                voteRequest.voteMessage().delete().queueAfter(5, TimeUnit.SECONDS,
                        suc -> voteRequests.remove(voteRequest.voteMessage().getIdLong()), ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE));
            }
        }
    }

    public void registerVote(Message message, List<Member> members, GuildSettings settings) {
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

        if (settings.generalSettings().isEmojiDebug()) Messages.markMessage(message, EmojiDebug.PROMPTED);

        var collect = components.values().stream().map(VoteComponent::component).collect(Collectors.toUnmodifiableList());

        var componentRows = getComponentRows(collect);


        message.replyEmbeds(builder.build())
                .setActionRows(componentRows).queue(voteMessage -> {
                    voteRequests.put(voteMessage.getIdLong(), new VoteRequest(message.getMember(), builder, voteMessage, message, components, Math.min(3, members.size())));
                    voteMessage.delete().queueAfter(1, TimeUnit.MINUTES, submit -> voteRequests.remove(voteMessage.getIdLong()),
                            ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE));
                });
    }

    private List<ActionRow> getComponentRows(List<ActionComponent> components) {
        var rows = new ArrayList<ActionRow>();
        var from = 0;
        var to = 5;

        var splitting = new ArrayList<>(components);
        splitting.add(DELETE);

        while (from < splitting.size()) {
            rows.add(ActionRow.of(splitting.subList(from, Math.min(to, splitting.size()))));
            from += 5;
            to += 5;
        }

        return rows;
    }
}
