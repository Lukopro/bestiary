package net.luko.bestia.data;

import net.luko.bestia.data.buff.MobBuff;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;

public class BestiaryDataSerializer {
    public static void write(FriendlyByteBuf buf, BestiaryData data){
        buf.writeVarInt(data.kills());
        buf.writeVarInt(data.level());
        buf.writeVarInt(data.remainingKills());

        MobBuff mobBuff = data.mobBuff();
        buf.writeFloat(mobBuff.damageFactor());
        buf.writeFloat(mobBuff.resistanceFactor());

        Map<String, Integer> spentPoints = data.spentPoints();

        buf.writeVarInt(data.totalPoints());
        buf.writeVarInt(data.remainingPoints());

        buf.writeVarInt(spentPoints.size());
        for(var entry : spentPoints.entrySet()){
            buf.writeUtf(entry.getKey(), 256);
            buf.writeVarInt(entry.getValue());
        }
    }

    public static BestiaryData read(FriendlyByteBuf buf){
        int kills = buf.readVarInt();
        int level = buf.readVarInt();
        int nextLevelKills = buf.readVarInt();

        float damageFactor = buf.readFloat();
        float resistanceFactor = buf.readFloat();

        Map<String, Integer> spentPoints = new HashMap<>();

        int totalPoints = buf.readVarInt();
        int remainingPoints = buf.readVarInt();

        int entriesSize = buf.readVarInt();
        for(int i = 0; i < entriesSize; i++){
            spentPoints.put(buf.readUtf(256), buf.readVarInt());
        }

        return new BestiaryData(kills, level, nextLevelKills,
                new MobBuff(damageFactor, resistanceFactor), totalPoints, remainingPoints, spentPoints);
    }
}
