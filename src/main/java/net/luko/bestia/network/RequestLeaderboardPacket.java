package net.luko.bestia.network;

import net.luko.bestia.data.leaderboard.LeaderboardEntry;
import net.luko.bestia.data.leaderboard.LeaderboardManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Supplier;

public class RequestLeaderboardPacket {
    private final ResourceLocation mobId;

    public RequestLeaderboardPacket(ResourceLocation mobId){
        this.mobId = mobId;
    }

    public static void encode(RequestLeaderboardPacket packet, FriendlyByteBuf buf){
        buf.writeResourceLocation(packet.mobId);
    }

    public static RequestLeaderboardPacket decode(FriendlyByteBuf buf){
        return new RequestLeaderboardPacket(buf.readResourceLocation());
    }

    public static void handle(RequestLeaderboardPacket packet, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if(sender == null) return;

            List<LeaderboardEntry> leaderboard = LeaderboardManager.getLeaderboard(packet.mobId, context.getSender().getServer());

            ModPackets.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender),
                    new LeaderboardPacket(packet.mobId, leaderboard));
        });
        context.setPacketHandled(true);
    }
}
