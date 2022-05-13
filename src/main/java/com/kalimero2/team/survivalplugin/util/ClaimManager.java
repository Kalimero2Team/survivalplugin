package com.kalimero2.team.survivalplugin.util;

import de.jeff_media.morepersistentdatatypes.DataType;
import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;

public class ClaimManager {

    private final NamespacedKey owner_key;
    private final NamespacedKey legacy_trusted_key;
    private final NamespacedKey trustlist_key;
    private final NamespacedKey teamclaim_key;
    private final SurvivalPlugin plugin;

    public ClaimManager(SurvivalPlugin plugin){
        this.plugin = plugin;
        owner_key = new NamespacedKey(plugin, "owner");
        legacy_trusted_key = new NamespacedKey(plugin, "trusted");
        trustlist_key = new NamespacedKey(plugin, "trustList");
        teamclaim_key = new NamespacedKey(plugin, "teamclaim");
    }

    public boolean isClaimed(Chunk chunk){
        return chunk.getPersistentDataContainer().has(owner_key, PersistentDataType.STRING);
    }

    public boolean isTeamClaim(Chunk chunk){
        return chunk.getPersistentDataContainer().has(teamclaim_key, PersistentDataType.STRING);
    }

    public UUID getOwner(Chunk chunk){
        String string = chunk.getPersistentDataContainer().get(owner_key, PersistentDataType.STRING);
        if(string != null){
            return UUID.fromString(string);
        }
        return null;
    }

    public List<UUID> getTrustedList(Chunk chunk){
        List<UUID> trustList;
        if(chunk.getPersistentDataContainer().has(trustlist_key, DataType.STRING_ARRAY)){
            trustList = new ArrayList<>();
            Arrays.stream(Objects.requireNonNull(chunk.getPersistentDataContainer().get(trustlist_key, DataType.STRING_ARRAY))).forEach(uuid -> trustList.add(UUID.fromString(uuid)));
        }else if(chunk.getPersistentDataContainer().has(legacy_trusted_key, DataType.OFFLINE_PLAYER_ARRAY)){
            trustList = new ArrayList<>();
            OfflinePlayer[] offlinePlayers = chunk.getPersistentDataContainer().get(legacy_trusted_key, DataType.OFFLINE_PLAYER_ARRAY);
            if(offlinePlayers != null){
                for(OfflinePlayer offlinePlayer : offlinePlayers){
                    trustList.add(offlinePlayer.getUniqueId());
                }
            }
            chunk.getPersistentDataContainer().set(trustlist_key, DataType.STRING_ARRAY, trustList.stream().map(UUID::toString).toArray(String[]::new));
            plugin.getLogger().info("Converted trust list of chunk " + chunk.getX() + "," + chunk.getZ() + " in World "+ chunk.getWorld().getName() +" to string based array");

        }else {
            trustList = new ArrayList<>();
        }

        return trustList;
    }

    public Component getTeamClaimMessage(Chunk chunk){
        if(isTeamClaim(chunk)){
            return MiniMessage.miniMessage().deserialize(Objects.requireNonNullElse(chunk.getPersistentDataContainer().get(teamclaim_key, PersistentDataType.STRING), ""));
        }
        return Component.text("");
    }

    public boolean trust(Chunk chunk, OfflinePlayer offlinePlayer){
        if(this.getTrustedList(chunk).contains(offlinePlayer.getUniqueId())){
            return false;
        }

        List<UUID> trustList = getTrustedList(chunk);
        trustList.add(offlinePlayer.getUniqueId());
        chunk.getPersistentDataContainer().set(trustlist_key, DataType.STRING_ARRAY, trustList.stream().map(UUID::toString).toArray(String[]::new));
        return true;
    }

    public boolean unTrust(Chunk chunk, OfflinePlayer offlinePlayer){
        if(!this.getTrustedList(chunk).contains(offlinePlayer.getUniqueId())){
            return false;
        }

        List<UUID> trustList = getTrustedList(chunk);
        trustList.remove(offlinePlayer.getUniqueId());
        chunk.getPersistentDataContainer().set(trustlist_key, DataType.STRING_ARRAY, trustList.stream().map(UUID::toString).toArray(String[]::new));
        return true;
    }

    public boolean canModify(Chunk chunk, OfflinePlayer offlinePlayer){
        if(this.isClaimed(chunk)){
            if(!this.getOwner(chunk).equals(offlinePlayer.getUniqueId())){
                return this.getTrustedList(chunk).contains(offlinePlayer.getUniqueId());
            }
        }
        return true;
    }

    public boolean claimChunk(Chunk chunk, OfflinePlayer offlinePlayer){
        if(!this.isClaimed(chunk)) {
            if (this.canClaim(offlinePlayer)) {
                chunk.getPersistentDataContainer().set(owner_key,PersistentDataType.STRING,offlinePlayer.getUniqueId().toString());
                ExtraPlayerData extraPlayerData = getExtraPlayerData(offlinePlayer);
                extraPlayerData.chunks.add(getSerializableChunk(chunk));
                setExtraPlayerData(offlinePlayer, extraPlayerData);
                return true;
            }
        }
        return false;
    }

    public void teamClaimChunk(Chunk chunk, OfflinePlayer offlinePlayer, String message){
        chunk.getPersistentDataContainer().set(owner_key,PersistentDataType.STRING,offlinePlayer.getUniqueId().toString());
        chunk.getPersistentDataContainer().set(teamclaim_key, PersistentDataType.STRING, message);
        ExtraPlayerData extraPlayerData = getExtraPlayerData(offlinePlayer);
        extraPlayerData.chunks.add(getSerializableChunk(chunk));
        setExtraPlayerData(offlinePlayer, extraPlayerData);

    }

    public boolean unClaimChunk(Chunk chunk, OfflinePlayer offlinePlayer){
        if(this.isClaimed(chunk)){
            if(this.getOwner(chunk).equals(offlinePlayer.getUniqueId())){
                return forceUnClaimChunk(chunk);
            }
        }
        return false;
    }

    public boolean forceUnClaimChunk(Chunk chunk){
        if(this.isClaimed(chunk)){
            try {
                OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(getOwner(chunk));
                ExtraPlayerData extraPlayerData = getExtraPlayerData(offlinePlayer);
                extraPlayerData.chunks.remove(getSerializableChunk(chunk));
                setExtraPlayerData(offlinePlayer, extraPlayerData);
            }catch (Exception exception){
                exception.printStackTrace();
            }
            chunk.getPersistentDataContainer().remove(owner_key);
            chunk.getPersistentDataContainer().remove(legacy_trusted_key);
            chunk.getPersistentDataContainer().remove(trustlist_key);
            chunk.getPersistentDataContainer().remove(teamclaim_key);

            return true;
        }
        return false;
    }

    public boolean canClaim(OfflinePlayer offlinePlayer){
        return getExtraPlayerData(offlinePlayer).chunks.size() < getExtraPlayerData(offlinePlayer).maxclaims;
    }

    public ExtraPlayerData getExtraPlayerData(OfflinePlayer offlinePlayer){
        File file = new File(plugin.playerDataFolder + "/"+ offlinePlayer.getUniqueId() + ".json");
        if(!file.exists()){
            return new ExtraPlayerData(new HashSet<>(), plugin.getConfig().getInt("claim.max-claims"),false);
        }
        return ExtraPlayerData.loadData(file.getAbsolutePath());
    }

    public void setExtraPlayerData(OfflinePlayer offlinePlayer, ExtraPlayerData extraPlayerData){
        File file = new File(plugin.playerDataFolder + "/"+ offlinePlayer.getUniqueId() + ".json");
        extraPlayerData.saveData(file.getAbsolutePath());
    }

    public Chunk getChunk(SerializableChunk serializableChunk){
        String world = serializableChunk.world;
        Integer x = serializableChunk.x;
        Integer z = serializableChunk.z;
        return Objects.requireNonNull(plugin.getServer().getWorld(UUID.fromString(world))).getChunkAt(x,z);
    }

    public SerializableChunk getSerializableChunk(Chunk chunk){
        return new SerializableChunk(chunk.getWorld().getUID().toString(),chunk.getX(), chunk.getZ());
    }

}
