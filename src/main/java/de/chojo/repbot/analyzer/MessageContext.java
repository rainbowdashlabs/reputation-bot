package de.chojo.repbot.analyzer;

import de.chojo.repbot.dao.components.MemberHolder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.Collection;
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
    private Message message;
    private Member target;

    public static MessageContext byMessage(Message message) {
        return new MessageContext(message, message.getMember());
    }

    public static MessageContext byMessageAndMember(Message message, Member member) {
        return new MessageContext(message, member);
    }

    private MessageContext(Message message, Member target) {
        this.message = message;
        this.target = target;
    }

    public void addIds(Collection<Long> ids) {
        this.userIds.addAll(ids);
    }

    public void addMembers(Collection<Member> members) {
        this.members.addAll(members);
        this.users.addAll(members.stream().map(Member::getUser).toList());
    }

    public void addRawMessages(Collection<Message> messages) {
        this.rawMessages.addAll(messages);
    }

    public void addContextMessages(Collection<Message> messages) {
        this.contextMessages.addAll(messages);
    }

    public void addMember(Member member) {
        this.members.add(member);
        this.users.add(member.getUser());
    }

    public void addRawMessage(Message message) {
        this.rawMessages.add(message);
    }

    public void addContextMessage(Message message) {
        this.contextMessages.add(message);
    }

    public MessageContext combine(MessageContext context) {
        addIds(context.userIds);
        addRawMessages(context.rawMessages);
        addContextMessages(context.contextMessages);
        addMembers(context.members);
        return this;
    }

    protected MessageContext refresh(Message message) {
        this.message = message;
        return this;
    }

    @Override
    public Member member() {
        return target;
    }

    public Set<Message> rawMessages() {
        return rawMessages;
    }

    public Set<Message> contextMessages() {
        return contextMessages;
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
        return members;
    }

    public Message message() {
        return message;
    }

    public Set<User> users() {
        return users;
    }

    public void removeMember(Member member) {
        members.remove(member);
        users.remove(member.getUser());
        userIds.remove(member.getIdLong());
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }
}
