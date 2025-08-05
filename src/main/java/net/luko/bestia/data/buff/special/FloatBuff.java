package net.luko.bestia.data.buff.special;

import net.minecraft.resources.ResourceLocation;

public class FloatBuff extends SpecialBuff<Float>{
    protected final float increment;

    public FloatBuff(ResourceLocation id, float defaultValue, float increment, int maxLevel) {
        super(id, defaultValue, maxLevel);
        this.increment = increment;
    }

    @Override
    public Float computeValue(int level) {
        return defaultValue + increment * (float)level;
    }

    @Override
    public String getInfo(int level){
        String baseInfo = getBaseInfo(level);
        if(level == 0) return baseInfo;
        return String.format(baseInfo, computeValue(level));
    }
}
