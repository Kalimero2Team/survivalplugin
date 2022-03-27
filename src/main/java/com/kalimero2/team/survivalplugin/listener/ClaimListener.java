package com.kalimero2.team.survivalplugin.listener;

import com.google.common.collect.Lists;
import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import com.kalimero2.team.survivalplugin.util.ClaimManager;
import com.kalimero2.team.survivalplugin.util.ExtraPlayerData;
import com.kalimero2.team.survivalplugin.util.SerializableChunk;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.util.Vector;

import java.util.List;

public class ClaimListener implements Listener {
    private final ClaimManager claimManager;
    private SurvivalPlugin plugin;

    public ClaimListener(SurvivalPlugin plugin){
        this.plugin = plugin;
        this.claimManager = plugin.claimManager;
    }

    public boolean shouldcancel(Chunk chunk, Player player){
        if(claimManager.isClaimed(chunk)){
            if(claimManager.isTeamClaim(chunk)){
                if(!claimManager.getTrustedList(chunk).contains(player)){
                    return !player.hasPermission("chunk.team");
                }
            }
            if(!claimManager.getOwner(chunk).equals(player)){
                return !claimManager.getTrustedList(chunk).contains(player);
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event){
        Chunk toChunk = event.getTo().getChunk();
        if(event.getFrom().getChunk().equals(toChunk)){
            return;
        }
        if(claimManager.isClaimed(toChunk)){

            if(claimManager.isTeamClaim(toChunk)){
                Component message = claimManager.getTeamClaimMessage(toChunk);
                if(claimManager.isTeamClaim(event.getFrom().getChunk())){
                    if(claimManager.getTeamClaimMessage(event.getFrom().getChunk()).equals(message)){
                        return;
                    }
                }

                event.getPlayer().sendActionBar(message);
                return;
            }

            String playername = claimManager.getOwner(toChunk).getName();
            TextComponent msg = Component.text().content("Grundstücksbesitzer: ").color(NamedTextColor.WHITE).append(Component.text(playername).color(NamedTextColor.GRAY)).build();
            event.getPlayer().sendActionBar(msg);
        }
        
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event){
        if(shouldcancel(event.getBlock().getChunk(), event.getPlayer())){
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event){
        if(shouldcancel(event.getBlock().getChunk(), event.getPlayer())){
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerBucketFishEvent(PlayerBucketEntityEvent event){
        if(shouldcancel(event.getEntity().getLocation().getChunk(), event.getPlayer())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event){
        if(event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)){
            if(claimManager.isTeamClaim(event.getEntity().getChunk())){
                event.setCancelled(true);
            }
        }

        if(event.getEntity() instanceof Player player){
            if(claimManager.isTeamClaim(player.getChunk())){
                if(List.of(EntityDamageEvent.DamageCause.FALL, EntityDamageEvent.DamageCause.MAGIC).contains(event.getCause())){
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Player player){
            onEntityDamageByPlayer(event, player);
        }else{
            if(event.getDamager() instanceof Projectile projectile){
                if(event.getEntity() instanceof Hanging){
                    event.setCancelled(true);
                }
                if(projectile.getShooter() instanceof Player player){
                    if(shouldcancel(event.getEntity().getChunk(), player)){
                        onEntityDamageByPlayer(event, player);
                    }
                }
            }
        }
    }

    private void onEntityDamageByPlayer(EntityDamageByEntityEvent event, Player player) {
        if(event.getEntity() instanceof Player || event.getEntity() instanceof Monster){
            event.setCancelled(false);
            return;
        }else if(event.getEntity() instanceof Animals || event.getEntity() instanceof Tameable || event.getEntity() instanceof NPC || event.getEntity() instanceof Hanging || event.getEntity() instanceof ArmorStand){
            if(shouldcancel(event.getEntity().getLocation().getChunk(), player)){
                event.setCancelled(true);
                return;
            }
        }

        if(claimManager.isTeamClaim(event.getEntity().getChunk())){
            if(shouldcancel(event.getEntity().getChunk(), player)){
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onLecternBookEvent(PlayerTakeLecternBookEvent event){
        if(shouldcancel(event.getLectern().getChunk(), event.getPlayer())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteractEvent(PlayerInteractEvent event){
        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            if(shouldcancel(event.getClickedBlock().getChunk(), event.getPlayer())){
                Material material = event.getClickedBlock().getType();
                List<Material> protected_materials = Lists.newArrayList(Material.CHEST,Material.TRAPPED_CHEST,Material.BARREL,Material.SHULKER_BOX,Material.WHITE_SHULKER_BOX,Material.ORANGE_SHULKER_BOX,Material.MAGENTA_SHULKER_BOX,Material.LIGHT_BLUE_SHULKER_BOX,Material.YELLOW_SHULKER_BOX,Material.LIME_SHULKER_BOX,Material.PINK_SHULKER_BOX,Material.GRAY_SHULKER_BOX,Material.LIGHT_GRAY_SHULKER_BOX,Material.CYAN_SHULKER_BOX,Material.PURPLE_SHULKER_BOX,Material.BLUE_SHULKER_BOX,Material.BROWN_SHULKER_BOX,Material.GREEN_SHULKER_BOX,Material.RED_SHULKER_BOX,Material.BLACK_SHULKER_BOX,Material.FURNACE,Material.BLAST_FURNACE,Material.SMOKER,Material.BREWING_STAND,Material.DAMAGED_ANVIL,Material.JUKEBOX,Material.HOPPER,Material.DROPPER,Material.DISPENSER,Material.CAULDRON,Material.NOTE_BLOCK,Material.BEACON,Material.COMPARATOR,Material.REPEATER,Material.REDSTONE);

                if(protected_materials.contains(material)){
                    event.setCancelled(true);
                    return;
                }
                if(event.getPlayer().getItemInUse() != null) {
                    if (event.getPlayer().getItemInUse().getType().equals(Material.POWDER_SNOW_BUCKET)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }else if(event.getAction().equals(Action.PHYSICAL)){
            if(event.getClickedBlock() != null){
                if(event.getClickedBlock().getType().equals(Material.FARMLAND)){
                    if(shouldcancel(event.getClickedBlock().getChunk(), event.getPlayer())){
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        if(claimManager.isTeamClaim(event.getPlayer().getChunk())){
            if(shouldcancel(event.getPlayer().getChunk(), event.getPlayer())){
                if(!event.getAction().equals(Action.RIGHT_CLICK_AIR)){
                    event.setCancelled(true);
                }
            }
        }

    }

    @EventHandler
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if(event.getCause().equals(PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT)){
            if(shouldcancel(event.getTo().getChunk(), event.getPlayer())){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent breakEvent) {
        Player player = breakEvent.getPlayer();
        Block block = breakEvent.getBlock();

        if(shouldcancel(block.getChunk(), player)){
            breakEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlocksPlace(BlockMultiPlaceEvent placeEvent) {
        Player player = placeEvent.getPlayer();
        for(BlockState blockState:placeEvent.getReplacedBlockStates()){
            if(shouldcancel(blockState.getChunk(), player)){
                placeEvent.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent placeEvent) {
        Player player = placeEvent.getPlayer();
        Block block = placeEvent.getBlock();
        if(shouldcancel(block.getChunk(), player)){
            placeEvent.setCancelled(true);
        }

    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event){
        if(event.getRemover() instanceof Player player){
            if(shouldcancel(event.getEntity().getChunk(), player)){
                event.setCancelled(true);
            }
        }else if (event.getRemover() instanceof Projectile projectile){
            if(projectile.getShooter() instanceof Player player){
                if(shouldcancel(event.getEntity().getChunk(), player)){
                    event.setCancelled(true);
                }
            }else{
                if(claimManager.isClaimed(event.getEntity().getChunk())){
                    event.setCancelled(true);
                }
            }
        }else if(event.getRemover() instanceof Creeper){
            if(claimManager.isClaimed(event.getEntity().getChunk())){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event){
        if(shouldcancel(event.getEntity().getChunk(), event.getPlayer())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event){
        if(shouldcancel(event.getRightClicked().getChunk(), event.getPlayer())){
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event){
        List<Block> blocks = event.getBlocks();

        Vector vector = event.getDirection().getDirection();

        Chunk originChunk = event.getBlock().getChunk();
        Chunk destChunk = event.getBlock().getLocation().add(vector).getBlock().getChunk();
        if(claimManager.isClaimed(destChunk)){
            if(claimManager.isClaimed(originChunk)){
                if(!claimManager.getOwner(destChunk).getUniqueId().equals(claimManager.getOwner(originChunk).getUniqueId())){
                    event.setCancelled(true);
                }
            }else {
                event.setCancelled(true);
            }
        }

        if(claimManager.isClaimed(event.getBlock().getChunk())){
            OfflinePlayer owner = claimManager.getOwner(event.getBlock().getChunk());
            for(Block block:blocks){
                if(claimManager.isClaimed(block.getChunk())){
                    if(claimManager.getOwner(block.getChunk()) != null){
                        if(!owner.getUniqueId().equals(claimManager.getOwner(block.getChunk()).getUniqueId())){
                            event.setCancelled(true);
                        }
                    }
                }
                Block block1 = block.getLocation().add(vector).getBlock();
                if(claimManager.isClaimed(block1.getChunk())){
                    if(claimManager.getOwner(block1.getChunk()) != null){
                        if(!owner.getUniqueId().equals(claimManager.getOwner(block1.getChunk()).getUniqueId())){
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }else {
            for(Block block: blocks){
                if(claimManager.isClaimed(block.getChunk())){
                    event.setCancelled(true);
                }
                Block block1 = block.getLocation().add(vector).getBlock();

                if(claimManager.isClaimed(block1.getChunk())){
                    event.setCancelled(true);
                }
            }

        }
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event){
        List<Block> blocks = event.getBlocks();
        if(claimManager.isClaimed(event.getBlock().getChunk())){
            OfflinePlayer owner = claimManager.getOwner(event.getBlock().getChunk());
            for(Block block:blocks){
                if(claimManager.isClaimed(block.getChunk())){
                    if(claimManager.getOwner(block.getChunk()) != null){
                        if(!owner.getUniqueId().equals(claimManager.getOwner(block.getChunk()).getUniqueId())){
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }else {
            for(Block block: blocks){
                if(claimManager.isClaimed(block.getChunk())){
                    event.setCancelled(true);
                }
            }
        }
    }


    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event){
        Chunk from_chunk = event.getBlock().getChunk();
        Chunk to_chunk = event.getToBlock().getChunk();

        if(from_chunk.equals(to_chunk)){
            return;
        }

        if(claimManager.isClaimed(from_chunk)){
            if(claimManager.isClaimed(to_chunk)){
                if(!claimManager.getOwner(from_chunk).getUniqueId().equals(claimManager.getOwner(to_chunk).getUniqueId())){
                    event.setCancelled(true);
                }
            }
        }else {
            if(claimManager.isClaimed(to_chunk)){
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if(event.getEntity().getType() == EntityType.ENDERMAN){
            event.setCancelled(true);
        }else if(event.getEntity().getType() == EntityType.BOAT){
            if(claimManager.isClaimed(event.getBlock().getChunk())){
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onEntityInteract(EntityInteractEvent event){
        if(claimManager.isClaimed(event.getBlock().getChunk())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent event){
        if (event.getBlock().getBlockData() instanceof Directional directional) {
            Chunk originChunk = event.getBlock().getChunk();
            Chunk destChunk = event.getBlock().getLocation().add(directional.getFacing().getDirection()).getChunk();

            if(hasSameOwner(originChunk, destChunk)){
                event.setCancelled(true);
            }
        }
    }

    private boolean hasSameOwner(Chunk originChunk, Chunk destChunk) {
        if(!originChunk.equals(destChunk)){
            if(claimManager.isClaimed(destChunk)){
                if(claimManager.isClaimed(originChunk)){
                    if(!claimManager.getOwner(destChunk).getUniqueId().equals(claimManager.getOwner(originChunk).getUniqueId())){
                        return true;
                    }
                }else {
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event){
        Chunk originChunk = event.getSource().getChunk();
        Chunk destChunk = event.getBlock().getChunk();

        if(hasSameOwner(originChunk, destChunk)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFertilize(BlockFertilizeEvent event){
        Chunk originChunk = event.getBlock().getChunk();

        for(BlockState block:event.getBlocks()){
            Chunk destChunk = block.getChunk();

            if(hasSameOwner(originChunk, destChunk)){
                event.setCancelled(true);
            }
        }
    }


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

}
