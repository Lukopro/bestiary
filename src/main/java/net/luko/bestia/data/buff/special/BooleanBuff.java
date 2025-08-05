package net.luko.bestia.data.buff.special;

import net.minecraft.resources.ResourceLocation;

public class BooleanBuff extends SpecialBuff<Boolean>{

    public BooleanBuff(ResourceLocation id){
        super(id, false, 1);
    }

    @Override
    public Boolean computeValue(int level) {
        return level > 0;
    }

    @Override
    public String getInfo(int level){
        return getBaseInfo(level);
    }
}