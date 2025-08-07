package net.luko.bestia.config;

import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.ForgeConfigSpec;

public class BestiaConfig {
    public static final ForgeConfigSpec COMMON_CONFIG;

    public static ForgeConfigSpec.BooleanValue ENABLE_SPECIAL_BUFFS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Features");

        ENABLE_SPECIAL_BUFFS = builder
                .comment("View, apply, and use special buffs (the ones you get every 10 levels). This should not interfere with any data generated already.")
                .define("enableSpecialBuffs", true);

        builder.pop();

        COMMON_CONFIG = builder.build();
    }
}
