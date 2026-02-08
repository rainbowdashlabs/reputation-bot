/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.debugService;

import net.dv8tion.jda.api.Permission;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class DebugResult {
    private final List<Permission> missingGlobalPermissions = new LinkedList<>();
    private final List<SimpleProblems> simpleProblems = new LinkedList<>();
    private final List<MissingPermissions> missingPermissions = new LinkedList<>();
    private final List<RankProblem> rankProblems = new LinkedList<>();
    private final List<ReputationChannelProblem> reputationChannelProblems = new LinkedList<>();
    private final List<SimpleWarning> simpleWarnings = new LinkedList<>();

    public List<Permission> getMissingGlobalPermissions() {
        return missingGlobalPermissions;
    }

    public List<SimpleProblems> getSimpleProblems() {
        return simpleProblems;
    }

    public List<MissingPermissions> getMissingPermissions() {
        return missingPermissions;
    }

    public List<RankProblem> getRankProblems() {
        return rankProblems;
    }

    public List<ReputationChannelProblem> getReputationChannelProblems() {
        return reputationChannelProblems;
    }

    public List<SimpleWarning> getSimpleWarnings() {
        return simpleWarnings;
    }

    public void addMissingGlobalPermission(Permission permission) {
        missingGlobalPermissions.add(permission);
    }

    public boolean isGloballyMissing(Permission permission) {
        return missingGlobalPermissions.contains(permission);
    }

    public void addSimpleProblem(SimpleProblems simpleProblems) {
        this.simpleProblems.add(simpleProblems);
    }

    public void addMissingPermissions(MissingPermissions missingPermissions) {
        Optional<MissingPermissions> first = this.missingPermissions.stream().filter(e -> e.equals(missingPermissions)).findFirst();
        if(first.isPresent()){
            first.get().getPermissions().addAll(missingPermissions.getPermissions());
            return;
        }
        this.missingPermissions.add(missingPermissions);
    }

    public void addRankProblem(RankProblem rankProblem) {
        rankProblems.add(rankProblem);
    }

    public void addReputationChannelProblem(ReputationChannelProblem reputationChannelProblem) {
        reputationChannelProblems.add(reputationChannelProblem);
    }

    public void addSimpleWarning(SimpleWarning simpleWarning) {
        simpleWarnings.add(simpleWarning);
    }
}
