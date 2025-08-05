package net.luko.bestia.data.buff.special;

import net.minecraft.resources.ResourceLocation;

public class IntegerBuff extends SpecialBuff<Integer>{
    protected final int increment;

    public IntegerBuff(ResourceLocation id, int base, int increment, int maxLevel){
        super(id, base, maxLevel);
        this.increment = increment;
    }

    @Override
    public Integer computeValue(int level) {
        return defaultValue + increment * level;
    }

    @Override
    public String getInfo(int level){
        String baseInfo = getBaseInfo(level);
        if(level == 0) return baseInfo;
        return String.format(baseInfo, computeValue(level));
    }
}
