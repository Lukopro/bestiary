package net.luko.bestia.server;

import net.luko.bestia.Bestia;
import net.luko.bestia.data.BestiaryManager;
import net.luko.bestia.data.buff.MobBuff;
import net.luko.bestia.data.PlayerBestiaryStore;
import net.luko.bestia.data.buff.special.SpecialBuffRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Bestia.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerModEvents {
    private static final ResourceLocation EXECUTE_SOURCE = ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "execute");
    private static final ResourceKey<Registry<DamageType>> DAMAGE_TYPE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("minecraft", "damage_type"));

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

    @SubscribeEvent
    public static void applyRerollBuff(LivingDropsEvent event){
        if(!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        if(level.isClientSide) return;

        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        BestiaryManager manager = PlayerBestiaryStore.get(player);
        if(manager == null) return;
        int rerolls = manager.getSpecialBuffValue(SpecialBuffRegistry.REROLL, mobId);
        if(rerolls <= 0) return;

        MinecraftServer server = level.getServer();
        if(server == null) return;
        LootTable lootTable = server.getLootData().getLootTable(entity.getLootTable());
        if(lootTable == LootTable.EMPTY) return;

        List<ItemStack> extraLoot = new ArrayList<>();

        LootParams lootParams = new LootParams.Builder((ServerLevel) level)
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, event.getSource())
                .withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                .withOptionalParameter(LootContextParams.KILLER_ENTITY, player)
                .create(LootContextParamSets.ENTITY);

        for(int i = 0; i < rerolls; i++){
            extraLoot.addAll(lootTable.getRandomItems(lootParams));
        }

        for(ItemStack stack : extraLoot){
            ItemEntity itemEntity = new ItemEntity(level, entity.getX(), entity.getEyeY(), entity.getZ(), stack);
            event.getDrops().add(itemEntity);
        }
    }

    @SubscribeEvent
    public static void applyExecuteBuff(LivingHurtEvent event){
        if(!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        LivingEntity entity = event.getEntity();
        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());

        BestiaryManager manager = PlayerBestiaryStore.get(player);
        if(manager == null) return;
        float threshold = manager.getSpecialBuffValue(SpecialBuffRegistry.EXECUTE, mobId);
        if(threshold <= 0F) return;

        if(entity.getHealth() <= entity.getMaxHealth() * threshold){
            DamageType type = player.server.registryAccess()
                    .registryOrThrow(DAMAGE_TYPE_REGISTRY_KEY).get(EXECUTE_SOURCE);
            if(type == null) return;
            entity.hurt(new DamageSource(Holder.direct(type)), entity.getMaxHealth());

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void applyLifestealBuff(LivingDamageEvent event){
        if(!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType());

        BestiaryManager manager = PlayerBestiaryStore.get(player);
        if(manager == null) return;
        float lifesteal = manager.getSpecialBuffValue(SpecialBuffRegistry.LIFESTEAL, mobId);
        if(lifesteal <= 0F) return;

        player.heal(event.getAmount() * lifesteal);
        Bestia.LOGGER.info("{}, {}, {}", event.getAmount(), lifesteal,event.getAmount() * lifesteal );
    }

}
