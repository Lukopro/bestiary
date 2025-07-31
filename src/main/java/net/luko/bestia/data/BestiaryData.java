package net.luko.bestia.data;

import net.luko.bestia.data.buff.MobBuff;

import java.util.Map;

public record BestiaryData(
        int kills,
        int level,
        int remainingKills,
        MobBuff mobBuff,
        int totalPoints,
        int remainingPoints,
        Map<String, Integer> spentPoints
) {}
