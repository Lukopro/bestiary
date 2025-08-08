package net.luko.bestia.network;

import net.luko.bestia.Bestia;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModPackets {
    private static int packetId = 0;
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "main"),
            () -> "1.0", s -> true, s -> true
    );

    public static void register(){
        CHANNEL.registerMessage(packetId++, BestiarySyncPacket.class,
                BestiarySyncPacket::encode,
                BestiarySyncPacket::decode,
                BestiarySyncPacket::handle);

        CHANNEL.registerMessage(packetId++, RequestBestiarySyncPacket.class,
                RequestBestiarySyncPacket::encode,
                RequestBestiarySyncPacket::decode,
                RequestBestiarySyncPacket::handle);

        CHANNEL.registerMessage(packetId++, SpendPointPacket.class,
                SpendPointPacket::encode,
                SpendPointPacket::decode,
                SpendPointPacket::handle);

        CHANNEL.registerMessage(packetId++, ClearPointsPacket.class,
                ClearPointsPacket::encode,
                ClearPointsPacket::decode,
                ClearPointsPacket::handle);
    }
}
