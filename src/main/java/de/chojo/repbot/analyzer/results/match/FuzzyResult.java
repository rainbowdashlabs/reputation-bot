package de.chojo.repbot.analyzer.results.match;

import de.chojo.jdautil.parsing.WeightedEntry;
import de.chojo.repbot.analyzer.results.match.fuzzy.MemberMatch;
import de.chojo.repbot.dao.snapshots.analyzer.ResultSnapshot;
import de.chojo.repbot.dao.snapshots.analyzer.match.FuzzyResultSnapshot;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;
import java.util.stream.Collectors;

public class FuzzyResult extends MatchResult {
    private final List<String> thankwords;
    private final List<MemberMatch> memberMatches;
    private final List<WeightedEntry<Member>> weightedReceiver;

    public FuzzyResult(String match, List<String> thankwords, List<MemberMatch> memberMatches, Member donor, List<WeightedEntry<Member>> weightedReceiver) {
        super(ThankType.FUZZY, donor, match);
        this.thankwords = thankwords;
        this.memberMatches = memberMatches;
        this.weightedReceiver = weightedReceiver;
    }

    @Override
    public List<Member> receivers() {
        return weightedReceiver.stream().map(WeightedEntry::getReference).collect(Collectors.toList());
    }

    public List<WeightedEntry<Member>> weightedReceiver() {
        return weightedReceiver;
    }

    public List<MemberMatch> memberMatches() {
        return memberMatches;
    }

    public List<String> thankwords() {
        return thankwords;
    }

    @Override
    public ResultSnapshot toSnapshot() {
        return new FuzzyResultSnapshot(donor().getIdLong(), match(),thankwords, memberMatches);
    }
}
