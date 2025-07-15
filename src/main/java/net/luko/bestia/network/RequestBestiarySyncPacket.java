package net.luko.bestia.network;

import net.luko.bestia.data.BestiaryManager;
import net.luko.bestia.data.PlayerBestiaryStore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestBestiarySyncPacket {
    public RequestBestiarySyncPacket(){}

    public static void encode(RequestBestiarySyncPacket packet, FriendlyByteBuf buf){

    }

    public static RequestBestiarySyncPacket decode(FriendlyByteBuf buf){
        return new RequestBestiarySyncPacket();
    }

    public static void handle(RequestBestiarySyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if(player != null){
                BestiaryManager manager = PlayerBestiaryStore.get(player);
                manager.syncToPlayer(player);
            }
        });
        context.setPacketHandled(true);
    }
}
