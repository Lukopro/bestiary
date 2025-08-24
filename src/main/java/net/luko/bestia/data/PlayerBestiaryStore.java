package net.luko.bestia.data;

import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerBestiaryStore {
    private static final Map<UUID, BestiaryManager> STORE = new HashMap<>();

    public static void set(ServerPlayer player, BestiaryManager manager){
        STORE.put(player.getUUID(), manager);
    }

    public static BestiaryManager get(ServerPlayer player){
        return STORE.get(player.getUUID());
    }

    public static Map<UUID, BestiaryManager> getAll(){
        return Collections.unmodifiableMap(STORE);
    }

    public static void remove(ServerPlayer player){
        STORE.remove(player.getUUID());
    }
}
