package net.luko.bestia.util;

import net.luko.bestia.client.ClientConfigStore;
import net.luko.bestia.config.BestiaCommonConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;


public class MobIdUtil {
    public static boolean validBestiaryMob(ResourceLocation mobId, LogicalSide side){
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(mobId);
        if(type == null) return false;
        if(side.isServer() && BestiaCommonConfig.entityIsWhitelisted(mobId)) return true;
        if(side.isClient() && ClientConfigStore.INSTANCE.entityIsWhitelisted(mobId)) return true;

        MobCategory category = type.getCategory();
        if(category == MobCategory.MISC) return false;
        
        return side.isClient()
                ? !ClientConfigStore.INSTANCE.entityIsBlacklisted(mobId)
                : !BestiaCommonConfig.entityIsBlacklisted(mobId);
    }
}
