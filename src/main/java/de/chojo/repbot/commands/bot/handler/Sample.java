/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.parsing.WeightedEntry;
import de.chojo.jdautil.util.SnowflakeCreator;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.analyzer.results.AnalyzerResult;
import de.chojo.repbot.analyzer.results.empty.EmptyResultReason;
import de.chojo.repbot.analyzer.results.match.ThankType;
import de.chojo.repbot.analyzer.results.match.fuzzy.MemberMatch;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.service.reputation.SubmitResult;
import de.chojo.repbot.service.reputation.SubmitResultType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.internal.entities.ReceivedMessage;

import java.util.Collections;
import java.util.List;

import static de.chojo.repbot.dao.access.guild.reputation.sub.Analyzer.MAPPER;
import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static java.util.concurrent.ThreadLocalRandom.current;

public class Sample implements SlashHandler {
    private final GuildRepository guildRepository;
    private final SnowflakeCreator snowflakeCreator = SnowflakeCreator.builder().build();

    public Sample(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        RepGuild repGuild = guildRepository.guild(event.getGuild());
        event.deferReply().queue();

        List<Member> members = event.getGuild().loadMembers().get().stream().limit(100).toList();
        List<TextChannel> channels = event.getGuild().getTextChannels();

        for (Member member : members) {
            for (int i = 0; i < current().nextInt(250); i++) {
                addReputation(member, members.get(current().nextInt(members.size())), channels.get(current().nextInt(channels.size())));
            }
        }

        for (int i = 0; i < 200; i++) {
            SubmitResult result = new SubmitResult(SubmitResultType.values()[current().nextInt(SubmitResultType.values().length)], Collections.emptyList());
            addReputationResults(event.getGuild(), channels.get(current().nextInt(channels.size())), snowflakeCreator.nextLong(), result);
        }

        event.getHook().sendMessage("Done").queue();
    }

    private void addReputation(Member receiver, Member donor, TextChannel channel) {
        var messageId = snowflakeCreator.nextLong();
        var thankType = ThankType.values()[current().nextInt(ThankType.values().length)];
        long refMessage = snowflakeCreator.nextLong();
        query("""
                INSERT INTO
                reputation_log(guild_id, donor_id, receiver_id, message_id, ref_message_id, channel_id, cause) VALUES(?,?,?,?,?,?,?)
                    ON CONFLICT(guild_id, donor_id, receiver_id, message_id)
                        DO NOTHING;
                """)
                .single(call().bind(receiver.getGuild().getIdLong())
                              .bind(donor == null ? 0 : donor.getIdLong())
                              .bind(receiver.getIdLong())
                              .bind(messageId)
                              .bind(thankType == ThankType.ANSWER ? refMessage : null)
                              .bind(channel.getIdLong())
                              .bind(thankType))
                .insert();

        var result = switch (thankType) {
            case FUZZY -> AnalyzerResult.fuzzy(receiver.getEffectiveName(),
                    List.of("thanks"),
                    List.of(new MemberMatch(donor.getUser().getName(), donor.getUser().getName(), donor.getEffectiveName(), 1)),
                    donor,
                    List.of(WeightedEntry.directMatch(receiver)));
            case MENTION -> AnalyzerResult.mention(receiver.getEffectiveName(), donor, List.of(receiver));
            case ANSWER ->
                    AnalyzerResult.answer(receiver.getEffectiveName(), donor, receiver, build(refMessage, channel, receiver));
            case DIRECT, EMBED, REACTION ->
                    AnalyzerResult.empty(receiver.getEffectiveName(), EmptyResultReason.NO_MATCH);
        };

        addAnalyzerResult(receiver.getGuild(), channel, messageId, result);
    }

    private Message build(long messageId, TextChannel channel, Member member) {
        return new ReceivedMessage(messageId, channel.getIdLong(), member.getIdLong(), member.getJDA(), member.getGuild(), channel, MessageType.DEFAULT, null, false,
                0, false, false, "", "", member.getUser(), member, null, null, null, null, Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                0, null, null, null, 0
        );
    }

    public void addAnalyzerResult(Guild guild, TextChannel guildChannel, long messageId, AnalyzerResult result) {
        String resultString;
        try {
            resultString = MAPPER.writeValueAsString(result.toSnapshot());
        } catch (JsonProcessingException e) {
            return;
        }
        query("""
                INSERT INTO analyzer_results(guild_id, channel_id, message_id, result) VALUES(?, ?, ?, ?::JSONB)
                    ON CONFLICT (guild_id, message_id)
                        DO NOTHING;
                """).single(call().bind(guild.getIdLong())
                                  .bind(guildChannel.getIdLong())
                                  .bind(messageId)
                                  .bind(resultString))
                    .insert();
    }

    public void addReputationResults(Guild guild, TextChannel guildChannel, long messageId, SubmitResult result) {
        String resultString;
        try {
            resultString = MAPPER.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return;
        }
        query("""
                INSERT INTO reputation_results(guild_id, channel_id, message_id, result) VALUES(?, ?, ?, ?::JSONB);
                """).single(call().bind(guild.getIdLong())
                                  .bind(guildChannel.getIdLong())
                                  .bind(messageId)
                                  .bind(resultString))
                    .insert();

    }
}
