package net.luko.bestiary.server;

import net.luko.bestiary.Bestiary;
import net.luko.bestiary.data.BestiaryManager;
import net.luko.bestiary.data.MobBuff;
import net.luko.bestiary.data.PlayerBestiaryStore;
import net.luko.bestiary.network.ModPackets;
import net.luko.bestiary.network.SyncBestiaryPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Bestiary.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerModEvents {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;

        CompoundTag tag = player.getPersistentData().getCompound("Bestiary");
        BestiaryManager manager = new BestiaryManager();
        manager.loadFromNBT(tag);

        ModPackets.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncBestiaryPacket(manager.getAllData())
        );

        PlayerBestiaryStore.set(player, manager);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;

        CompoundTag tag = PlayerBestiaryStore.get(player).serializeNBT();
        player.getPersistentData().put("Bestiary", tag);

        PlayerBestiaryStore.remove(player);
    }

    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event){
        if(!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if(event.getEntity() instanceof Player) return;

        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(
                event.getEntity().getType());

        BestiaryManager manager = PlayerBestiaryStore.get(player);
        manager.onKill(mobId);

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
