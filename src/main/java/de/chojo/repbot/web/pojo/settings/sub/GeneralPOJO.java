/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.time.Instant;
import java.util.Optional;

public class GeneralPOJO {
    protected boolean stackRoles;
    protected DiscordLocale language;
    protected ReputationMode reputationMode;
    protected Instant resetDate;
    @JsonSerialize(using = ToStringSerializer.class)
    protected long systemChannel;

    public GeneralPOJO(boolean stackRoles, DiscordLocale language, ReputationMode reputationMode, Instant resetDate, long systemChannel) {
        this.stackRoles = stackRoles;
        this.language = language;
        this.reputationMode = reputationMode;
        this.resetDate = resetDate;
        this.systemChannel = systemChannel;
    }

    public Optional<DiscordLocale> language() {
        return Optional.ofNullable(language);
    }

    public boolean isStackRoles() {
        return stackRoles;
    }

    public long systemChannel() {
        return systemChannel;
    }

    public Instant resetDate() {
        return resetDate;
    }

    public ReputationMode reputationMode() {
        return reputationMode;
    }
}
