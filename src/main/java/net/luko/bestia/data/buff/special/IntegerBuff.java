package net.luko.bestia.data.buff.special;

public class IntegerBuff extends SpecialBuff<Integer>{
    protected final int increment;

    public IntegerBuff(String id, int base, int increment, int maxLevel){
        super(id, base, maxLevel);
        this.increment = increment;
    }

    @Override
    public Integer computeValue(int level) {
        return defaultValue + increment * level;
    }
}
