package net.luko.bestia.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class BestiaClientConfig {
    public static final ForgeConfigSpec CLIENT_CONFIG;

    public static final ForgeConfigSpec.BooleanValue SHOWN_BEFORE;

    static{
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        SHOWN_BEFORE = builder
                .comment("Show the info screen upon opening the bestiary? (updated by the screen itself)")
                .define("shownBefore", false);

        CLIENT_CONFIG = builder.build();
    }
}
