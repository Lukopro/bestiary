package net.luko.bestia.data;

public record BestiaryData(
        int kills,
        int level,
        int nextLevelKills,
        MobBuff mobBuff
) {}
