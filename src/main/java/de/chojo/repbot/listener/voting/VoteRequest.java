/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.listener.voting;

import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionComponent;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Class representing a vote request.
 */
public class VoteRequest {
    private final Member member;
    private final LocalizedEmbedBuilder embedBuilder;
    private final Message voteMessage;
    private final Message refMessage;
    private final Map<String, VoteComponent> voteTargets;
    private int remainingVotes;

    /**
     * Constructs a new VoteRequest.
     *
     * @param member the member initiating the vote
     * @param embedBuilder the embed builder for creating message embeds
     * @param voteMessage the message containing the vote
     * @param refMessage the reference message
     * @param voteTargets the map of vote targets
     * @param remainingVotes the number of remaining votes
     */
    public VoteRequest(Member member, LocalizedEmbedBuilder embedBuilder, Message voteMessage, Message refMessage, Map<String, VoteComponent> voteTargets, int remainingVotes) {
        this.member = member;
        this.embedBuilder = embedBuilder;
        this.voteMessage = voteMessage;
        this.refMessage = refMessage;
        this.voteTargets = voteTargets;
        this.remainingVotes = remainingVotes;
    }

    /**
     * Retrieves the target member for the given ID.
     *
     * @param id the ID of the vote target
     * @return an Optional containing the target member if present, otherwise an empty Optional
     */
    public Optional<Member> getTarget(String id) {
        return Optional.ofNullable(voteTargets.get(id).member());
    }

    /**
     * Decrements the remaining votes count.
     */
    public void voted() {
        remainingVotes--;
    }

    /**
     * Creates a new message embed with the given description.
     *
     * @param description the description for the embed
     * @return the created MessageEmbed
     */
    public MessageEmbed getNewEmbed(String description) {
        return embedBuilder.setDescription(description).build();
    }

    /**
     * Retrieves the member initiating the vote.
     *
     * @return the member initiating the vote
     */
    public Member member() {
        return member;
    }

    /**
     * Retrieves the embed builder.
     *
     * @return the embed builder
     */
    public LocalizedEmbedBuilder embedBuilder() {
        return embedBuilder;
    }

    /**
     * Retrieves the vote message.
     *
     * @return the vote message
     */
    public Message voteMessage() {
        return voteMessage;
    }

    /**
     * Retrieves the reference message.
     *
     * @return the reference message
     */
    public Message refMessage() {
        return refMessage;
    }

    /**
     * Retrieves the map of vote targets.
     *
     * @return the map of vote targets
     */
    public Map<String, VoteComponent> voteTargets() {
        return voteTargets;
    }

    /**
     * Retrieves the list of action components for the vote targets.
     *
     * @return the list of action components
     */
    public List<ActionComponent> components() {
        return voteTargets.values().stream().map(VoteComponent::component).toList();
    }

    /**
     * Retrieves the number of remaining votes.
     *
     * @return the number of remaining votes
     */
    public int remainingVotes() {
        return remainingVotes;
    }

    /**
     * Removes a vote target by ID.
     *
     * @param id the ID of the vote target to remove
     */
    public void remove(String id) {
        voteTargets.remove(id);
    }

    /**
     * Checks if the member can still vote.
     *
     * @return true if the member can still vote, false otherwise
     */
    public boolean canVote() {
        return remainingVotes > 0;
    }
}
