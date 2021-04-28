package de.chojo.repbot.data.updater;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
class Patch {
    private int major;
    private int patch;
    private String query;
}
