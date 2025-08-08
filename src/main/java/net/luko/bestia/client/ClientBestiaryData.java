package net.luko.bestia.client;

import net.luko.bestia.data.BestiaryData;
import net.luko.bestia.screen.BestiaryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ClientBestiaryData {
    private static final Map<ResourceLocation, BestiaryData> DATA = new HashMap<>();
    private static boolean openScreenAfterSync = false;

    public static void set(Map<ResourceLocation, BestiaryData> newData){
        DATA.clear();
        DATA.putAll(newData);

        if(openScreenAfterSync){
            openScreenAfterSync = false;
            Minecraft.getInstance().setScreen(new BestiaryScreen(DATA));
        }
    }

    public static void scheduleScreenOpenAfterSync(){
        openScreenAfterSync = true;
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
