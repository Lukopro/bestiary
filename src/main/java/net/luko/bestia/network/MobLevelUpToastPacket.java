package net.luko.bestia.network;

import net.luko.bestia.client.ClientBestiaryData;
import net.luko.bestia.data.BestiaryData;
import net.luko.bestia.screen.MobLevelUpToast;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MobLevelUpToastPacket {
    private final ResourceLocation mobId;

    public MobLevelUpToastPacket(ResourceLocation mobId){
        this.mobId = mobId;
    }

    public static void encode(MobLevelUpToastPacket msg, FriendlyByteBuf buf){
        buf.writeResourceLocation(msg.mobId);
    }

    public static MobLevelUpToastPacket decode(FriendlyByteBuf buf){
        return new MobLevelUpToastPacket(buf.readResourceLocation());
    }

    public static void handle(MobLevelUpToastPacket msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            BestiaryData data = ClientBestiaryData.getFor(msg.mobId);
            Minecraft mc = Minecraft.getInstance();
            mc.getToasts().addToast(new MobLevelUpToast(msg.mobId, data));
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.0F));
        });
        context.setPacketHandled(true);
    }
}
