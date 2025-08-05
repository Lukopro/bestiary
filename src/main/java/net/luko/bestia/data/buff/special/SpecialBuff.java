package net.luko.bestia.data.buff.special;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class SpecialBuff<T> {
    protected final ResourceLocation id;
    protected final T defaultValue;
    protected final int maxLevel;

    public SpecialBuff(ResourceLocation id, T defaultValue, int maxLevel){
        this.id = id;
        this.defaultValue = defaultValue;
        this.maxLevel = maxLevel;
    }

    public ResourceLocation getId(){
        return id;
    }

    public T getDefaultValue(){
        return defaultValue;
    }

    public int getMaxLevel(){
        return maxLevel;
    }

    public abstract T computeValue(int level);

    protected String getBaseInfo(int level){
        return level == 0
                ? Component.translatable("buff.special." + id.getNamespace() + "." + id.getPath() + ".info.unleveled").getString()
                : Component.translatable("buff.special." + id.getNamespace() + "." + id.getPath() + ".info").getString();
    }

    public abstract String getInfo(int level);

    public String getDisplayName(){
        return Component.translatable("buff.special." + id.getNamespace() + "." + id.getPath()).getString();
    }
}
