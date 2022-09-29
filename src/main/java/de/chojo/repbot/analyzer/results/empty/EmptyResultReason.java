package de.chojo.repbot.analyzer.results.empty;

public enum EmptyResultReason {
    NO_PATTERN,
    NO_MATCH,
    REFERENCE_MESSAGE_NOT_FOUND,
    INSUFFICIENT_SCORE,
    INTERNAL_ERROR,
    TARGET_NOT_ON_GUILD
}
