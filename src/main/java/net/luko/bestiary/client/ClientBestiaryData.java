package net.luko.bestiary.client;

import net.luko.bestiary.data.BestiaryData;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ClientBestiaryData {
    private static final Map<ResourceLocation, BestiaryData> DATA = new HashMap<>();

    public static void set(Map<ResourceLocation, BestiaryData> newData){
        DATA.clear();
        DATA.putAll(newData);
    }

    public static BestiaryData getFor(ResourceLocation mobId){
        return DATA.get(mobId);
    }

    public static Map<ResourceLocation, BestiaryData> getAll(){
        return Collections.unmodifiableMap(DATA);
    }

    public static void clear(){
        DATA.clear();
    }
}
