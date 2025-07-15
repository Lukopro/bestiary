package net.luko.bestiary.network;

import net.luko.bestiary.Bestiary;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModPackets {
    private static int packetId = 0;
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(Bestiary.MODID, "main"),
            () -> "1.0", s -> true, s -> true
    );

    public static void register(){
        CHANNEL.registerMessage(packetId++, SyncBestiaryPacket.class,
                SyncBestiaryPacket::encode,
                SyncBestiaryPacket::decode,
                SyncBestiaryPacket::handle);
    }
}
