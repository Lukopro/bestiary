package net.luko.bestia.data;

public enum BestiaryKey {
    ROOT("Bestiary"),
    ENTRIES("Entries"),
    PLAYER_NAME("PlayerName"),
    VERSION("Version");

    private final String key;

    BestiaryKey(String key){
        this.key = key;
    }

    public String get(){
        return this.key;
    }

    public enum Entry {
        ID("id"),
        KILLS("kills"),
        SPENT_POINTS("spent_points");

        private final String key;
        Entry(String key) { this.key = key; }
        public String get() { return key; }
    }
}
