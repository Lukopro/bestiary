package net.luko.bestia.server;

import net.luko.bestia.Bestia;
import net.luko.bestia.data.BestiaryManager;
import net.luko.bestia.data.PlayerBestiaryStore;
import net.luko.bestia.data.buff.special.SpecialBuffRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;

@Mod.EventBusSubscriber(modid = Bestia.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ApplySpecialBuffs {
    private static final ResourceLocation EXECUTE_SOURCE = ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "execute");
    private static final ResourceKey<Registry<DamageType>> DAMAGE_TYPE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("minecraft", "damage_type"));


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
            Holder<DamageType> holder = player.server.registryAccess()
                    .registryOrThrow(DAMAGE_TYPE_REGISTRY_KEY).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, EXECUTE_SOURCE));

            entity.hurt(new DamageSource(holder), entity.getMaxHealth());

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
    }

    @SubscribeEvent
    public static void applyReflexBuff(LivingHurtEvent event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        Entity source = event.getSource().getEntity();
        if(!(source instanceof LivingEntity attacker)) return;

        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(attacker.getType());

        BestiaryManager manager = PlayerBestiaryStore.get(player);
        if(manager == null) return;
        float force = manager.getSpecialBuffValue(SpecialBuffRegistry.REFLEX, mobId);

        if(force > 0) attacker.knockback(force, player.getX() - attacker.getX(), player.getZ() - attacker.getZ());
    }

    @SubscribeEvent
    public static void applyDazeBuff(LivingAttackEvent event){
        if(!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        LivingEntity target = event.getEntity();
        if(target.level().isClientSide) return;
        if(!(target instanceof Mob mob)) return;

        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType());
        BestiaryManager manager = PlayerBestiaryStore.get(player);
        if(manager == null) return;

        int ticks = manager.getSpecialBuffValue(SpecialBuffRegistry.DAZE, mobId);
        if(ticks > 0){
            MinecraftServer server = mob.getServer();
            if(server != null){
                mob.goalSelector.disableControlFlag(Goal.Flag.MOVE);
                mob.goalSelector.disableControlFlag(Goal.Flag.LOOK);
                mob.goalSelector.disableControlFlag(Goal.Flag.JUMP);
                mob.targetSelector.disableControlFlag(Goal.Flag.TARGET);
                mob.getNavigation().stop();
                mob.setTarget(null);
                mob.setLastHurtByMob(null);
                mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);

                mobAITickCounts.put(mob.getUUID(), ticks);
            }
        }
    }

    private static Map<UUID, Integer> mobAITickCounts = new HashMap<>();

    @SubscribeEvent
    public static void resetDazeApplications(TickEvent.ServerTickEvent event){
        if(event.phase != TickEvent.Phase.END) return;

        Map<UUID, Integer> newTickCounts = new HashMap<>();
        for(var entry : mobAITickCounts.entrySet()){
            Mob mob = null;
            for(ServerLevel level : ServerLifecycleHooks.getCurrentServer().getAllLevels()){
                Entity e = level.getEntity(entry.getKey());
                if(e instanceof Mob m){
                    mob = m;
                    break;
                }
            }
            if(mob != null && mob.isAlive()){
                if(entry.getValue() <= 0){
                    mob.goalSelector.enableControlFlag(Goal.Flag.MOVE);
                    mob.goalSelector.enableControlFlag(Goal.Flag.LOOK);
                    mob.goalSelector.enableControlFlag(Goal.Flag.JUMP);
                    mob.targetSelector.enableControlFlag(Goal.Flag.TARGET);
                } else {
                    mob.setTarget(null);
                    mob.getNavigation().stop();
                    newTickCounts.put(entry.getKey(), entry.getValue() - 1);
                }
            }
        }

        mobAITickCounts = newTickCounts;
    }

    private static Set<UUID> mobsToCancelKnockback = new HashSet<>();

    @SubscribeEvent
    public static void markToCancelKnockback(LivingHurtEvent event){
        if(!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        LivingEntity target = event.getEntity();
        if(target.level().isClientSide) return;

        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType());
        BestiaryManager manager = PlayerBestiaryStore.get(player);
        if(manager == null) return;

        if(manager.getSpecialBuffValue(SpecialBuffRegistry.HOLD, mobId)) mobsToCancelKnockback.add(target.getUUID());
    }

    @SubscribeEvent
    public static void applyHoldBuff(LivingKnockBackEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;

        if (mobsToCancelKnockback.contains(target.getUUID())) {
            event.setStrength(0);
            mobsToCancelKnockback.remove(target.getUUID());
        }
    }

    @SubscribeEvent
    public static void cleanupKnockbackList(TickEvent.ServerTickEvent event){
        if(event.phase != TickEvent.Phase.END) return;

        Iterator<UUID> iterator = mobsToCancelKnockback.iterator();
        while(iterator.hasNext()){
            UUID uuid = iterator.next();
            Entity entity = null;
            for(ServerLevel level : ServerLifecycleHooks.getCurrentServer().getAllLevels()){
                entity = level.getEntity(uuid);
                if(entity != null) break;
            }
            if(entity == null || !entity.isAlive()) iterator.remove();
        }
    }

}
