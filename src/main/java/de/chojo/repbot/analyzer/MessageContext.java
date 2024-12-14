/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.analyzer;

import de.chojo.repbot.dao.components.MemberHolder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class representing the context of a message, including associated members, users, and messages.
 */
public class MessageContext implements MemberHolder {
    private final Set<Long> userIds = new HashSet<>();
    private final Set<Member> members = new HashSet<>();
    private final Set<User> users = new HashSet<>();
    private final Set<Message> rawMessages = new LinkedHashSet<>();
    private final Set<Message> contextMessages = new LinkedHashSet<>();
    private final Member target;
    private Message message;

    /**
     * Private constructor to initialize MessageContext with a message and target member.
     *
     * @param message the message associated with the context
     * @param target the target member
     */
    private MessageContext(Message message, Member target) {
        this.message = message;
        this.target = target;
    }

    /**
     * Creates a MessageContext instance using the given message.
     *
     * @param message the message to create the context from
     * @return a new MessageContext instance
     */
    public static MessageContext byMessage(Message message) {
        return new MessageContext(message, message.getMember());
    }

    /**
     * Creates a MessageContext instance using the given message and member.
     *
     * @param message the message to create the context from
     * @param member the member associated with the context
     * @return a new MessageContext instance
     */
    public static MessageContext byMessageAndMember(Message message, Member member) {
        return new MessageContext(message, member);
    }

    /**
     * Adds a collection of user IDs to the context.
     *
     * @param ids the collection of user IDs to add
     */
    public void addIds(Collection<Long> ids) {
        userIds.addAll(ids);
    }

    /**
     * Adds a collection of members to the context.
     *
     * @param members the collection of members to add
     */
    public void addMembers(Collection<Member> members) {
        this.members.addAll(members);
        users.addAll(members.stream().map(Member::getUser).toList());
    }

    /**
     * Adds a collection of raw messages to the context.
     *
     * @param messages the collection of raw messages to add
     */
    public void addRawMessages(Collection<Message> messages) {
        rawMessages.addAll(messages);
    }

    /**
     * Adds a collection of context messages to the context.
     *
     * @param messages the collection of context messages to add
     */
    public void addContextMessages(Collection<Message> messages) {
        contextMessages.addAll(messages);
    }

    /**
     * Adds a single member to the context.
     *
     * @param member the member to add
     */
    public void addMember(Member member) {
        members.add(member);
        users.add(member.getUser());
    }

    /**
     * Adds a single raw message to the context.
     *
     * @param message the raw message to add
     */
    public void addRawMessage(Message message) {
        rawMessages.add(message);
    }

    /**
     * Adds a single context message to the context.
     *
     * @param message the context message to add
     */
    public void addContextMessage(Message message) {
        contextMessages.add(message);
    }

    /**
     * Combines another MessageContext into this one.
     *
     * @param context the MessageContext to combine with this one
     * @return the combined MessageContext
     */
    public MessageContext combine(MessageContext context) {
        addIds(context.userIds);
        addRawMessages(context.rawMessages);
        addContextMessages(context.contextMessages);
        addMembers(context.members);
        return this;
    }

    /**
     * Refreshes the context with a new message.
     *
     * @param message the new message to refresh the context with
     * @return the refreshed MessageContext
     */
    protected MessageContext refresh(Message message) {
        this.message = message;
        return this;
    }

    /**
     * Gets the target member of the context.
     *
     * @return the target member
     */
    @Override
    public Member member() {
        return target;
    }

    /**
     * Gets an unmodifiable set of raw messages in the context.
     *
     * @return an unmodifiable set of raw messages
     */
    public Set<Message> rawMessages() {
        return Collections.unmodifiableSet(rawMessages);
    }

    /**
     * Gets an unmodifiable set of context messages in the context.
     *
     * @return an unmodifiable set of context messages
     */
    public Set<Message> contextMessages() {
        return Collections.unmodifiableSet(contextMessages);
    }

    /**
     * Gets the latest messages up to a specified limit.
     *
     * @param limit the maximum number of messages to retrieve
     * @return a set of the latest messages
     */
    public Set<Message> latestMessages(int limit) {
        return rawMessages().stream().limit(limit).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Resolves the guild members by the stored user IDs.
     *
     * @return the MessageContext for chaining
     */
    public MessageContext resolve() {
        addMembers(userIds.stream()
                          .map(id -> guild().retrieveMemberById(id).onErrorMap(e -> null).complete())
                          .filter(Objects::nonNull)
                          .collect(Collectors.toSet()));
        return this;
    }

    /**
     * Gets an unmodifiable set of members in the context.
     *
     * @return an unmodifiable set of members
     */
    public Set<Member> members() {
        return Collections.unmodifiableSet(members);
    }

    /**
     * Gets the message associated with the context.
     *
     * @return the message
     */
    public Message message() {
        return message;
    }

    /**
     * Gets an unmodifiable set of users in the context.
     *
     * @return an unmodifiable set of users
     */
    public Set<User> users() {
        return Collections.unmodifiableSet(users);
    }

    /**
     * Checks if the context is empty (i.e., has no members).
     *
     * @return true if the context is empty, false otherwise
     */
    public boolean isEmpty() {
        return members.isEmpty();
    }
}
