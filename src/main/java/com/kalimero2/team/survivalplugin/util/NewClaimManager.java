package com.kalimero2.team.survivalplugin.util;

import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import de.jeff_media.morepersistentdatatypes.DataType;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class NewClaimManager implements Listener {

    private final NamespacedKey owner_key;
    private final NamespacedKey trusted_key;
    private final NamespacedKey teamclaim_key;
    private final SurvivalPlugin plugin;
    private final HashMap<Chunk, ClaimedChunk> chunkHashMap;

    public NewClaimManager(SurvivalPlugin plugin){
        this.plugin = plugin;
        owner_key = new NamespacedKey(plugin, "owner");
        trusted_key = new NamespacedKey(plugin, "trusted");
        teamclaim_key = new NamespacedKey(plugin, "teamclaim");
        chunkHashMap = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this,plugin);
    }

    public boolean isClaimed(Chunk chunk){
        return chunk.getPersistentDataContainer().has(owner_key);
    }

    @Nullable
    public ClaimedChunk getClaimedChunk(Chunk chunk){
        return chunkHashMap.get(chunk);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event){
        Chunk chunk = event.getChunk();
        if(isClaimed(chunk) && !chunkHashMap.containsKey(chunk)){
            String ownerString = chunk.getPersistentDataContainer().get(owner_key, PersistentDataType.STRING);
            if(ownerString != null){
                OfflinePlayer owner = plugin.getServer().getOfflinePlayer(UUID.fromString(ownerString));

                if(chunk.getPersistentDataContainer().has(trusted_key, DataType.OFFLINE_PLAYER_ARRAY)){
                    List<OfflinePlayer> trusted =  Arrays.stream(chunk.getPersistentDataContainer().get(trusted_key, DataType.OFFLINE_PLAYER_ARRAY)).toList();
                    String teamClaim = chunk.getPersistentDataContainer().get(teamclaim_key, PersistentDataType.STRING);
                    ClaimedChunk claimedChunk = new ClaimedChunk(chunk, owner, new ArrayList<>(trusted), teamClaim);
                    chunkHashMap.put(chunk, claimedChunk);
                }
            }
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event){
        Chunk chunk = event.getChunk();
        chunkHashMap.remove(chunk);
    }

    /*


    @EventHandler(priority = EventPriority.MONITOR,ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event){
        if(claimManager.isClaimed(event.getChunk())){
            OfflinePlayer chunkOwner = claimManager.getOwner(event.getChunk());
            ExtraPlayerData extraPlayerData = claimManager.getExtraPlayerData(chunkOwner);
            SerializableChunk serializableChunk = claimManager.getSerializableChunk(event.getChunk());
            if(!extraPlayerData.chunks.contains(serializableChunk)){
                plugin.getLogger().warning("Missing Claimed Chunk added to PlayerData. Chunk: "+serializableChunk.toString()+ " Chunk Owner: "+ chunkOwner.getName());
                extraPlayerData.chunks.add(serializableChunk);
                claimManager.setExtraPlayerData(chunkOwner, extraPlayerData);
            }
        }
    }
     */
}
