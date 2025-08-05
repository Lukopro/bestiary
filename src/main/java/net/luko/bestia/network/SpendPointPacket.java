package net.luko.bestia.network;

import net.luko.bestia.data.BestiaryManager;
import net.luko.bestia.data.PlayerBestiaryStore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SpendPointPacket {
    private final ResourceLocation mobId;
    private final ResourceLocation buffId;

    public SpendPointPacket(ResourceLocation mobId, ResourceLocation buffId){
        this.mobId = mobId;
        this.buffId = buffId;
    }

    public static void encode(SpendPointPacket packet, FriendlyByteBuf buf){
        buf.writeResourceLocation(packet.mobId);
        buf.writeResourceLocation(packet.buffId);
    }

    public static SpendPointPacket decode(FriendlyByteBuf buf){
        ResourceLocation mobId = buf.readResourceLocation();
        ResourceLocation buffId = buf.readResourceLocation();
        return new SpendPointPacket(mobId, buffId);
    }

    public static void handle(SpendPointPacket packet, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if(player != null){
                BestiaryManager manager = PlayerBestiaryStore.get(player);
                manager.onSpendPointWithSync(player, packet.mobId, packet.buffId);
            }
        });
        context.setPacketHandled(true);
    }
}
