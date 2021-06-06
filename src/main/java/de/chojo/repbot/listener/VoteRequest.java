package de.chojo.repbot.listener;

import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Map;
import java.util.Optional;

public class VoteRequest {
    private final Member member;
    private final LocalizedEmbedBuilder embedBuilder;
    private final Message voteMessage;
    private final Message refMessage;
    private final Map<String, Member> voteTargets;
    private int remainingVotes;

    public VoteRequest(Member member, LocalizedEmbedBuilder embedBuilder, Message voteMessage, Message refMessage, Map<String, Member> voteTargets, int remainingVotes) {
        this.member = member;
        this.embedBuilder = embedBuilder;
        this.voteMessage = voteMessage;
        this.refMessage = refMessage;
        this.voteTargets = voteTargets;
        this.remainingVotes = remainingVotes;
    }

    public Optional<Member> getTarget(String emoji) {
        return Optional.ofNullable(voteTargets.get(emoji));
    }

    public void voted() {
        remainingVotes--;
    }

    public MessageEmbed getNewEmbed(String description) {
        return embedBuilder.setDescription(description).build();
    }

    public Member member() {
        return member;
    }

    public LocalizedEmbedBuilder embedBuilder() {
        return embedBuilder;
    }

    public Message voteMessage() {
        return voteMessage;
    }

    public Message refMessage() {
        return refMessage;
    }

    public Map<String, Member> voteTargets() {
        return voteTargets;
    }

    public int remainingVotes() {
        return remainingVotes;
    }
}
