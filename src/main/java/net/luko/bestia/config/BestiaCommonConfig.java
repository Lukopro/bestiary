package net.luko.bestia.config;

import net.luko.bestia.client.ClientConfigStore;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.Objects;

public class BestiaCommonConfig {
    public static final ForgeConfigSpec COMMON_CONFIG;

    public static ForgeConfigSpec.BooleanValue ENABLE_SPECIAL_BUFFS;

    public static ForgeConfigSpec.DoubleValue DAMAGE_FACTOR_PER_LEVEL;
    public static ForgeConfigSpec.DoubleValue RESISTANCE_FACTOR_PER_LEVEL;

    public static ForgeConfigSpec.IntValue LEVELS_PER_SPECIAL_BUFF_POINT;

    public static ForgeConfigSpec.IntValue REROLL_MAX_LEVEL;
    public static ForgeConfigSpec.IntValue EXECUTE_MAX_LEVEL;
    public static ForgeConfigSpec.IntValue LIFESTEAL_MAX_LEVEL;
    public static ForgeConfigSpec.IntValue REFLEX_MAX_LEVEL;
    public static ForgeConfigSpec.IntValue DAZE_MAX_LEVEL;

    public static ForgeConfigSpec.DoubleValue EXECUTE_BUFF_PER_LEVEL;
    public static ForgeConfigSpec.DoubleValue LIFESTEAL_BUFF_PER_LEVEL;
    public static ForgeConfigSpec.DoubleValue REFLEX_BUFF_PER_LEVEL;
    public static ForgeConfigSpec.IntValue DAZE_BUFF_PER_LEVEL;

    public static ForgeConfigSpec.IntValue MAX_LEVEL;
    public static ForgeConfigSpec.ConfigValue<String> KILLS_FORMULA;
    public static ForgeConfigSpec.BooleanValue MONOTONY_CHECK;
    public static ForgeConfigSpec.IntValue MIN_LEADERBOARD_LEVEL;

    public static ForgeConfigSpec.IntValue AUTOSAVE_INTERVAL;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_ENTITIES;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> WHITELISTED_ENTITIES;

    public static List<ResourceLocation> PARSED_BLACKLISTED_ENTITIES;
    public static List<ResourceLocation> PARSED_WHITELISTED_ENTITIES;

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
                .defineInRange("damageFactorPerLevel", 0.02, 0.0, 1.0);

        RESISTANCE_FACTOR_PER_LEVEL = builder
                .comment("For each level, by what factor should incoming damage be multiplied by (multiplicative)? (default: 0.96)")
                .defineInRange("resistanceFactorPerLevel", 0.98, 0.1, 1.0);

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

        REFLEX_MAX_LEVEL = builder
                .comment("How many levels can you apply to special buff 'reflex'? (default: 5, 0 to disable)")
                .defineInRange("reflexMaxLevel", 5, 0, Integer.MAX_VALUE);

        DAZE_MAX_LEVEL = builder
                .comment("How many levels can you apply to special buff 'daze'? (default: 3, 0 to disable)")
                .defineInRange("dazeMaxLevel", 3, 0, Integer.MAX_VALUE);



        EXECUTE_BUFF_PER_LEVEL = builder
                .comment("How much should be added to the execute buff's threshold per level? (default: 0.1)")
                .defineInRange("executeBuffPerLevel", 0.1, 0.0, 1.0);

        LIFESTEAL_BUFF_PER_LEVEL = builder
                .comment("How much health should be stolen per livesteal level? (default: 0.05)")
                .defineInRange("lifestealBuffPerLevel", 0.05, 0.0, 1.0);

        REFLEX_BUFF_PER_LEVEL = builder
                .comment("How much knockback should be applied to attacking mobs? (default: 0.4)")
                .defineInRange("reflexBuffPerLevel", 0.4, 0.0, 100.0);

        DAZE_BUFF_PER_LEVEL = builder
                .comment("How many ticks should affected mobs be dazed for? (default: 8)")
                .defineInRange("dazeBuffPerLevel", 8, 0, Integer.MAX_VALUE);

        builder.pop();

        builder.push("Stats");

        MAX_LEVEL = builder
                .comment("Max mob level you can achieve? (default: 10000)")
                .defineInRange("maxLevel", 10000, 1, Integer.MAX_VALUE);

        KILLS_FORMULA = builder
                .comment("This string is passed to a custom formula parser as a formula, and is used for level calculations.")
                .comment("The formula should be strictly increasing. Formula supports +-*/^ and unary minus operators, and parenthesis.")
                .comment("For a level, L (case-sensitive), this equation maps it to an amount of kills. (default: L * (L + 1), max length = 256 characters)")
                .define("killsFormula", "L * (L + 1)", value -> value instanceof String && ((String) value).length() <= 256);

        MONOTONY_CHECK = builder
                .comment("Should the kills formula be fully checked upon modloading?")
                .comment("Use this if you don't know if the killsFormula is strictly increasing.")
                .comment("This is laggy! Don't leave this on. (default: false)")
                .define("monotonyCheck", false);

        MIN_LEADERBOARD_LEVEL = builder
                .comment("Players with a mob level lower than this are omitted on the leaderboard. (Default: 1)")
                .defineInRange("minLeaderboardLevel", 1, 0, Integer.MAX_VALUE);

        builder.pop();

        builder.push("Data");

        AUTOSAVE_INTERVAL = builder
                .comment("How many ticks until the server saves all bestiary data automatically? (default: 12000 = 10 minutes)")
                        .defineInRange("autosaveInterval", 12000, 100, Integer.MAX_VALUE);

        BLACKLISTED_ENTITIES = builder
                .comment("Any mobs that should be excluded from Bestiary Data storage and display? (default: [])")
                        .defineList("blacklistedEntities",
                                List.of(),
                                obj -> obj instanceof String);

        WHITELISTED_ENTITIES = builder
                .comment("Any mobs in the MISC category that should be included in Bestiary Data storage and display? (default: [])")
                .defineList("whitelistedEntities",
                        List.of(),
                        obj -> obj instanceof String);

        builder.pop();

        COMMON_CONFIG = builder.build();
    }

    public static void initializeCaches(){
        PARSED_BLACKLISTED_ENTITIES = BLACKLISTED_ENTITIES.get().stream()
                .map(ResourceLocation::tryParse)
                .filter(Objects::nonNull)
                .toList();

        PARSED_WHITELISTED_ENTITIES = WHITELISTED_ENTITIES.get().stream()
                .map(ResourceLocation::tryParse)
                .filter(Objects::nonNull)
                .toList();
    }

    public static boolean entityIsBlacklisted(ResourceLocation id){
        return PARSED_BLACKLISTED_ENTITIES.contains(id);
    }

    public static boolean entityIsWhitelisted(ResourceLocation id){
        return PARSED_WHITELISTED_ENTITIES.contains(id);
    }

    public static ClientConfigStore createConfigToSync(){
        ClientConfigStore config = new ClientConfigStore();

        config.enableSpecialBuffs = ENABLE_SPECIAL_BUFFS.get();

        config.damageFactorPerLevel = DAMAGE_FACTOR_PER_LEVEL.get();
        config.resistanceFactorPerLevel = RESISTANCE_FACTOR_PER_LEVEL.get();

        config.maxLevel = MAX_LEVEL.get();
        config.killsFormula = KILLS_FORMULA.get();

        config.levelsPerSpecialBuffPoint = LEVELS_PER_SPECIAL_BUFF_POINT.get();

        config.rerollMaxLevel = REROLL_MAX_LEVEL.get();
        config.executeMaxLevel = EXECUTE_MAX_LEVEL.get();
        config.lifestealMaxLevel = LIFESTEAL_MAX_LEVEL.get();
        config.reflexMaxLevel = REFLEX_MAX_LEVEL.get();
        config.dazeMaxLevel = DAZE_MAX_LEVEL.get();

        config.executeBuffPerLevel = EXECUTE_BUFF_PER_LEVEL.get();
        config.lifestealBuffPerLevel = LIFESTEAL_BUFF_PER_LEVEL.get();
        config.reflexBuffPerLevel = REFLEX_BUFF_PER_LEVEL.get();
        config.dazeBuffPerLevel = DAZE_BUFF_PER_LEVEL.get();

        config.minLeaderboardLevel = MIN_LEADERBOARD_LEVEL.get();

        config.blacklistedEntities = PARSED_BLACKLISTED_ENTITIES;
        config.whitelistedEntities = PARSED_WHITELISTED_ENTITIES;

        return config;
    }
}
