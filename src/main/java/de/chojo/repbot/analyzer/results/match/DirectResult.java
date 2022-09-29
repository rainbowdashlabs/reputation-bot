package de.chojo.repbot.analyzer.results.match;

import net.dv8tion.jda.api.entities.Member;

import java.util.List;

public class DirectResult extends MatchResult {
    private final List<Member> receivers;
    public DirectResult(ThankType type, Member donor, List<Member> receivers) {
        super(type, donor);
        this.receivers = receivers;
    }

    @Override
    public List<Member> receivers() {
        return receivers;
    }
}
