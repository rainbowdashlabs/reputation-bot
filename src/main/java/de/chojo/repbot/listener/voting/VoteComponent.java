package de.chojo.repbot.listener.voting;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.components.Component;


public class VoteComponent {
    Member member;
    Component component;

    public VoteComponent(Member member, Component component) {
        this.member = member;
        this.component = component;
    }

    public Member member() {
        return member;
    }

    public Component component() {
        return component;
    }
}
