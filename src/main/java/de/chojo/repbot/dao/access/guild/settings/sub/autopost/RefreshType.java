package de.chojo.repbot.dao.access.guild.settings.sub.autopost;

public enum RefreshType {
    /**
     * Delete old posts and send again.
     */
    DELETE_AND_REPOST,
    /**
     * Send again.
     */
    REPOST,
    /**
     * Update the original message.
     */
    UPDATE;
}
