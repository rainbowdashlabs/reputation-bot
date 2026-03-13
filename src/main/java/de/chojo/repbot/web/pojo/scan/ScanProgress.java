/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.scan;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import de.chojo.repbot.service.scanservice.ScanProcess;

import java.util.List;

/**
 * Represents the progress of a {@link ScanProcess}.
 * <p>
 * Scan progress is a nested object that might contain more scan progress objects that represent child entities of a progress.
 * A child might be the channels of a category, threads of a text channel, or threads of a forum channel.
 *
 * @param target  the type of the target channel
 * @param id      id of the channel. 0 if the result contains the threads of a text channel.
 * @param scanned amount of scanned messages. This is the sum of all sub processes.
 * @param hits    amount of given reputation. This is the sum of all sub processes.
 * @param childs  a list of child scan progress objects. These might be channels of a category, threads of a text channel, or threads of a forum channel.
 */
@JsonPropertyOrder({"target", "id", "scanned", "maxMessages", "hits", "childs"})
public record ScanProgress(
        ScanTarget target,
        @JsonSerialize(using = ToStringSerializer.class) long id,
        String name,
        int scanned,
        int maxMessages,
        int hits,
        List<ScanProgress> childs) {}
