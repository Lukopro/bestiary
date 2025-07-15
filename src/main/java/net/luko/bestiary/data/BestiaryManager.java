package net.luko.bestiary.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import oshi.util.tuples.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BestiaryManager {
    private final Map<ResourceLocation, Integer> killCounts = new HashMap<>();
    private final Map<ResourceLocation, BestiaryData> cachedData = new HashMap<>();

    public void loadFromNBT(CompoundTag bestiaryTag){
        for(String key : bestiaryTag.getAllKeys()){
            ResourceLocation mobId = ResourceLocation.parse(key);
            int kills = bestiaryTag.getInt(key);
            killCounts.put(mobId, kills);
            cachedData.put(mobId, computeBestiaryData(kills));
        }
    }

    public void onKill(ResourceLocation mobId){
        int newKills = killCounts.getOrDefault(mobId, 0) + 1;
        killCounts.put(mobId, newKills);
        cachedData.put(mobId, computeBestiaryData(newKills));
    }

    public int getKillCount(ResourceLocation mobId){
        return killCounts.getOrDefault(mobId, 0);
    }

    public BestiaryData getData(ResourceLocation mobId){
        return cachedData.getOrDefault(mobId, computeBestiaryData(0));
    }

    public Map<ResourceLocation, BestiaryData> getAllData(){
        return Collections.unmodifiableMap(cachedData);
    }

    public CompoundTag serializeNBT(){
        CompoundTag tag = new CompoundTag();
        for(Map.Entry<ResourceLocation, Integer> entry : killCounts.entrySet()){
            tag.putInt(entry.getKey().toString(), entry.getValue());
        }
        return tag;
    }

    private BestiaryData computeBestiaryData(int kills){
        Pair<Integer, Integer> levelAndRemaining = computeLevelAndRemaining(kills);
        int level = levelAndRemaining.getA();
        int remaining = levelAndRemaining.getB();
        MobBuff mobBuff = computeMobBuff(level);

        return new BestiaryData(kills, level, remaining, mobBuff);
    }

    private Pair<Integer, Integer> computeLevelAndRemaining(int kills){
        int level = Mth.floor((-1 + Math.sqrt(1 + 4 * kills)) / 2.0);
        int remaining = (level + 1) * (level + 2) - kills;
        return new Pair<>(level, remaining);
    }

    private MobBuff computeMobBuff(int level){
        float damageFactor = 1.0F + 0.05F * (float)level;
        float resistanceFactor = (float)Math.pow(0.95F, level);
        return new MobBuff(damageFactor, resistanceFactor);
    }
}
