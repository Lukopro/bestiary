package net.luko.bestia;

import com.mojang.logging.LogUtils;
import net.luko.bestia.config.BestiaClientConfig;
import net.luko.bestia.config.BestiaCommonConfig;
import net.luko.bestia.data.buff.special.SpecialBuffRegistry;
import net.luko.bestia.network.ModPackets;
import net.luko.bestia.util.LevelFormula;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Bestia.MODID)
public class Bestia {
    public static final String MODID = "bestia";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Bestia(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        context.registerConfig(ModConfig.Type.COMMON, BestiaCommonConfig.COMMON_CONFIG);
        context.registerConfig(ModConfig.Type.CLIENT, BestiaClientConfig.CLIENT_CONFIG);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ModPackets.register();
        event.enqueueWork(SpecialBuffRegistry::init);
        event.enqueueWork(LevelFormula::init);
    }
}
