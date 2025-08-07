package net.luko.bestia.data.buff.special;

import net.luko.bestia.Bestia;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SpecialBuffRegistry {
    private static final Map<ResourceLocation, SpecialBuff<?>> BUFFS = new HashMap<>();

    public static final SpecialBuff<Integer> REROLL =
            register(new IntegerBuff(ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "reroll"), 0, 1, 3));
    public static final SpecialBuff<Float> EXECUTE =
            register(new FloatBuff(ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "execute"), 0.0F, 0.1F, 3, true));
    public static final SpecialBuff<Float> LIFESTEAL =
            register(new FloatBuff(ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "lifesteal"), 0.0F, 0.05F, 5, true));

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
