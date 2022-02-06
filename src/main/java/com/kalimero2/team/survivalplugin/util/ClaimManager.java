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

    public NamespacedKey owner_key;
    public NamespacedKey trusted_key;
    public NamespacedKey teamclaim_key;
    private SurvivalPlugin plugin;

    public ClaimManager(SurvivalPlugin plugin){
        this.plugin = plugin;
        owner_key = new NamespacedKey(plugin, "owner");
        trusted_key = new NamespacedKey(plugin, "trusted");
        teamclaim_key = new NamespacedKey(plugin, "teamclaim");
    }

    public boolean isClaimed(Chunk chunk){
        return chunk.getPersistentDataContainer().has(owner_key, PersistentDataType.STRING);
    }

    public boolean isTeamClaim(Chunk chunk){
        return chunk.getPersistentDataContainer().has(teamclaim_key, PersistentDataType.STRING);
    }

    public OfflinePlayer getOwner(Chunk chunk){
        String string = chunk.getPersistentDataContainer().get(owner_key, PersistentDataType.STRING);
        if(string != null){
            return plugin.getServer().getOfflinePlayer(UUID.fromString(string));
        }
        return null;
    }

    public List<OfflinePlayer> getTrustedList(Chunk chunk){
        List<OfflinePlayer> trustList = new ArrayList<>();
        if(chunk.getPersistentDataContainer().has(trusted_key,PersistentDataType.STRING)){ /*Convert to something better*/
            chunk.getPersistentDataContainer().remove(trusted_key);
            chunk.getPersistentDataContainer().set(trusted_key, DataType.OFFLINE_PLAYER_ARRAY , trustList.toArray(new OfflinePlayer[0]));
        }else if(chunk.getPersistentDataContainer().has(trusted_key, DataType.OFFLINE_PLAYER_ARRAY)){
            List<OfflinePlayer> list =  Arrays.stream(chunk.getPersistentDataContainer().get(trusted_key, DataType.OFFLINE_PLAYER_ARRAY)).toList();
            trustList = new ArrayList<>(list); // http://stackoverflow.com/questions/5755477/ddg#5755510
        }

        return trustList;
    }

    public Component getTeamClaimMessage(Chunk chunk){
        if(isTeamClaim(chunk)){
            return MiniMessage.get().parse(Objects.requireNonNullElse(chunk.getPersistentDataContainer().get(teamclaim_key, PersistentDataType.STRING), ""));
        }
        return Component.text("");
    }

    public boolean trust(Chunk chunk, OfflinePlayer offlinePlayer){
        if(this.getTrustedList(chunk).contains(offlinePlayer)){
            return false;
        }

        List<OfflinePlayer> trustList = getTrustedList(chunk);
        trustList.add(offlinePlayer);
        chunk.getPersistentDataContainer().set(trusted_key, DataType.OFFLINE_PLAYER_ARRAY , trustList.toArray(new OfflinePlayer[0]));
        return true;
    }

    public boolean unTrust(Chunk chunk, OfflinePlayer offlinePlayer){
        if(!this.getTrustedList(chunk).contains(offlinePlayer)){
            return false;
        }

        List<OfflinePlayer> trustList = getTrustedList(chunk);
        trustList.remove(offlinePlayer);
        chunk.getPersistentDataContainer().set(trusted_key, DataType.OFFLINE_PLAYER_ARRAY , trustList.toArray(new OfflinePlayer[0]));
        return true;
    }

    public boolean canModify(Chunk chunk, OfflinePlayer offlinePlayer){
        if(this.isClaimed(chunk)){
            if(!this.getOwner(chunk).equals(offlinePlayer)){
                return this.getTrustedList(chunk).contains(offlinePlayer);
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
            if(this.getOwner(chunk).equals(offlinePlayer)){
                return forceUnClaimChunk(chunk);
            }
        }
        return false;
    }

    public boolean forceUnClaimChunk(Chunk chunk){
        if(this.isClaimed(chunk)){
            try {
                ExtraPlayerData extraPlayerData = getExtraPlayerData(getOwner(chunk));
                extraPlayerData.chunks.remove(getSerializableChunk(chunk));
                setExtraPlayerData(getOwner(chunk), extraPlayerData);
            }catch (Exception exception){
                exception.printStackTrace();
            }
            chunk.getPersistentDataContainer().remove(owner_key);
            chunk.getPersistentDataContainer().remove(trusted_key);
            chunk.getPersistentDataContainer().remove(teamclaim_key);

            return true;
        }
        return false;
    }

    public boolean canClaim(OfflinePlayer offlinePlayer){
        return getExtraPlayerData(offlinePlayer).chunks.size() < getExtraPlayerData(offlinePlayer).maxclaims;
    }

    public ExtraPlayerData getExtraPlayerData(OfflinePlayer offlinePlayer){
        File file = new File(plugin.playerDataFolder + "/"+ offlinePlayer.getUniqueId().toString() + ".json");
        if(!file.exists()){
            return new ExtraPlayerData(new HashSet<>(), plugin.getConfig().getInt("claim.max-claims"),false);
        }
        return ExtraPlayerData.loadData(file.getAbsolutePath());
    }

    public void setExtraPlayerData(OfflinePlayer offlinePlayer, ExtraPlayerData extraPlayerData){
        File file = new File(plugin.playerDataFolder + "/"+ offlinePlayer.getUniqueId().toString() + ".json");
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
