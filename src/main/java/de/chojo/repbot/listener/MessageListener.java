package de.chojo.repbot.listener;

import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.analyzer.MessageAnalyzer;
import de.chojo.repbot.analyzer.ThankType;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import de.chojo.repbot.manager.MemberCacheManager;
import de.chojo.repbot.manager.ReputationManager;
import de.chojo.repbot.util.HistoryUtil;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class MessageListener extends ListenerAdapter {
    private final Configuration configuration;
    private final GuildData guildData;
    private final ReputationData reputationData;
    private final MemberCacheManager memberCacheManager;
    private final String[] requestEmojis = new String[] {"1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "🔟"};
    private final ReactionListener reactionListener;
    private final Localizer localizer;
    private final ReputationManager reputationManager;

    public MessageListener(DataSource dataSource, Configuration configuration, MemberCacheManager memberCacheManager, ReactionListener reactionListener, Localizer localizer, ReputationManager reputationManager) {
        guildData = new GuildData(dataSource);
        reputationData = new ReputationData(dataSource);
        this.configuration = configuration;
        this.memberCacheManager = memberCacheManager;
        this.reactionListener = reactionListener;
        this.localizer = localizer;
        this.reputationManager = reputationManager;
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

        var prefix = settings.getPrefix().orElse(configuration.getDefaultPrefix());
        if (prefix.startsWith("re:")) {
            var compile = Pattern.compile(prefix.substring(3));
            if(compile.matcher(message.getContentRaw()).find()) return;
        }else {
            if (message.getContentRaw().startsWith(prefix)) return;
        }
        if (message.getContentRaw().startsWith(settings.getPrefix().orElse(configuration.getDefaultPrefix()))) {
            return;
        }

        var analyzerResult = MessageAnalyzer.processMessage(thankwordPattern, message, settings.getMaxMessageAge(), true, 0.85, 3);

        var donator = analyzerResult.getDonator();

        if (analyzerResult.getType() == ThankType.NO_MATCH) return;

        var resultType = analyzerResult.getType();
        var resolveNoTarget = true;
        for (var result : analyzerResult.getReceivers()) {
            var refMessage = analyzerResult.getReferenceMessage();
            switch (resultType) {
                case FUZZY -> {
                    if (!settings.isFuzzyActive()) return;
                    reputationManager.submitReputation(guild, donator, result.getReference().getUser(), message, refMessage, resultType);
                    resolveNoTarget = false;
                }
                case MENTION -> {
                    if (!settings.isMentionActive()) return;
                    reputationManager.submitReputation(guild, donator, result.getReference().getUser(), message, refMessage, resultType);
                    resolveNoTarget = false;
                }
                case ANSWER -> {
                    if (!settings.isAnswerActive()) return;
                    if (!settings.isFreshMessage(refMessage)) return;
                    reputationManager.submitReputation(guild, donator, result.getReference().getUser(), message, refMessage, resultType);
                    resolveNoTarget = false;
                }
            }
        }
        if (resolveNoTarget) {
            resolveNoTarget(message, settings);
        }
    }

    private void resolveNoTarget(Message message, GuildSettings settings) {
        var recentMembers = HistoryUtil.getRecentMembers(message, settings.getMaxMessageAge());
        recentMembers.remove(message.getMember());
        if (recentMembers.isEmpty()) return;

        List<Member> members;

        if (recentMembers.size() > 10) {
            members = recentMembers.stream().limit(10).collect(Collectors.toList());
        } else {
            members = new ArrayList<>(recentMembers);
        }

        List<String> first = new ArrayList<>();
        List<String> second = new ArrayList<>();
        Map<String, Member> targets = new LinkedHashMap<>();

        for (var i = 0; i < members.size(); i++) {
            targets.put(requestEmojis[i], members.get(i));
            (i % 2 == 0 ? first : second).add(requestEmojis[i] + " " + members.get(i).getAsMention());
        }

        var builder = new LocalizedEmbedBuilder(localizer, message.getGuild())
                .setTitle("listener.messages.request.title")
                .setDescription("listener.messages.request.descr")
                .addField("", String.join("\n", first), true)
                .addField("", String.join("\n", second), true)
                .setColor(Color.orange)
                .setFooter(localizer.localize("messages.destruction", message.getGuild(), Replacement.create("MIN", 1)));

        message.reply(builder.build()).queue(voteMessage -> {
            reactionListener.registerAfterVote(voteMessage, new VoteRequest(message.getMember(), builder, voteMessage, message, targets, Math.min(3, targets.size())));
            var i = 0;
            for (var reaction : targets.keySet()) {
                voteMessage.addReaction(reaction).queueAfter(i * 250L, TimeUnit.MILLISECONDS);
                i++;
            }
            voteMessage.addReaction("🗑️").queueAfter(i * 250L, TimeUnit.MILLISECONDS);
            voteMessage.delete().queueAfter(1, TimeUnit.MINUTES, e -> reactionListener.unregisterVote(voteMessage), err -> reactionListener.unregisterVote(voteMessage));
        });
    }
}
