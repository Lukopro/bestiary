package net.luko.bestia.network;

import net.luko.bestia.Bestia;
import net.luko.bestia.data.BestiaryManager;
import net.luko.bestia.data.PlayerBestiaryStore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClearPointsPacket {
    private final ResourceLocation mobId;

    public ClearPointsPacket(ResourceLocation mobId){
        this.mobId = mobId;
    }

    public static void encode(ClearPointsPacket packet, FriendlyByteBuf buf){
        buf.writeResourceLocation(packet.mobId);
    }

    public static ClearPointsPacket decode(FriendlyByteBuf buf){
        ResourceLocation mobId = buf.readResourceLocation();
        return new ClearPointsPacket(mobId);
    }

    public static void handle(ClearPointsPacket packet, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Bestia.LOGGER.info("Handling");
            ServerPlayer player = context.getSender();
            if(player != null){
                BestiaryManager manager = PlayerBestiaryStore.get(player);
                Bestia.LOGGER.info("Clearing");
                manager.onClearPointsWithSync(player, packet.mobId);
                Bestia.LOGGER.info("Gucci");
            }
        });
        context.setPacketHandled(true);
    }
}
