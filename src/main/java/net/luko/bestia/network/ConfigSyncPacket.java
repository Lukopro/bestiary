package net.luko.bestia.network;

import net.luko.bestia.client.ClientConfigStore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ConfigSyncPacket {
    private final ClientConfigStore config;

    public ConfigSyncPacket(ClientConfigStore config){
        this.config = config;
    }

    public static void encode(ConfigSyncPacket packet, FriendlyByteBuf buf){
        ClientConfigStore config = packet.config;

        buf.writeBoolean(config.enableSpecialBuffs);

        buf.writeInt(config.maxLevel);
        buf.writeUtf(config.killsFormula, 256);

        buf.writeDouble(config.damageFactorPerLevel);
        buf.writeDouble(config.resistanceFactorPerLevel);

        buf.writeInt(config.levelsPerSpecialBuffPoint);

        buf.writeInt(config.rerollMaxLevel);
        buf.writeInt(config.executeMaxLevel);
        buf.writeInt(config.lifestealMaxLevel);
        buf.writeInt(config.reflexMaxLevel);
        buf.writeInt(config.dazeMaxLevel);

        buf.writeDouble(config.executeBuffPerLevel);
        buf.writeDouble(config.lifestealBuffPerLevel);
        buf.writeDouble(config.reflexBuffPerLevel);
        buf.writeInt(config.dazeBuffPerLevel);

        buf.writeInt(config.minLeaderboardLevel);

        List<ResourceLocation> blacklistedEntities = config.blacklistedEntities;
        List<ResourceLocation> whitelistedEntities = config.whitelistedEntities;

        buf.writeInt(blacklistedEntities.size());
        for(ResourceLocation id : blacklistedEntities){
            buf.writeResourceLocation(id);
        }

        buf.writeInt(whitelistedEntities.size());
        for(ResourceLocation id : whitelistedEntities){
            buf.writeResourceLocation(id);
        }
    }

    public static ConfigSyncPacket decode(FriendlyByteBuf buf){
        ClientConfigStore config = new ClientConfigStore();

        config.enableSpecialBuffs = buf.readBoolean();

        config.maxLevel = buf.readInt();
        config.killsFormula = buf.readUtf(256);

        config.damageFactorPerLevel = buf.readDouble();
        config.resistanceFactorPerLevel = buf.readDouble();

        config.levelsPerSpecialBuffPoint = buf.readInt();

        config.rerollMaxLevel = buf.readInt();
        config.executeMaxLevel = buf.readInt();
        config.lifestealMaxLevel = buf.readInt();
        config.reflexMaxLevel = buf.readInt();
        config.dazeMaxLevel = buf.readInt();

        config.executeBuffPerLevel = buf.readDouble();
        config.lifestealBuffPerLevel = buf.readDouble();
        config.reflexBuffPerLevel = buf.readDouble();
        config.dazeBuffPerLevel = buf.readInt();

        config.minLeaderboardLevel = buf.readInt();

        int blacklistSize = buf.readInt();
        List<ResourceLocation> blacklistedEntities = new ArrayList<>();

        for(int i = 0; i < blacklistSize; i++){
            blacklistedEntities.add(buf.readResourceLocation());
        }

        int whitelistSize = buf.readInt();
        List<ResourceLocation> whitelistedEntities = new ArrayList<>();

        for(int i = 0; i < whitelistSize; i++){
            whitelistedEntities.add(buf.readResourceLocation());
        }

        config.blacklistedEntities = blacklistedEntities;
        config.whitelistedEntities = whitelistedEntities;

        return new ConfigSyncPacket(config);
    }

    public static void handle(ConfigSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientConfigStore.sync(packet.config));
        context.setPacketHandled(true);
    }
}
