package net.luko.bestia.data.buff.special;

public abstract class SpecialBuff<T> {
    protected final String id;
    protected final T defaultValue;
    protected final int maxLevel;

    public SpecialBuff(String id, T defaultValue, int maxLevel){
        this.id = id;
        this.defaultValue = defaultValue;
        this.maxLevel = maxLevel;
    }

    public String getId(){
        return id;
    }

    public T getDefaultValue(){
        return defaultValue;
    }

    public int getMaxLevel(){
        return maxLevel;
    }

    public abstract T computeValue(int level);
}
