package net.luko.bestia.data.buff.special;

import net.luko.bestia.Bestia;
import net.luko.bestia.client.ClientConfigStore;
import net.luko.bestia.config.BestiaCommonConfig;
import net.luko.bestia.util.ResourceUtil;
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

        float executeBuff = BestiaCommonConfig.EXECUTE_BUFF_PER_LEVEL.get().floatValue();
        float lifestealBuff = BestiaCommonConfig.LIFESTEAL_BUFF_PER_LEVEL.get().floatValue();
        float reflexBuff = BestiaCommonConfig.REFLEX_BUFF_PER_LEVEL.get().floatValue();
        int dazeBuff = BestiaCommonConfig.DAZE_BUFF_PER_LEVEL.get();

        REROLL = register(new IntegerBuff(
                ResourceUtil.fromNamespaceAndPath(Bestia.MODID, "reroll"),
                0, 1, rerollMax));
        EXECUTE = register(new FloatBuff(
                ResourceUtil.fromNamespaceAndPath(Bestia.MODID, "execute"),
                0.0F, executeBuff, executeMax, true));
        LIFESTEAL = register(new FloatBuff(
                ResourceUtil.fromNamespaceAndPath(Bestia.MODID, "lifesteal"),
                0.0F, lifestealBuff, lifestealMax, true));
        REFLEX = register(new FloatBuff(
                ResourceUtil.fromNamespaceAndPath(Bestia.MODID, "reflex"),
                0.0F, reflexBuff, reflexMax, false));
        DAZE = register(new IntegerBuff(
                ResourceUtil.fromNamespaceAndPath(Bestia.MODID, "daze"),
                0, dazeBuff, dazeMax));
        HOLD = register(new BooleanBuff(
                ResourceUtil.fromNamespaceAndPath(Bestia.MODID, "hold")));
    }

    public static void reinitializeForClient(){
        final ClientConfigStore config = ClientConfigStore.INSTANCE;

        int rerollMax = config.rerollMaxLevel;
        int executeMax = config.executeMaxLevel;
        int lifestealMax = config.lifestealMaxLevel;
        int reflexMax = config.reflexMaxLevel;
        int dazeMax = config.dazeMaxLevel;

        float executeBuff = (float)config.executeBuffPerLevel;
        float lifestealBuff = (float)config.lifestealBuffPerLevel;
        float reflexBuff = (float)config.reflexBuffPerLevel;
        int dazeBuff = config.dazeBuffPerLevel;

        REROLL = register(new IntegerBuff(
                ResourceUtil.fromNamespaceAndPath(Bestia.MODID, "reroll"),
                0, 1, rerollMax));
        EXECUTE = register(new FloatBuff(
                ResourceUtil.fromNamespaceAndPath(Bestia.MODID, "execute"),
                0.0F, executeBuff, executeMax, true));
        LIFESTEAL = register(new FloatBuff(
                ResourceUtil.fromNamespaceAndPath(Bestia.MODID, "lifesteal"),
                0.0F, lifestealBuff, lifestealMax, true));
        REFLEX = register(new FloatBuff(
                ResourceUtil.fromNamespaceAndPath(Bestia.MODID, "reflex"),
                0.0F, reflexBuff, reflexMax, false));
        DAZE = register(new IntegerBuff(
                ResourceUtil.fromNamespaceAndPath(Bestia.MODID, "daze"),
                0, dazeBuff, dazeMax));
        HOLD = register(new BooleanBuff(
                ResourceUtil.fromNamespaceAndPath(Bestia.MODID, "hold")));
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
