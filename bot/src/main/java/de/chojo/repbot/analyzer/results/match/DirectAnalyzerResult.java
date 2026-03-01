/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.analyzer.results.match;

import de.chojo.repbot.dao.snapshots.analyzer.ResultSnapshot;
import de.chojo.repbot.dao.snapshots.analyzer.match.DirectResultSnapshot;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

public class DirectAnalyzerResult extends MatchAnalyzerResult {
    private final List<Member> receivers;

    public DirectAnalyzerResult(String match, ThankType type, Member donor, List<Member> receivers) {
        super(type, donor, match);
        this.receivers = receivers;
    }

    @Override
    public List<Member> receivers() {
        return receivers;
    }

    @Override
    public ResultSnapshot toSnapshot() {
        return new DirectResultSnapshot(thankType(), donorId(), match(), receiverIds());
    }

    protected List<Long> receiverIds() {
        return receivers.stream().map(Member::getIdLong).toList();
    }
}
