package de.chojo.repbot.analyzer.results.match.fuzzy;

public class MemberMatch {
    private final String word;
    private final String name;
    private final String nickname;
    private final double score;

    public MemberMatch(String word, String name, String nickname, double score) {
        this.word = word;
        this.name = name;
        this.nickname = nickname;
        this.score = score;
    }

    public String word() {
        return word;
    }

    public String name() {
        return name;
    }

    public String nickname() {
        return nickname;
    }

    public double score() {
        return score;
    }
}
