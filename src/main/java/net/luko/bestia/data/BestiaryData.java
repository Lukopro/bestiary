package net.luko.bestia.data;

import net.luko.bestia.config.BestiaCommonConfig;
import net.luko.bestia.data.buff.MobBuff;
import net.luko.bestia.data.buff.special.SpecialBuffRegistry;
import net.luko.bestia.util.LevelFormula;
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
        return this.level == BestiaCommonConfig.MAX_LEVEL.get()
                ? 0
                : LevelFormula.getKills(this.level + 1) - LevelFormula.getKills(this.level);
    }

    public static int totalNeededForLevel(int level){
        return level * (level + 1);
    }

    public static BestiaryData compute(int kills, Map<ResourceLocation, Integer> spentPoints){
        int level = Math.min(LevelFormula.getLevel(kills), BestiaCommonConfig.MAX_LEVEL.get());
        int remainingKills = LevelFormula.getKills(level + 1) - kills;

        float perLevelDamage = BestiaCommonConfig.DAMAGE_FACTOR_PER_LEVEL.get().floatValue();
        float damageFactor = 1.0F + perLevelDamage * (float)level;
        float perLevelResistance = BestiaCommonConfig.RESISTANCE_FACTOR_PER_LEVEL.get().floatValue();
        float resistanceFactor = (float)Math.pow(perLevelResistance, level);
        MobBuff mobBuff = new MobBuff(damageFactor, resistanceFactor);

        int levelsPerPoint = BestiaCommonConfig.LEVELS_PER_SPECIAL_BUFF_POINT.get();
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

        for(var entry : spentPointsArg.entrySet()){
            int max = SpecialBuffRegistry.get(entry.getKey()).getMaxLevel();
            if(entry.getValue() > max){
                remainingPoints += entry.getValue() - max;
                spentPointsArg.put(entry.getKey(), max);
            }
        }

        return new BestiaryData(kills, level, remainingKills, mobBuff,
                totalPoints, remainingPoints, spentPointsArg);
    }
}
