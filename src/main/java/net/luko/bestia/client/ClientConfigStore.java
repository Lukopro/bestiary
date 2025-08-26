package net.luko.bestia.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.luko.bestia.Bestia;
import net.luko.bestia.data.buff.special.SpecialBuffRegistry;
import net.luko.bestia.util.LevelFormula;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ClientConfigStore {
    // Use this to hold server-synced config values for proper display in menus

    public boolean enableSpecialBuffs;

    public int maxLevel;
    public String killsFormula;

    public double damageFactorPerLevel;
    public double resistanceFactorPerLevel;

    public int levelsPerSpecialBuffPoint;

    public int rerollMaxLevel;
    public int executeMaxLevel;
    public int lifestealMaxLevel;
    public int reflexMaxLevel;
    public int dazeMaxLevel;

    public double executeBuffPerLevel;
    public double lifestealBuffPerLevel;
    public double reflexBuffPerLevel;
    public int dazeBuffPerLevel;

    public int minLeaderboardLevel;

    public List<ResourceLocation> blacklistedEntities;
    public List<ResourceLocation> whitelistedEntities;

    public static ClientConfigStore INSTANCE = new ClientConfigStore();

    public static void sync(ClientConfigStore newStore){
        Bestia.LOGGER.info("Common config sync was received, storing and reinitializing data...");
        INSTANCE = newStore;
        SpecialBuffRegistry.reinitializeForClient();
        LevelFormula.reinitializeForClient();
    }

    public boolean entityIsBlacklisted(ResourceLocation id){
        return blacklistedEntities.contains(id);
    }

    public boolean entityIsWhitelisted(ResourceLocation id){
        return whitelistedEntities.contains(id);
    }
}
