package de.chojo.repbot.data.updater;

import lombok.Data;

@Data
class VersionInfo {
    private int version;
    private int patch;

    public VersionInfo(int version, int patch) {
        this.version = version;
        this.patch = patch;
    }
}
