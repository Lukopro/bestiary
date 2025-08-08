package net.luko.bestia.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class BestiaConfig {
    public static final ForgeConfigSpec COMMON_CONFIG;

    public static ForgeConfigSpec.BooleanValue ENABLE_SPECIAL_BUFFS;

    public static ForgeConfigSpec.DoubleValue DAMAGE_FACTOR_PER_LEVEL;
    public static ForgeConfigSpec.DoubleValue RESISTANCE_FACTOR_PER_LEVEL;

    public static ForgeConfigSpec.IntValue LEVELS_PER_SPECIAL_BUFF_POINT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Features");

        ENABLE_SPECIAL_BUFFS = builder
                .comment("View, apply, and use special buffs. This should not interfere with any data generated already.")
                .define("enableSpecialBuffs", true);

        builder.pop();

        builder.push("Buffs");

        DAMAGE_FACTOR_PER_LEVEL = builder
                .comment("For each level, how much of the original damage should be added on attack (additive)? (default: 0.05)")
                .defineInRange("damageFactorPerLevel", 0.05, 0.0, 1.0);

        RESISTANCE_FACTOR_PER_LEVEL = builder
                .comment("For each level, by what factor should incoming damage be multiplied by (multiplicative)? (default: 0.96)")
                .defineInRange("resistanceFactorPerLevel", 0.96, 0.1, 1.0);

        LEVELS_PER_SPECIAL_BUFF_POINT = builder
                .comment("How many levels until a special buff point is earned? (default: 10)")
                .defineInRange("levelsPerSpecialPoint", 10, 1, 100);

        builder.pop();

        COMMON_CONFIG = builder.build();
    }
}
