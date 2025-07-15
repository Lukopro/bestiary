package net.luko.bestiary.network;

import net.luko.bestiary.client.ClientBestiaryData;
import net.luko.bestiary.data.BestiaryData;
import net.luko.bestiary.data.BestiaryDataSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SyncBestiaryPacket {
    private final Map<ResourceLocation, BestiaryData> data;

    public SyncBestiaryPacket(Map<ResourceLocation, BestiaryData> data){
        this.data = data;
    }

    public static void encode(SyncBestiaryPacket packet, FriendlyByteBuf buf){
        buf.writeVarInt(packet.data.size());
        for(var entry : packet.data.entrySet()){
            buf.writeResourceLocation(entry.getKey());
            BestiaryDataSerializer.write(buf, entry.getValue());
        }
    }

    public static SyncBestiaryPacket decode(FriendlyByteBuf buf){
        int size = buf.readVarInt();
        Map<ResourceLocation, BestiaryData> map = new HashMap<>();
        for(int i = 0; i < size; i++){
            ResourceLocation mobId = buf.readResourceLocation();
            BestiaryData data = BestiaryDataSerializer.read(buf);
            map.put(mobId, data);
        }
        return new SyncBestiaryPacket(map);
    }

    public static void handle(SyncBestiaryPacket packet, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> ClientBestiaryData.set(packet.data));
        context.get().setPacketHandled(true);
    }
}
