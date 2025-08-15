package net.luko.bestia.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class BestiaCommonConfig {
    public static final ForgeConfigSpec COMMON_CONFIG;

    public static ForgeConfigSpec.BooleanValue ENABLE_SPECIAL_BUFFS;

    public static ForgeConfigSpec.DoubleValue DAMAGE_FACTOR_PER_LEVEL;
    public static ForgeConfigSpec.DoubleValue RESISTANCE_FACTOR_PER_LEVEL;

    public static ForgeConfigSpec.IntValue LEVELS_PER_SPECIAL_BUFF_POINT;

    public static ForgeConfigSpec.IntValue REROLL_MAX_LEVEL;
    public static ForgeConfigSpec.IntValue EXECUTE_MAX_LEVEL;
    public static ForgeConfigSpec.IntValue LIFESTEAL_MAX_LEVEL;

    public static ForgeConfigSpec.DoubleValue EXECUTE_BUFF_PER_LEVEL;
    public static ForgeConfigSpec.DoubleValue LIFESTEAL_BUFF_PER_LEVEL;

    public static ForgeConfigSpec.IntValue MAX_LEVEL;
    public static ForgeConfigSpec.ConfigValue<String> KILLS_FORMULA;
    public static ForgeConfigSpec.BooleanValue MONOTONY_CHECK;

    public static ForgeConfigSpec.IntValue AUTOSAVE_INTERVAL;

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

        REROLL_MAX_LEVEL = builder
                .comment("How many levels can you apply to special buff 'reroll'? (default: 3, 0 to disable)")
                .defineInRange("rerollMaxLevel", 3, 0, Integer.MAX_VALUE);

        EXECUTE_MAX_LEVEL = builder
                .comment("How many levels can you apply to special buff 'execute'? (default: 3, 0 to disable)")
                .defineInRange("executeMaxLevel", 3, 0, Integer.MAX_VALUE);

        LIFESTEAL_MAX_LEVEL = builder
                .comment("How many levels can you apply to special buff 'lifesteal'? (default: 5, 0 to disable)")
                .defineInRange("lifestealMaxLevel", 5, 0, Integer.MAX_VALUE);

        EXECUTE_BUFF_PER_LEVEL = builder
                .comment("How much should be added to the execute buff's threshold per level? (default: 0.1)")
                .defineInRange("executeBuffPerLevel", 0.1, 0.0, 1.0);

        LIFESTEAL_BUFF_PER_LEVEL = builder
                .comment("How much health should be stolen per livesteal level? (default: 0.05)")
                .defineInRange("lifestealBuffPerLevel", 0.05, 0.0, 1.0);

        builder.pop();

        builder.push("Stats");

        // Level 18341 is about where my data starts to break :/
        MAX_LEVEL = builder
                .comment("Max mob level you can achieve? (default: 18340)")
                .defineInRange("maxLevel", 18340, 1, Integer.MAX_VALUE);

        KILLS_FORMULA = builder
                .comment("This string is passed to a custom formula parser as a formula, and is used for level calculations.")
                .comment("The formula should be strictly increasing. Formula supports +-*/^ and unary minus operators, and parenthesis.")
                .comment("For a level, L (case-sensitive), this equation maps it to an amount of kills. (default: L * (L + 1))")
                .define("killsFormula", "L * (L + 1)");

        MONOTONY_CHECK = builder
                .comment("Should the kills formula be fully checked upon modloading?")
                .comment("Use this if you don't know if the killsFormula is strictly increasing.")
                .comment("This is laggy! Don't leave this on. (default: false)")
                .define("monotonyCheck", false);

        builder.pop();

        builder.push("Data");

        AUTOSAVE_INTERVAL = builder
                .comment("How many ticks until the server saves all bestiary data automatically? (default: 12000 = 10 minutes)")
                        .defineInRange("autosaveInterval", 12000, 100, Integer.MAX_VALUE);

        builder.pop();

        COMMON_CONFIG = builder.build();
    }
}
