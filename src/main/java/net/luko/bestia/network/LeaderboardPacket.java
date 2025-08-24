package net.luko.bestia.network;

import net.luko.bestia.data.leaderboard.LeaderboardEntry;
import net.luko.bestia.screen.BestiaryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class LeaderboardPacket {
    private final ResourceLocation mobId;
    private final List<LeaderboardEntry> leaderboard;

    public LeaderboardPacket(ResourceLocation mobId, List<LeaderboardEntry> leaderboard){
        this.mobId = mobId;
        this.leaderboard = leaderboard;
    }

    public static void encode(LeaderboardPacket packet, FriendlyByteBuf buf){
        buf.writeResourceLocation(packet.mobId);
        buf.writeInt(packet.leaderboard.size());
        for(LeaderboardEntry entry : packet.leaderboard){
            buf.writeUtf(entry.name());
            buf.writeInt(entry.level());
        }
    }

    public static LeaderboardPacket decode(FriendlyByteBuf buf){
        ResourceLocation mobId = buf.readResourceLocation();
        int size = buf.readInt();
        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        for(int i = 0; i < size; i++){
            String name = buf.readUtf();
            int level = buf.readInt();
            leaderboard.add(new LeaderboardEntry(name, level));
        }
        return new LeaderboardPacket(mobId, leaderboard);
    }

    public static void handle(LeaderboardPacket packet, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if(Minecraft.getInstance().screen instanceof BestiaryScreen screen){
                screen.openLeaderboardScreenComponent(packet.mobId, packet.leaderboard);
            }
        });
        context.setPacketHandled(true);
    }
}
