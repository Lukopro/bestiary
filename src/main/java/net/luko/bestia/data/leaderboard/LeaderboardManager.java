package net.luko.bestia.data.leaderboard;

import com.mojang.authlib.GameProfile;
import net.luko.bestia.Bestia;
import net.luko.bestia.config.BestiaCommonConfig;
import net.luko.bestia.data.BestiaryKey;
import net.luko.bestia.data.BestiaryManager;
import net.luko.bestia.data.PlayerBestiaryStore;
import net.luko.bestia.server.BestiaryOfflineCache;
import net.luko.bestia.util.MobIdUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraftforge.fml.LogicalSide;

import java.util.*;

public class LeaderboardManager {
    public static List<LeaderboardEntry> getLeaderboard(ResourceLocation mobId, MinecraftServer server){
        if(!MobIdUtil.validBestiaryMob(mobId, LogicalSide.SERVER)){
            Bestia.LOGGER.error("Player requested leaderboard data for invalid mob");
            return null;
        }

        List<LeaderboardEntry> leaderboard = new ArrayList<>();

        addOnlinePlayers(leaderboard, mobId);
        addOfflinePlayers(leaderboard, mobId, server);

        return leaderboard;
    }

    private static void addOnlinePlayers(List<LeaderboardEntry> leaderboard, ResourceLocation mobId) {
        for(BestiaryManager manager : PlayerBestiaryStore.getAll().values()){
            String name = manager.getPlayerName();
            int level = manager.getData(mobId).level();
            if(level < BestiaCommonConfig.MIN_LEADERBOARD_LEVEL.get()) continue;

            leaderboard.add(new LeaderboardEntry(name, level));
        }
    }

    private static void addOfflinePlayers(List<LeaderboardEntry> leaderboard, ResourceLocation mobId, MinecraftServer server){
        Map<UUID, CompoundTag> offlineTags = BestiaryOfflineCache.getAll();
        for(var entry : offlineTags.entrySet()){
            UUID uuid = entry.getKey();
            CompoundTag tag = entry.getValue();

            BestiaryManager tempManager = new BestiaryManager();
            tempManager.loadFromNBT(tag, null); // No name needed for tempManager
            int level = tempManager.getData(mobId).level();
            if(level < BestiaCommonConfig.MIN_LEADERBOARD_LEVEL.get()) continue;

            String name;

            if(tag.contains(BestiaryKey.PLAYER_NAME.get())){
                name = tag.getString(BestiaryKey.PLAYER_NAME.get());
            } else name = getNameWithoutTag(uuid, server);

            leaderboard.add(new LeaderboardEntry(name, level));
        }
    }

    public static String getNameWithoutTag(UUID uuid, MinecraftServer server){
        ServerPlayer onlinePlayer = server.getPlayerList().getPlayer(uuid);
        if(onlinePlayer != null) return onlinePlayer.getName().getString();

        GameProfileCache gameProfileCache = server.getProfileCache();
        if(gameProfileCache != null){
            Optional<GameProfile> optionalGameProfile = gameProfileCache.get(uuid);
            if(optionalGameProfile.isPresent()){
                return optionalGameProfile.get().getName();
            }
        }

        Bestia.LOGGER.warn("Could not find name for player: {}, this is either a bug or this player played before mod version 3.0 and has not logged in for a while.", uuid);
        return uuid.toString();
    }
}
