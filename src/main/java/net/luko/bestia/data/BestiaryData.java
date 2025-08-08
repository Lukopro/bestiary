package net.luko.bestia.data;

import net.luko.bestia.config.BestiaConfig;
import net.luko.bestia.data.buff.MobBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.HashMap;
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
    @Override
    public int remainingPoints(){
        return remainingPoints;
    }

    public int neededForNextLevel(){
        return (level + 1) * 2;
    }

    public static int totalNeededForLevel(int level){
        return level * (level + 1);
    }

    public static BestiaryData compute(int kills, Map<ResourceLocation, Integer> spentPoints){
        int level = Mth.floor((-1 + Math.sqrt(1 + 4 * kills)) / 2.0);
        int remainingKills = (level + 1) * (level + 2) - kills;

        float perLevelDamage = BestiaConfig.DAMAGE_FACTOR_PER_LEVEL.get().floatValue();
        float damageFactor = 1.0F + perLevelDamage * (float)level;
        float perLevelResistance = BestiaConfig.RESISTANCE_FACTOR_PER_LEVEL.get().floatValue();
        float resistanceFactor = (float)Math.pow(perLevelResistance, level);
        MobBuff mobBuff = new MobBuff(damageFactor, resistanceFactor);

        int levelsPerPoint = BestiaConfig.LEVELS_PER_SPECIAL_BUFF_POINT.get();
        int totalPoints = level / levelsPerPoint;
        int remainingPoints = totalPoints;
        for(var points : spentPoints.values()){
            remainingPoints -= points;
        }

        Map<ResourceLocation, Integer> spentPointsArg = spentPoints;
        if(remainingPoints < 0){
            spentPointsArg = new HashMap<>();
            remainingPoints = totalPoints;
        }

        return new BestiaryData(kills, level, remainingKills, mobBuff,
                totalPoints, remainingPoints, spentPointsArg);
    }
}
