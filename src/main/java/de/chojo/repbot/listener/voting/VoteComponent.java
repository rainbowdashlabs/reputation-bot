package de.chojo.repbot.listener.voting;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.components.ActionComponent;


public record VoteComponent(Member member, ActionComponent component) {
}
