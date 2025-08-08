package net.luko.bestia.data;

import net.luko.bestia.Bestia;
import net.luko.bestia.config.BestiaConfig;
import net.luko.bestia.data.buff.special.SpecialBuff;
import net.luko.bestia.data.buff.special.SpecialBuffRegistry;
import net.luko.bestia.network.ModPackets;
import net.luko.bestia.network.BestiarySyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BestiaryManager {
    private final Map<ResourceLocation, Integer> killCounts = new HashMap<>();
    private final Map<ResourceLocation, BestiaryData> cachedData = new HashMap<>();
    private final Map<ResourceLocation, Map<ResourceLocation, Integer>> spentPoints = new HashMap<>();

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
            Map<ResourceLocation, Integer> loadedSpentPoints = loadSpecialBuffs(tag.getCompound("spent_points"));
            this.killCounts.put(mobId, kills);
            BestiaryData newData = BestiaryData.compute(kills, loadedSpentPoints);
            this.cachedData.put(mobId, newData);
            this.spentPoints.put(mobId, newData.spentPoints());

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

    public void onKillWithSync(ServerPlayer player, ResourceLocation mobId){
        addKillsAndSync(player, mobId, 1);
    }

    public void setLevelAndSync(ServerPlayer player, ResourceLocation mobId, int level){
        int newKills = BestiaryData.totalNeededForLevel(level);
        this.setKillsAndSync(player, mobId, newKills);
    }

    public void addLevelsAndSync(ServerPlayer player, ResourceLocation mobId, int levels){
        int newKills = BestiaryData.totalNeededForLevel(cachedData.get(mobId).level() + levels);
        this.setKillsAndSync(player, mobId, newKills);
    }

    public void setKillsAndSync(ServerPlayer player, ResourceLocation mobId, int kills){
        this.killCounts.put(mobId, kills);
        BestiaryData newData = BestiaryData.compute(kills, this.spentPoints.getOrDefault(mobId, new HashMap<>()));
        this.cachedData.put(mobId, newData);
        this.spentPoints.put(mobId, newData.spentPoints());
        syncToPlayer(player);
    }

    public void addKillsAndSync(ServerPlayer player, ResourceLocation mobId, int kills){
        int newKills = this.killCounts.getOrDefault(mobId, 0) + kills;
        this.setKillsAndSync(player, mobId, newKills);
    }

    public void onSpendPointWithSync(ServerPlayer player, ResourceLocation mobId, ResourceLocation specialBuff){
        int newPoints = this.spentPoints
                .computeIfAbsent(mobId, id -> new HashMap<>())
                .getOrDefault(specialBuff, 0) + 1;
        if(getSpecialBuffLevel(SpecialBuffRegistry.get(specialBuff), mobId) >= SpecialBuffRegistry.get(specialBuff).getMaxLevel()){
            Bestia.LOGGER.warn("Client attempted to spend buff point, but buff is maxed.");
            return;
        }
        if(cachedData.get(mobId).remainingPoints() <= 0){
            Bestia.LOGGER.warn("Client attempted to spend buff point, but no points are available.");
            return;
        }
        this.spentPoints.get(mobId).put(specialBuff, newPoints);
        this.cachedData.put(mobId, BestiaryData.compute(this.killCounts.get(mobId), this.spentPoints.get(mobId)));
        syncToPlayer(player);
    }

    public void onClearPointsWithSync(ServerPlayer player, ResourceLocation mobId){
        var points = this.spentPoints.get(mobId);
        if(points != null) points.clear();
        if(this.cachedData.containsKey(mobId)) this.cachedData.put(mobId, BestiaryData.compute(this.killCounts.get(mobId), this.spentPoints.get(mobId)));
        syncToPlayer(player);
    }

    public int getKillCount(ResourceLocation mobId){
        return this.killCounts.getOrDefault(mobId, 0);
    }

    public BestiaryData getData(ResourceLocation mobId){
        return this.cachedData.getOrDefault(mobId, BestiaryData.compute(0, new HashMap<>()));
    }

    public Map<ResourceLocation, BestiaryData> getAllData(){
        return Collections.unmodifiableMap(this.cachedData);
    }

    public CompoundTag serializeNBT(){
        CompoundTag tag = new CompoundTag();
        ListTag entries = new ListTag();
        for(var entry : this.cachedData.entrySet()){
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

    public void syncToPlayer(ServerPlayer player){
        ModPackets.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new BestiarySyncPacket(this.getAllData())
        );
    }

    public int getSpecialBuffLevel(SpecialBuff<?> buff, ResourceLocation mobId){
        return BestiaConfig.ENABLE_SPECIAL_BUFFS.get()
                ? this.spentPoints.getOrDefault(mobId, new HashMap<>()).getOrDefault(buff.getId(), 0)
                : 0;
    }

    public <T> T getSpecialBuffValue(SpecialBuff<T> buff, ResourceLocation mobId){
        return buff.computeValue(getSpecialBuffLevel(buff, mobId));
    }
}
