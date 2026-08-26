/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    protected boolean everyoneTokenPurchase;

    @JsonSerialize(using = ToStringSerializer.class)
    protected long systemChannel;

    /**
     * All values are required. A partial body is rejected instead of silently resetting the omitted settings, because
     * the settings are applied by difference.
     */
    @JsonCreator
    public GeneralPOJO(
            @JsonProperty(value = "stackRoles", required = true) boolean stackRoles,
            @JsonProperty(value = "language", required = true) DiscordLocale language,
            @JsonProperty(value = "reputationMode", required = true) ReputationMode reputationMode,
            @JsonProperty(value = "resetDate", required = true) Instant resetDate,
            @JsonProperty(value = "systemChannel", required = true) long systemChannel,
            @JsonProperty(value = "everyoneTokenPurchase", required = true) boolean everyoneTokenPurchase) {
        this.stackRoles = stackRoles;
        this.language = language;
        this.reputationMode = reputationMode;
        this.resetDate = resetDate;
        this.systemChannel = systemChannel;
        this.everyoneTokenPurchase = everyoneTokenPurchase;
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

    public boolean everyoneTokenPurchase() {
        return everyoneTokenPurchase;
    }
}
