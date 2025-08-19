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
    public static SpecialBuff<Float> REFLEX;
    public static SpecialBuff<Integer> DAZE;
    public static SpecialBuff<Boolean> HOLD;

    public static void init(){
        int rerollMax = BestiaCommonConfig.REROLL_MAX_LEVEL.get();
        int executeMax = BestiaCommonConfig.EXECUTE_MAX_LEVEL.get();
        int lifestealMax = BestiaCommonConfig.LIFESTEAL_MAX_LEVEL.get();
        int reflexMax = BestiaCommonConfig.REFLEX_MAX_LEVEL.get();
        int dazeMax = BestiaCommonConfig.DAZE_MAX_LEVEL.get();

        REROLL = register(new IntegerBuff(
                ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "reroll"),
                0, 1, rerollMax));
        EXECUTE = register(new FloatBuff(
                ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "execute"),
                0.0F, BestiaCommonConfig.EXECUTE_BUFF_PER_LEVEL.get().floatValue(), executeMax, true));
        LIFESTEAL = register(new FloatBuff(
                ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "lifesteal"),
                0.0F, BestiaCommonConfig.LIFESTEAL_BUFF_PER_LEVEL.get().floatValue(), lifestealMax, true));
        REFLEX = register(new FloatBuff(
                ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "reflex"),
                0.0F, BestiaCommonConfig.REFLEX_BUFF_PER_LEVEL.get().floatValue(), reflexMax, false));
        DAZE = register(new IntegerBuff(
                ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "daze"),
                0, BestiaCommonConfig.DAZE_BUFF_PER_LEVEL.get(), dazeMax));
        HOLD = register(new BooleanBuff(
                ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "hold")));
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
