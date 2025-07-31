package net.luko.bestia.data.buff.special;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SpecialBuffRegistry {
    private static final Map<String, SpecialBuff<?>> BUFFS = new HashMap<>();

    public static final SpecialBuff<Integer> REROLL =
            register(new IntegerBuff("reroll", 0, 1, 3));
    public static final SpecialBuff<Float> EXECUTE =
            register(new FloatBuff("execute", 0.0F, 0.1F, 3));
    public static final SpecialBuff<Float> LIFESTEAL =
            register(new FloatBuff("lifesteal", 0.0F, 0.05F, 5));

    public static <T> SpecialBuff<T> register(SpecialBuff<T> buff){
        BUFFS.put(buff.getId(), buff);
        return buff;
    }

    public static SpecialBuff<?> get(String id){
        return BUFFS.get(id);
    }

    public static Collection<SpecialBuff<?>> all(){
        return BUFFS.values();
    }
}
