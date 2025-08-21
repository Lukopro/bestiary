package net.luko.bestia.util;

import net.luko.bestia.config.BestiaCommonConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.ForgeRegistries;

public class MobIdUtil {
    public static boolean validBestiaryMob(ResourceLocation mobId){
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(mobId);
        if(type == null) return false;
        if(BestiaCommonConfig.entityIsWhitelisted(mobId)) return true;

        MobCategory category = type.getCategory();
        if(category == MobCategory.MISC) return false;

        return !BestiaCommonConfig.entityIsBlacklisted(mobId);
    }
}
