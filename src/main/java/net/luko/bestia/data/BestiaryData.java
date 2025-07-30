package net.luko.bestia.data;

import java.util.Map;

public record BestiaryData(
        int kills,
        int level,
        int remaining,
        MobBuff mobBuff,
        Map<String, Integer> spentPoints
) {}
