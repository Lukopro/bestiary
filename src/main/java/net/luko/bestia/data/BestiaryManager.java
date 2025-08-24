package net.luko.bestia.data;

import net.luko.bestia.Bestia;
import net.luko.bestia.config.BestiaClientConfig;
import net.luko.bestia.config.BestiaCommonConfig;
import net.luko.bestia.data.buff.special.SpecialBuff;
import net.luko.bestia.data.buff.special.SpecialBuffRegistry;
import net.luko.bestia.network.MobLevelUpToastPacket;
import net.luko.bestia.network.ModPackets;
import net.luko.bestia.network.BestiarySyncPacket;
import net.luko.bestia.util.MobIdUtil;
import net.minecraft.core.registries.BuiltInRegistries;
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
    private String playerName;

    public void loadFromNBT(CompoundTag bestiaryTag, String playerName){
        if(bestiaryTag.contains(BestiaryKey.VERSION.get()) || bestiaryTag.isEmpty()){
            loadEntriesNBT(bestiaryTag.getList(BestiaryKey.ENTRIES.get(), Tag.TAG_COMPOUND));
        } else {
            loadEntriesNBT(convertFromVersion1To2(bestiaryTag).getList(BestiaryKey.ENTRIES.get(), Tag.TAG_COMPOUND));
        }
        this.playerName = playerName;
    }

    private void loadEntriesNBT(ListTag entriesTag){
        for(Tag t : entriesTag){
            CompoundTag tag = (CompoundTag) t;
            ResourceLocation mobId = ResourceLocation.tryParse(tag.getString(BestiaryKey.Entry.ID.get()));
            if(mobId == null) {
                Bestia.LOGGER.warn("{} was not parsed correctly. Data for this mob will not persist.",
                        tag.getString(BestiaryKey.Entry.ID.get()));
                continue;
            }
            int kills = tag.getInt(BestiaryKey.Entry.KILLS.get());
            Map<ResourceLocation, Integer> loadedSpentPoints = loadSpecialBuffs(tag.getCompound(BestiaryKey.Entry.SPENT_POINTS.get()));
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

    private CompoundTag convertFromVersion1To2(CompoundTag oldTag){
        Bestia.LOGGER.info("Converting old bestiary tag...");
        Bestia.LOGGER.debug("Old tag: {}", oldTag);

        CompoundTag newTag = new CompoundTag();
        ListTag entries = new ListTag();
        for(String key : oldTag.getAllKeys()){
            int kills = oldTag.getInt(key);
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString(BestiaryKey.Entry.ID.get(), key);
            entryTag.putInt(BestiaryKey.Entry.KILLS.get(), kills);
            entryTag.put(BestiaryKey.Entry.SPENT_POINTS.get(), new CompoundTag());
            entries.add(entryTag);
        }
        newTag.put(BestiaryKey.ENTRIES.get(), entries);
        newTag.putInt(BestiaryKey.VERSION.get(), 2);

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
        if(!MobIdUtil.validBestiaryMob(mobId)){
            Bestia.LOGGER.warn("Attempted to modify Bestiary for invalid mob ID: {}", mobId);
            return;
        }

        BestiaryData newData = BestiaryData.compute(kills, this.spentPoints.getOrDefault(mobId, new HashMap<>()));
        int oldLevel = this.getData(mobId).level();

        this.killCounts.put(mobId, kills);
        this.cachedData.put(mobId, newData);
        this.spentPoints.put(mobId, newData.spentPoints());

        syncToPlayer(player);
        if(newData.level() != oldLevel) sendToast(player, mobId);
    }

    public void addKillsAndSync(ServerPlayer player, ResourceLocation mobId, int kills){
        int newKills = this.killCounts.getOrDefault(mobId, 0) + kills;
        this.setKillsAndSync(player, mobId, newKills);
    }

    public void onSpendPointWithSync(ServerPlayer player, ResourceLocation mobId, ResourceLocation specialBuff){
        if(getSpecialBuffLevel(SpecialBuffRegistry.get(specialBuff), mobId) >= SpecialBuffRegistry.get(specialBuff).getMaxLevel()){
            Bestia.LOGGER.warn("Client attempted to spend buff point, but buff is maxed.");
            return;
        }
        if(cachedData.get(mobId).remainingPoints() <= 0){
            Bestia.LOGGER.warn("Client attempted to spend buff point, but no points are available.");
            return;
        }

        int newPoints = this.spentPoints
                .computeIfAbsent(mobId, id -> new HashMap<>())
                .getOrDefault(specialBuff, 0) + 1;

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
        return MobIdUtil.validBestiaryMob(mobId)
                ? this.killCounts.getOrDefault(mobId, 0)
                : 0;
    }

    public BestiaryData getData(ResourceLocation mobId){
        return MobIdUtil.validBestiaryMob(mobId)
                ? this.cachedData.getOrDefault(mobId, BestiaryData.compute(0, new HashMap<>()))
                : BestiaryData.compute(0, new HashMap<>());
    }

    public Map<ResourceLocation, BestiaryData> getAllData(){
        return Collections.unmodifiableMap(this.cachedData);
    }

    public CompoundTag serializeNBT(){
        CompoundTag tag = new CompoundTag();
        ListTag entries = new ListTag();
        for(var entry : this.cachedData.entrySet()){
            CompoundTag entryTag = new CompoundTag();

            entryTag.putString(BestiaryKey.Entry.ID.get(), entry.getKey().toString());
            entryTag.putInt(BestiaryKey.Entry.KILLS.get(), entry.getValue().kills());
            CompoundTag spentPointsTag = new CompoundTag();
            for(var buff : entry.getValue().spentPoints().entrySet()){
                spentPointsTag.putInt(buff.getKey().toString(), buff.getValue());
            }
            entryTag.put(BestiaryKey.Entry.SPENT_POINTS.get(), spentPointsTag);

            entries.add(entryTag);
        }

        tag.putString(BestiaryKey.PLAYER_NAME.get(), this.playerName);
        tag.put(BestiaryKey.ENTRIES.get(), entries);
        tag.putInt(BestiaryKey.VERSION.get(), 3);
        return tag;
    }

    public void syncToPlayer(ServerPlayer player){
        ModPackets.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new BestiarySyncPacket(this.getAllData())
        );
    }

    public void sendToast(ServerPlayer player, ResourceLocation mobId){
        if(BestiaClientConfig.SHOW_LEVEL_UP_TOASTS.get()) ModPackets.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new MobLevelUpToastPacket(mobId)
        );
    }

    public int getSpecialBuffLevel(SpecialBuff<?> buff, ResourceLocation mobId){
        return BestiaCommonConfig.ENABLE_SPECIAL_BUFFS.get() && MobIdUtil.validBestiaryMob(mobId)
                ? this.spentPoints.getOrDefault(mobId, new HashMap<>()).getOrDefault(buff.getId(), 0)
                : 0;
    }

    public <T> T getSpecialBuffValue(SpecialBuff<T> buff, ResourceLocation mobId){
        return buff.computeValue(getSpecialBuffLevel(buff, mobId));
    }

    public String getPlayerName() {
        return this.playerName;
    }
}
