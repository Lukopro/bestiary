package net.luko.bestia.data.buff.special;

import net.luko.bestia.Bestia;
import net.luko.bestia.config.BestiaCommonConfig;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SpecialBuffRegistry {
    private static final Map<ResourceLocation, SpecialBuff<?>> BUFFS = new HashMap<>();

    public static SpecialBuff<Integer> REROLL;
    public static SpecialBuff<Float> EXECUTE;
    public static SpecialBuff<Float> LIFESTEAL;

    public static void init(){
        int rerollMaxLevel = BestiaCommonConfig.REROLL_MAX_LEVEL.get();
        int executeMaxLevel = BestiaCommonConfig.EXECUTE_MAX_LEVEL.get();
        int lifestealMaxLevel = BestiaCommonConfig.LIFESTEAL_MAX_LEVEL.get();

        REROLL = register(new IntegerBuff(
                ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "reroll"),
                0, 1, rerollMaxLevel));
        EXECUTE = register(new FloatBuff(
                ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "execute"),
                0.0F, BestiaCommonConfig.EXECUTE_BUFF_PER_LEVEL.get().floatValue(), executeMaxLevel, true));
        LIFESTEAL = register(new FloatBuff(
                ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "lifesteal"),
                0.0F, BestiaCommonConfig.LIFESTEAL_BUFF_PER_LEVEL.get().floatValue(), lifestealMaxLevel, true));
    }

    public static <T> SpecialBuff<T> register(SpecialBuff<T> buff){
        BUFFS.put(buff.getId(), buff);
        return buff;
    }

    public static SpecialBuff<?> get(ResourceLocation id){
        return BUFFS.get(id);
    }

    public static Collection<SpecialBuff<?>> all(){
        return BUFFS.values();
    }
}
