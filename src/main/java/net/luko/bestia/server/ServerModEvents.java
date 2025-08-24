package net.luko.bestia.server;

import net.luko.bestia.Bestia;
import net.luko.bestia.config.BestiaCommonConfig;
import net.luko.bestia.data.BestiaryKey;
import net.luko.bestia.data.BestiaryManager;
import net.luko.bestia.data.buff.MobBuff;
import net.luko.bestia.data.PlayerBestiaryStore;
import net.luko.bestia.util.MobIdUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.eventlog.EventLogDirectory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Bestia.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerModEvents {

    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event){
        MinecraftServer server = event.getServer();

        BestiaryOfflineCache.clear();

        File playerDataFolder = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).toFile();
        if(!playerDataFolder.exists() || !playerDataFolder.isDirectory()){
            Bestia.LOGGER.warn("Player data folder not found: {}", playerDataFolder.getAbsolutePath());
            return;
        }

        File[] files = playerDataFolder.listFiles((dir, name) -> name.endsWith(".dat"));
        if(files == null) {
            Bestia.LOGGER.warn("Files is null? Strange error");
            return;
        }
        Bestia.LOGGER.debug("Attempting to cache bestiary data from {} playerdata files", files.length);

        for(File file : files){
            try {
                CompoundTag playerTag = NbtIo.readCompressed(file);
                CompoundTag forgeData = playerTag.getCompound("ForgeData");

                if(forgeData.contains(BestiaryKey.ROOT.get())){
                    CompoundTag bestiaryTag = forgeData.getCompound(BestiaryKey.ROOT.get());
                    UUID uuid = UUID.fromString(file.getName().replace(".dat", ""));
                    BestiaryOfflineCache.put(uuid, bestiaryTag);
                }
            } catch (IOException | RuntimeException e){
                Bestia.LOGGER.error("Failed to load bestiary data from {} " +
                        "(you can probably ignore this if you are in singleplayer)", file.getName(), e);
            }
        }

        Bestia.LOGGER.info("Loaded {} offline player bestiary entries", BestiaryOfflineCache.size());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        UUID uuid = player.getUUID();

        CompoundTag bestiaryTag = BestiaryOfflineCache.get(uuid);
        if(bestiaryTag != null) {
            Bestia.LOGGER.debug("Found offline cache for player {}", uuid);
            BestiaryOfflineCache.remove(uuid);
        }
        else {
            Bestia.LOGGER.debug("Could not find offline cache for player {}, falling back", uuid);
            bestiaryTag = player.getPersistentData().getCompound(BestiaryKey.ROOT.get());
        }


        BestiaryManager manager = new BestiaryManager();
        manager.loadFromNBT(bestiaryTag, event.getEntity().getName().getString());

        manager.syncToPlayer(player);

        PlayerBestiaryStore.set(player, manager);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;

        BestiaryManager manager = PlayerBestiaryStore.get(player);
        if(manager == null){
            Bestia.LOGGER.warn("Bestiary manager for player {} was null", player);
            return;
        }

        CompoundTag tag = manager.serializeNBT();
        player.getPersistentData().put(BestiaryKey.ROOT.get(), tag);
        BestiaryOfflineCache.put(player.getUUID(), tag);

        PlayerBestiaryStore.remove(player);
    }

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event){
        if(event.phase != TickEvent.Phase.END) return;

        tickCounter++;
        if(tickCounter >= BestiaCommonConfig.AUTOSAVE_INTERVAL.get()){
            tickCounter = 0;
            autosaveAllPlayers();
        }
    }

    private static void autosaveAllPlayers(){
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server == null) return;

        List<ServerPlayer> playerList = server.getPlayerList().getPlayers();
        for(ServerPlayer player : playerList){
            BestiaryManager manager = PlayerBestiaryStore.get(player);
            if(manager != null){
                CompoundTag tag = manager.serializeNBT();
                player.getPersistentData().put(BestiaryKey.ROOT.get(), tag);
            }
        }

        Bestia.LOGGER.info("Autosaved Bestiary data for {} player{}", playerList.size(), playerList.size() == 1 ? "" : "s");
    }

    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event){
        if(!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if(event.getEntity() instanceof Player) return;

        ResourceLocation mobId = ForgeRegistries.ENTITY_TYPES.getKey(
                event.getEntity().getType());

        if(!MobIdUtil.validBestiaryMob(mobId)) return;

        BestiaryManager manager = PlayerBestiaryStore.get(player);
        if(manager == null) return;

        manager.onKillWithSync(player, mobId);

        // Later on, do this less often
        player.getPersistentData().put(BestiaryKey.ROOT.get(), manager.serializeNBT());
    }

    @SubscribeEvent
    public static void onPlayerAttacksMob(LivingHurtEvent event){
        if(!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        Entity target = event.getEntity();

        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        BestiaryManager manager = PlayerBestiaryStore.get(player);
        MobBuff buff = manager.getData(mobId).mobBuff();

        float modifier = buff.damageFactor();
        event.setAmount(event.getAmount() * modifier);
    }

    @SubscribeEvent
    public static void onPlayerDamaged(LivingHurtEvent event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        Entity source = event.getSource().getEntity();
        if(!(source instanceof LivingEntity attacker)) return;

        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(attacker.getType());
        BestiaryManager manager = PlayerBestiaryStore.get(player);
        MobBuff buff = manager.getData(mobId).mobBuff();

        float modifier = buff.resistanceFactor();
        event.setAmount(event.getAmount() * modifier);
    }

}
