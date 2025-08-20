package net.luko.bestia.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class BestiaClientConfig {
    public static final ForgeConfigSpec CLIENT_CONFIG;

    public static final ForgeConfigSpec.BooleanValue SHOWN_BEFORE;

    public static final ForgeConfigSpec.BooleanValue SHOW_NOTIFICATION_BADGES;

    static{
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        SHOWN_BEFORE = builder
                .comment("Show the info screen upon opening the bestiary? (updated by the screen itself)")
                .define("shownBefore", false);

        SHOW_NOTIFICATION_BADGES = builder
                .comment("When a mob has unspent special buff points, should a red alert/icon show up in the Bestiary for it?")
                .define("showNotificationBadges", true);

        CLIENT_CONFIG = builder.build();
    }
}
