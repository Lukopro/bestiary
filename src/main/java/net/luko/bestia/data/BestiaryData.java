package net.luko.bestia.data;

import net.luko.bestia.data.buff.MobBuff;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record BestiaryData(
        int kills,
        int level,
        int remainingKills,
        MobBuff mobBuff,
        int totalPoints,
        int remainingPoints,
        Map<ResourceLocation, Integer> spentPoints
) {
    public int neededForNextLevel(){
        return (level + 1) * 2;
    }

    public static int totalNeededForLevel(int level){
        return level * (level + 1);
    }
}
