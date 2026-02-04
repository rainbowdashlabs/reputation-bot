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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class MessageContext implements MemberHolder {
    private final Set<Long> userIds = new HashSet<>();
    private final Set<Member> members = new HashSet<>();
    private final Set<User> users = new HashSet<>();
    private final Set<Message> rawMessages = new LinkedHashSet<>();
    private final Set<Message> contextMessages = new LinkedHashSet<>();
    private final Member target;
    @Nullable
    private Message message;

    private MessageContext(@Nullable Message message, Member target) {
        this.message = message;
        this.target = target;
    }

    public static MessageContext byMessage(@NotNull Message message) {
        return new MessageContext(message, message.getMember());
    }

    public static MessageContext byMessageAndMember(@Nullable Message message, Member member) {
        return new MessageContext(message, member);
    }

    public void addIds(Collection<Long> ids) {
        userIds.addAll(ids);
    }

    public void addMembers(Collection<Member> members) {
        this.members.addAll(members);
        users.addAll(members.stream().map(Member::getUser).toList());
    }

    public void addRawMessages(Collection<Message> messages) {
        rawMessages.addAll(messages);
    }

    public void addContextMessages(Collection<Message> messages) {
        contextMessages.addAll(messages);
    }

    public void addMember(Member member) {
        members.add(member);
        users.add(member.getUser());
    }

    public void addRawMessage(Message message) {
        rawMessages.add(message);
    }

    public void addContextMessage(Message message) {
        contextMessages.add(message);
    }

    public MessageContext combine(MessageContext context) {
        addIds(context.userIds);
        addRawMessages(context.rawMessages);
        addContextMessages(context.contextMessages);
        addMembers(context.members);
        return this;
    }

    @Override
    public Member member() {
        return target;
    }

    public Set<Message> rawMessages() {
        return Collections.unmodifiableSet(rawMessages);
    }

    public Set<Message> contextMessages() {
        return Collections.unmodifiableSet(contextMessages);
    }

    public Set<Message> latestMessages(int limit) {
        return rawMessages().stream().limit(limit).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Resolves the guild members by the stored {@link #userIds}
     *
     * @return self for chaining
     */
    public MessageContext resolve() {
        addMembers(userIds.stream()
                          .map(id -> guild().retrieveMemberById(id).onErrorMap(e -> null).complete())
                          .filter(Objects::nonNull)
                          .collect(Collectors.toSet()));
        return this;
    }

    public Set<Member> members() {
        return Collections.unmodifiableSet(members);
    }

    @Nullable
    public Message message() {
        return message;
    }

    public Set<User> users() {
        return Collections.unmodifiableSet(users);
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    protected MessageContext refresh(Message message) {
        this.message = message;
        return this;
    }
}
