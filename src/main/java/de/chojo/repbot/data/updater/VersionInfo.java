package de.chojo.repbot.data.updater;

class VersionInfo {
    private int version;
    private int patch;

    public VersionInfo(int version, int patch) {
        this.version = version;
        this.patch = patch;
    }

    public int version() {
        return version;
    }

    public int patch() {
        return patch;
    }
}
