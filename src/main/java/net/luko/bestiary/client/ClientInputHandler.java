package net.luko.bestiary.client;

import net.luko.bestiary.Bestiary;
import net.luko.bestiary.network.ModPackets;
import net.luko.bestiary.network.RequestBestiarySyncPacket;
import net.luko.bestiary.screen.BestiaryScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Bestiary.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientInputHandler {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event){
        if(event.phase == TickEvent.Phase.END){
            while(ModKeybinds.KEY_OPEN_BESTIARY.get().consumeClick()){
                ModPackets.CHANNEL.sendToServer(new RequestBestiarySyncPacket());
                Minecraft.getInstance().setScreen(new BestiaryScreen(ClientBestiaryData.getAll()));
            }
        }
    }
}
