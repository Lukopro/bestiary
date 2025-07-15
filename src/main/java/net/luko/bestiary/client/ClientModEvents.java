package net.luko.bestiary.client;

import net.luko.bestiary.Bestiary;
import net.luko.bestiary.screen.BestiaryScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Bestiary.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onKeyPress(InputEvent.Key event){
        if(KEY_OPEN_BESTIARY.consumeClick()){
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new BestiaryScreen(ClientBestiaryData.getAll()));
        }
    }
}
