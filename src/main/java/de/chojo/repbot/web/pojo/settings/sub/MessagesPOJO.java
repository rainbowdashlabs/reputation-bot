/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessagesPOJO {
    protected boolean commandReputationEphemeral;
    protected boolean announceReaction;
    protected boolean announceAnswer;
    protected boolean announceMention;
    protected boolean announceFuzzy;
    protected boolean announceEmbed;
    protected boolean announceDirect;
    protected boolean announceCommand;
    protected boolean announceDelete;

    /**
     * All values are required. A partial body is rejected instead of silently resetting the omitted settings,
     * because {@link de.chojo.repbot.dao.access.guild.settings.sub.Messages#apply(MessagesPOJO)} writes every
     * value which differs from the current state.
     */
    @JsonCreator
    public MessagesPOJO(
            @JsonProperty(value = "commandReputationEphemeral", required = true) boolean commandReputationEphemeral,
            @JsonProperty(value = "announceReaction", required = true) boolean announceReaction,
            @JsonProperty(value = "announceAnswer", required = true) boolean announceAnswer,
            @JsonProperty(value = "announceMention", required = true) boolean announceMention,
            @JsonProperty(value = "announceFuzzy", required = true) boolean announceFuzzy,
            @JsonProperty(value = "announceEmbed", required = true) boolean announceEmbed,
            @JsonProperty(value = "announceDirect", required = true) boolean announceDirect,
            @JsonProperty(value = "announceCommand", required = true) boolean announceCommand,
            @JsonProperty(value = "announceDelete", required = true) boolean announceDelete) {
        this.commandReputationEphemeral = commandReputationEphemeral;
        this.announceReaction = announceReaction;
        this.announceAnswer = announceAnswer;
        this.announceMention = announceMention;
        this.announceFuzzy = announceFuzzy;
        this.announceEmbed = announceEmbed;
        this.announceDirect = announceDirect;
        this.announceCommand = announceCommand;
        this.announceDelete = announceDelete;
    }

    public boolean isCommandReputationEphemeral() {
        return commandReputationEphemeral;
    }

    public boolean isAnnounceReaction() {
        return announceReaction;
    }

    public boolean isAnnounceAnswer() {
        return announceAnswer;
    }

    public boolean isAnnounceMention() {
        return announceMention;
    }

    public boolean isAnnounceFuzzy() {
        return announceFuzzy;
    }

    public boolean isAnnounceEmbed() {
        return announceEmbed;
    }

    public boolean isAnnounceDirect() {
        return announceDirect;
    }

    public boolean isAnnounceCommand() {
        return announceCommand;
    }

    public boolean isAnnounceDelete() {
        return announceDelete;
    }
}
