package net.luko.bestiary.data;

import net.minecraft.network.FriendlyByteBuf;

public class BestiaryDataSerializer {
    public static void write(FriendlyByteBuf buf, BestiaryData data){
        buf.writeVarInt(data.kills());
        buf.writeVarInt(data.level());
        buf.writeVarInt(data.nextLevelKills());

        MobBuff mobBuff = data.mobBuff();
        buf.writeFloat(mobBuff.damageFactor());
        buf.writeFloat(mobBuff.resistanceFactor());
    }

    public static BestiaryData read(FriendlyByteBuf buf){
        int kills = buf.readVarInt();
        int level = buf.readVarInt();
        int nextLevelKills = buf.readVarInt();

        float damageFactor = buf.readFloat();
        float resistanceFactor = buf.readFloat();

        return new BestiaryData(kills, level, nextLevelKills, new MobBuff(damageFactor, resistanceFactor));
    }
}
