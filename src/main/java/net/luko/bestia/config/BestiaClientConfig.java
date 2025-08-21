package net.luko.bestia.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class BestiaClientConfig {
    public static final ForgeConfigSpec CLIENT_CONFIG;

    public static final ForgeConfigSpec.BooleanValue SHOWN_BEFORE;

    public static final ForgeConfigSpec.BooleanValue SHOW_NOTIFICATION_BADGES;

    public static final ForgeConfigSpec.BooleanValue SHOW_LEVEL_UP_TOASTS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        SHOWN_BEFORE = builder
                .comment("Show the info screen upon opening the bestiary? (updated by the screen itself)")
                .define("shownBefore", false);

        SHOW_NOTIFICATION_BADGES = builder
                .comment("When a mob has unspent special buff points, should a red alert/icon show up in the Bestiary for it?")
                .define("showNotificationBadges", true);

        SHOW_LEVEL_UP_TOASTS = builder
                .comment("Show a toast when you level up a mob?")
                .define("showLevelUpToasts", true);

        CLIENT_CONFIG = builder.build();
    }
}
