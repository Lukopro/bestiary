package net.luko.bestia.data;

import net.luko.bestia.Bestia;
import net.luko.bestia.data.buff.MobBuff;
import net.luko.bestia.data.buff.special.SpecialBuffRegistry;
import net.luko.bestia.network.ModPackets;
import net.luko.bestia.network.BestiarySyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.network.PacketDistributor;
import oshi.util.tuples.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BestiaryManager {
    private final Map<ResourceLocation, Integer> killCounts = new HashMap<>();
    private final Map<ResourceLocation, BestiaryData> cachedData = new HashMap<>();
    private final Map<ResourceLocation, Map<ResourceLocation, Integer>> specialBuffPoints = new HashMap<>();

    public void loadFromNBT(CompoundTag bestiaryTag){
        if(bestiaryTag.contains("Entries")){
            loadEntriesNBT(bestiaryTag.getList("Entries", Tag.TAG_COMPOUND));
        } else {
            loadEntriesNBT(convertToNewBestiaryTag(bestiaryTag).getList("Entries", Tag.TAG_COMPOUND));
        }
    }

    private void loadEntriesNBT(ListTag entriesTag){
        for(Tag t : entriesTag){
            CompoundTag tag = (CompoundTag) t;
            ResourceLocation mobId = ResourceLocation.tryParse(tag.getString("id"));
            if(mobId == null) {
                Bestia.LOGGER.warn("{} was not parsed correctly. Data for this mob will not persist.",
                        tag.getString("id"));
                continue;
            }
            int kills = tag.getInt("kills");
            Map<ResourceLocation, Integer> spentPoints = loadSpecialBuffs(tag.getCompound("spent_points"));
            killCounts.put(mobId, kills);
            specialBuffPoints.put(mobId, spentPoints);
            cachedData.put(mobId, computeBestiaryData(kills, spentPoints));
        }
    }

    private Map<ResourceLocation, Integer> loadSpecialBuffs(CompoundTag tag){
        Map<ResourceLocation, Integer> buffs = new HashMap<>();
        for(String key : tag.getAllKeys()){
            ResourceLocation buffId = ResourceLocation.tryParse(key);
            if(buffId != null) buffs.put(buffId, tag.getInt(key));
            else Bestia.LOGGER.warn("Invalid buff ID '{}', skipping.", key);
        }
        return buffs;
    }

    private CompoundTag convertToNewBestiaryTag(CompoundTag oldTag){
        Bestia.LOGGER.info("Converting old bestiary tag...");
        Bestia.LOGGER.debug("Old tag: {}", oldTag);

        CompoundTag newTag = new CompoundTag();
        ListTag entries = new ListTag();
        for(String key : oldTag.getAllKeys()){
            int kills = oldTag.getInt(key);
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("id", key);
            entryTag.putInt("kills", kills);
            entryTag.put("spent_points", new CompoundTag());
            entries.add(entryTag);
        }
        newTag.put("Entries", entries);
        newTag.putInt("Version", 2);

        Bestia.LOGGER.debug("New tag: {}", newTag);
        return newTag;
    }

    public void onKillNoSync(ResourceLocation mobId){
        int newKills = killCounts.getOrDefault(mobId, 0) + 1;
        killCounts.put(mobId, newKills);
        cachedData.put(mobId, computeBestiaryData(
                newKills, specialBuffPoints.getOrDefault(mobId, new HashMap<>())));
    }

    public void onKillWithSync(ServerPlayer player, ResourceLocation mobId){
        int newKills = killCounts.getOrDefault(mobId, 0) + 1;
        killCounts.put(mobId, newKills);
        cachedData.put(mobId, computeBestiaryData(
                newKills, specialBuffPoints.getOrDefault(mobId, new HashMap<>())));
        syncToPlayer(player);
    }

    public void setLevelAndSync(ServerPlayer player, ResourceLocation mobId, int level){
        int newKills = BestiaryData.totalNeededForLevel(level);
        killCounts.put(mobId, newKills);
        cachedData.put(mobId, computeBestiaryData(
                newKills, specialBuffPoints.getOrDefault(mobId, new HashMap<>())));
        syncToPlayer(player);
    }


    public void onSpendPointNoSync(ResourceLocation mobId, ResourceLocation specialBuff){
        int newPoints = specialBuffPoints
                .computeIfAbsent(mobId, id -> new HashMap<>())
                .getOrDefault(specialBuff, 0) + 1;
        if(specialBuffPoints.get(mobId).get(specialBuff) >= SpecialBuffRegistry.get(specialBuff).getMaxLevel()) return;
        specialBuffPoints.get(mobId).put(specialBuff, newPoints);
        cachedData.put(mobId, computeBestiaryData(killCounts.get(mobId), specialBuffPoints.get(mobId)));
    }

    public void onSpendPointWithSync(ServerPlayer player, ResourceLocation mobId, ResourceLocation specialBuff){
        int newPoints = specialBuffPoints
                .computeIfAbsent(mobId, id -> new HashMap<>())
                .getOrDefault(specialBuff, 0) + 1;
        if(specialBuffPoints.get(mobId).get(specialBuff) >= SpecialBuffRegistry.get(specialBuff).getMaxLevel()) return;
        specialBuffPoints.get(mobId).put(specialBuff, newPoints);
        cachedData.put(mobId, computeBestiaryData(killCounts.get(mobId), specialBuffPoints.get(mobId)));
        syncToPlayer(player);
    }

    public int getKillCount(ResourceLocation mobId){
        return killCounts.getOrDefault(mobId, 0);
    }

    public BestiaryData getData(ResourceLocation mobId){
        return cachedData.getOrDefault(mobId, computeBestiaryData(0, new HashMap<>()));
    }

    public Map<ResourceLocation, BestiaryData> getAllData(){
        return Collections.unmodifiableMap(cachedData);
    }

    public CompoundTag serializeNBT(){
        CompoundTag tag = new CompoundTag();
        ListTag entries = new ListTag();
        for(var entry : cachedData.entrySet()){
            CompoundTag entryTag = new CompoundTag();

            entryTag.putString("id", entry.getKey().toString());
            entryTag.putInt("kills", entry.getValue().kills());
            CompoundTag spentPointsTag = new CompoundTag();
            for(var buff : entry.getValue().spentPoints().entrySet()){
                spentPointsTag.putInt(buff.getKey().toString(), buff.getValue());
            }
            entryTag.put("spent_points", spentPointsTag);

            entries.add(entryTag);
        }

        tag.put("Entries", entries);
        tag.putInt("Version", 2);
        return tag;
    }

    private BestiaryData computeBestiaryData(int kills, Map<ResourceLocation, Integer> spentPoints){
        Pair<Integer, Integer> levelAndRemaining = computeLevelAndRemaining(kills);
        int level = levelAndRemaining.getA();
        int remaining = levelAndRemaining.getB();
        MobBuff mobBuff = computeMobBuff(level);

        int totalPoints = level / 10;
        int remainingPoints = totalPoints;
        for(var points : spentPoints.values()){
            remainingPoints -= points;
        }

        return new BestiaryData(kills, level, remaining, mobBuff, totalPoints, remainingPoints, spentPoints);
    }

    private Pair<Integer, Integer> computeLevelAndRemaining(int kills){
        int level = Mth.floor((-1 + Math.sqrt(1 + 4 * kills)) / 2.0);
        int remaining = (level + 1) * (level + 2) - kills;
        return new Pair<>(level, remaining);
    }

    public static int neededForLevel(int level){
        return 2 * level;
    }

    private MobBuff computeMobBuff(int level){
        float damageFactor = 1.0F + 0.05F * (float)level;
        float resistanceFactor = (float)Math.pow(0.95F, level);
        return new MobBuff(damageFactor, resistanceFactor);
    }

    public void syncToPlayer(ServerPlayer player){
        ModPackets.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new BestiarySyncPacket(this.getAllData())
        );
    }
}
