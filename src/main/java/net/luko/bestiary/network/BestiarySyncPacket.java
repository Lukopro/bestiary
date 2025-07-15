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

public class BestiarySyncPacket {
    private final Map<ResourceLocation, BestiaryData> data;

    public BestiarySyncPacket(Map<ResourceLocation, BestiaryData> data){
        this.data = data;
    }

    public static void encode(BestiarySyncPacket packet, FriendlyByteBuf buf){
        buf.writeVarInt(packet.data.size());
        for(var entry : packet.data.entrySet()){
            buf.writeResourceLocation(entry.getKey());
            BestiaryDataSerializer.write(buf, entry.getValue());
        }
    }

    public static BestiarySyncPacket decode(FriendlyByteBuf buf){
        int size = buf.readVarInt();
        Map<ResourceLocation, BestiaryData> map = new HashMap<>();
        for(int i = 0; i < size; i++){
            ResourceLocation mobId = buf.readResourceLocation();
            BestiaryData data = BestiaryDataSerializer.read(buf);
            map.put(mobId, data);
        }
        return new BestiarySyncPacket(map);
    }

    public static void handle(BestiarySyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientBestiaryData.set(packet.data));
        context.setPacketHandled(true);
    }
}
