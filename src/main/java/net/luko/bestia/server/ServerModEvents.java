package net.luko.bestia.server;

import net.luko.bestia.Bestia;
import net.luko.bestia.config.BestiaCommonConfig;
import net.luko.bestia.data.BestiaryManager;
import net.luko.bestia.data.buff.MobBuff;
import net.luko.bestia.data.PlayerBestiaryStore;
import net.luko.bestia.data.buff.special.SpecialBuffRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Bestia.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerModEvents {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;

        CompoundTag tag = player.getPersistentData().getCompound("Bestiary");
        BestiaryManager manager = new BestiaryManager();
        manager.loadFromNBT(tag);

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
        player.getPersistentData().put("Bestiary", tag);

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
                player.getPersistentData().put("Bestiary", tag);
            }
        }

        Bestia.LOGGER.info("Autosaved Bestiary data for {} player{}", playerList.size(), playerList.size() == 1 ? "" : "s");
    }

    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event){
        if(!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if(event.getEntity() instanceof Player) return;

        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(
                event.getEntity().getType());

        BestiaryManager manager = PlayerBestiaryStore.get(player);
        if(manager == null) return;

        manager.onKillWithSync(player, mobId);

        // Later on, do this less often
        player.getPersistentData().put("Bestiary", manager.serializeNBT());
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
