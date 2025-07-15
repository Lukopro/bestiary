package net.luko.bestiary.data;

public record BestiaryData(
        int kills,
        int level,
        int nextLevelKills,
        MobBuff mobBuff
) {}
