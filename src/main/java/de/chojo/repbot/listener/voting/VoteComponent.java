package de.chojo.repbot.listener.voting;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.components.ActionComponent;


public class VoteComponent {
    Member member;
    ActionComponent component;

    public VoteComponent(Member member, ActionComponent component) {
        this.member = member;
        this.component = component;
    }

    public Member member() {
        return member;
    }

    public ActionComponent component() {
        return component;
    }
}
