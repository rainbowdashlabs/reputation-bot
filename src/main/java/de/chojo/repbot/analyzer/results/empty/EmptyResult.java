package de.chojo.repbot.analyzer.results.empty;

import de.chojo.repbot.analyzer.results.Result;
import de.chojo.repbot.analyzer.results.ResultType;

import javax.annotation.Nullable;

public class EmptyResult implements Result {
    @Nullable
    private final String match;
    private final EmptyResultReason reason;

    public EmptyResult(@Nullable String match, EmptyResultReason reason) {
        this.match = match;
        this.reason = reason;
    }

    @Override
    public ResultType resultType() {
        return ResultType.NO_MATCH;
    }

    public EmptyResultReason reason() {
        return reason;
    }

    @Nullable
    public String match() {
        return match;
    }
}
