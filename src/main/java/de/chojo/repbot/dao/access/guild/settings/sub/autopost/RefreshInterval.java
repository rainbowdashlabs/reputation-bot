package de.chojo.repbot.dao.access.guild.settings.sub.autopost;

public enum RefreshInterval {
    /**
     * Send on every full hour.
     */
    HOURLY,
    /**
     * Send at midnight UTC.
     */
    DAILY,
    /**
     * Send at midnight UTC every Monday.
     */
    WEEKLY,
    /**
     * Send at midnight UTC every first day of the month.
     */
    MONTHLY;
}
