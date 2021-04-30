package de.chojo.repbot.data.updater;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
class Patch {
    private final int major;
    private final int patch;
    private final String query;
}
