package com.kalimero2.team.survivalplugin.util;

import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import de.jeff_media.morepersistentdatatypes.DataType;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;

public class ClaimManager implements Listener {

    private final NamespacedKey owner_key;
    private final NamespacedKey trusted_key;
    private final NamespacedKey teamclaim_key;
    private final SurvivalPlugin plugin;
    private final HashMap<Chunk, ClaimedChunk> chunkHashMap;

    public ClaimManager(SurvivalPlugin plugin){
        this.plugin = plugin;
        owner_key = new NamespacedKey(plugin, "owner");
        trusted_key = new NamespacedKey(plugin, "trusted");
        teamclaim_key = new NamespacedKey(plugin, "teamclaim");
        chunkHashMap = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this,plugin);
    }

    public ClaimedChunk getClaimedChunk(Chunk chunk){
        return getClaimedChunk(chunk, false);
    }

    public ClaimedChunk getClaimedChunk(Chunk chunk, boolean force){
        if(chunkHashMap.containsKey(chunk) && !force){
            return chunkHashMap.get(chunk);
        }else {
            if(chunk.getPersistentDataContainer().has(owner_key)){
                String ownerString = chunk.getPersistentDataContainer().get(owner_key, PersistentDataType.STRING);
                if(ownerString != null){
                    OfflinePlayer owner = plugin.getServer().getOfflinePlayer(UUID.fromString(ownerString));

                    if(chunk.getPersistentDataContainer().has(trusted_key, DataType.OFFLINE_PLAYER_ARRAY)){
                        List<OfflinePlayer> trusted =  Arrays.stream(chunk.getPersistentDataContainer().get(trusted_key, DataType.OFFLINE_PLAYER_ARRAY)).toList();
                        String teamClaim = chunk.getPersistentDataContainer().get(teamclaim_key, PersistentDataType.STRING);
                        ClaimedChunk claimedChunk = new ClaimedChunk(chunk, owner, new ArrayList<>(trusted), teamClaim);
                        chunkHashMap.put(chunk, claimedChunk);
                        return claimedChunk;
                    }
                }
            }
        }
        return new ClaimedChunk(chunk, null, null,null);
    }

    public void setClaimedChunk(ClaimedChunk claimedChunk, Chunk chunk){
        if(claimedChunk.getOwner() != null){
            chunk.getPersistentDataContainer().set(owner_key,PersistentDataType.STRING,claimedChunk.getOwner().getUniqueId().toString());
        }else {
            chunk.getPersistentDataContainer().remove(owner_key);
        }
        if(claimedChunk.getTrusted() != null){
            chunk.getPersistentDataContainer().set(trusted_key, DataType.OFFLINE_PLAYER_ARRAY , claimedChunk.getTrusted().toArray(new OfflinePlayer[0]));
        }else {
            chunk.getPersistentDataContainer().remove(trusted_key);
        }
        if(claimedChunk.getTeamClaim() != null){
            chunk.getPersistentDataContainer().set(teamclaim_key, DataType.OFFLINE_PLAYER_ARRAY , claimedChunk.getTrusted().toArray(new OfflinePlayer[0]));
        }else {
            chunk.getPersistentDataContainer().remove(teamclaim_key);
        }
        this.chunkHashMap.remove(chunk);
        this.chunkHashMap.put(chunk,claimedChunk);
    }

    public boolean claimChunk(Chunk chunk, OfflinePlayer offlinePlayer){
        if(!getClaimedChunk(chunk, true).isClaimed()){
            if(canClaim(offlinePlayer)){
                ClaimedChunk claimedChunk = new ClaimedChunk(chunk, offlinePlayer,null,null);
                setClaimedChunk(claimedChunk, chunk);
                return true;
            }
        }
        return false;
    }

    public boolean unClaimChunk(ClaimedChunk claimedChunk, OfflinePlayer offlinePlayer){
        if(claimedChunk.isClaimed() && claimedChunk.getOwner() != null && claimedChunk.getOwner().equals(offlinePlayer)){
            return forceUnClaimChunk(claimedChunk);
        }
        return false;
    }

    public boolean forceUnClaimChunk(ClaimedChunk claimedChunk){
        if(claimedChunk.isClaimed()){
            try {
                OfflinePlayer owner = claimedChunk.getOwner();
                if(owner != null){
                    ExtraPlayerData extraPlayerData = getExtraPlayerData(owner);
                    extraPlayerData.chunks.remove(getSerializableChunk(claimedChunk.getChunk()));
                    setExtraPlayerData(owner, extraPlayerData);
                }
            }catch (Exception exception){
                exception.printStackTrace();
            }
            setClaimedChunk(new ClaimedChunk(null,null,null,null), claimedChunk.getChunk());
            return true;
        }
        return false;
    }

    public boolean canClaim(OfflinePlayer offlinePlayer){
        return getExtraPlayerData(offlinePlayer).chunks.size() < getExtraPlayerData(offlinePlayer).maxclaims;
    }

    public boolean trust(ClaimedChunk claimedChunk, OfflinePlayer offlinePlayer){
        if(claimedChunk.getTrusted().contains(offlinePlayer)){
            return false;
        }

        List<OfflinePlayer> trustList = claimedChunk.getTrusted();
        trustList.add(offlinePlayer);
        setClaimedChunk(new ClaimedChunk(claimedChunk.getChunk(),claimedChunk.getOwner(),trustList,claimedChunk.getTeamClaim()),claimedChunk.getChunk());
        return true;
    }

    public boolean unTrust(ClaimedChunk claimedChunk, OfflinePlayer offlinePlayer){
        if(!claimedChunk.getTrusted().contains(offlinePlayer)){
            return false;
        }

        List<OfflinePlayer> trustList = claimedChunk.getTrusted();
        trustList.remove(offlinePlayer);
        setClaimedChunk(new ClaimedChunk(claimedChunk.getChunk(),claimedChunk.getOwner(),trustList,claimedChunk.getTeamClaim()),claimedChunk.getChunk());
        return true;
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event){
        ClaimedChunk claimedChunk = getClaimedChunk(event.getChunk());
        if(claimedChunk.isClaimed()){
            OfflinePlayer chunkOwner = claimedChunk.getOwner();
            if(chunkOwner != null){
                ExtraPlayerData extraPlayerData = getExtraPlayerData(chunkOwner);
                SerializableChunk serializableChunk = getSerializableChunk(event.getChunk());
                if(!extraPlayerData.chunks.contains(serializableChunk)){
                    plugin.getLogger().warning("Missing Claimed Chunk added to PlayerData. Chunk: "+serializableChunk.toString()+ " Chunk Owner: "+ chunkOwner.getName());
                    extraPlayerData.chunks.add(serializableChunk);
                    setExtraPlayerData(chunkOwner, extraPlayerData);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnLoad(ChunkUnloadEvent event){
        chunkHashMap.remove(event.getChunk());
    }
}
