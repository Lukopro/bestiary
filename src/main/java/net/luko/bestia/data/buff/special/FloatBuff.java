package net.luko.bestia.data.buff.special;

public class FloatBuff extends SpecialBuff<Float>{
    protected final float increment;

    public FloatBuff(String id, float defaultValue, float increment, int maxLevel) {
        super(id, defaultValue, maxLevel);
        this.increment = increment;
    }

    @Override
    public Float computeValue(int level) {
        return defaultValue + increment * (float)level;
    }
}
