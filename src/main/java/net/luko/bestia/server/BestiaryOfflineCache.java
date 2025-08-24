package net.luko.bestia.server;

import net.minecraft.nbt.CompoundTag;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BestiaryOfflineCache {
    private static final Map<UUID, CompoundTag> CACHED = new HashMap<>();

    public static void put(UUID uuid, CompoundTag bestiaryTag){
        CACHED.put(uuid, bestiaryTag);
    }

    public static CompoundTag get(UUID uuid){
        return CACHED.get(uuid);
    }

    public static Map<UUID, CompoundTag> getAll(){
        return Collections.unmodifiableMap(CACHED);
    }

    public static void remove(UUID uuid){
        CACHED.remove(uuid);
    }

    public static void clear(){
        CACHED.clear();
    }

    public static int size(){
        return CACHED.size();
    }
}
