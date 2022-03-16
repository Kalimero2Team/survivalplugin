package com.kalimero2.team.survivalplugin.listener;

import com.google.common.collect.Lists;
import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import com.kalimero2.team.survivalplugin.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
import org.bukkit.util.Vector;

import java.util.List;

public class ClaimListener implements Listener {
    private final ClaimManager claimManager;
    private SurvivalPlugin plugin;

    public ClaimListener(SurvivalPlugin plugin){
        this.plugin = plugin;
        this.claimManager = plugin.claimManager;
    }

    public boolean shouldCancel(Chunk chunk, Player player){
        ClaimedChunk claimedChunk = claimManager.getClaimedChunk(chunk);
        if(claimedChunk.isClaimed()){
            if(claimedChunk.getTeamClaim() != null){
                if(!claimedChunk.getTrusted().contains(player)){
                    return !player.hasPermission("chunk.team");
                }
            }
            if(claimedChunk.getOwner() != null && !claimedChunk.getOwner().equals(player)){
                return !claimedChunk.getTrusted().contains(player);
            }
        }
        return false;
    }

    private boolean hasSameOwner(Chunk originChunk, Chunk destChunk) {
        if(!originChunk.equals(destChunk)){
            ClaimedChunk origin = claimManager.getClaimedChunk(originChunk);
            ClaimedChunk dest = claimManager.getClaimedChunk(destChunk);

            if(origin.getOwner() != null && dest.getOwner() != null){
                return origin.getOwner().equals(dest.getOwner());
            }
            return (!origin.isClaimed() && !dest.isClaimed());
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event){
        Chunk toChunk = event.getTo().getChunk();
        Chunk fromChunk = event.getFrom().getChunk();
        if(fromChunk.equals(toChunk)){
            return;
        }
        ClaimedChunk to = claimManager.getClaimedChunk(toChunk);
        ClaimedChunk from = claimManager.getClaimedChunk(fromChunk);
        if(to.isClaimed()){
            if(to.getTeamClaim() != null && from.getTeamClaim() != null){
                if(from.getTeamClaim().equals(to.getTeamClaim())){
                    return;
                }
                Component message = MiniMessage.miniMessage().deserialize(to.getTeamClaim());
                event.getPlayer().sendActionBar(message);
                return;
            }

            if(to.getOwner() != null){
                String playername = to.getOwner().getName();
                TextComponent msg = Component.text().content("Grundstücksbesitzer: ").color(NamedTextColor.WHITE).append(Component.text(playername).color(NamedTextColor.GRAY)).build();
                event.getPlayer().sendActionBar(msg);
            }
        }
        
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event){
        if(shouldCancel(event.getBlock().getChunk(), event.getPlayer())){
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event){
        if(shouldCancel(event.getBlock().getChunk(), event.getPlayer())){
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerBucketFishEvent(PlayerBucketEntityEvent event){
        if(shouldCancel(event.getEntity().getLocation().getChunk(), event.getPlayer())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event){
        ClaimedChunk claimedChunk = claimManager.getClaimedChunk(event.getEntity().getChunk());
        if(claimedChunk != null && claimedChunk.getTeamClaim() != null){
            if(event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
                event.setCancelled(true);
            }if(event.getEntity() instanceof Player){
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
                    if(shouldCancel(event.getEntity().getChunk(), player)){
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
            if(shouldCancel(event.getEntity().getLocation().getChunk(), player)){
                event.setCancelled(true);
                return;
            }
        }

        ClaimedChunk claimedChunk = claimManager.getClaimedChunk(event.getEntity().getChunk());
        if(claimedChunk.getTeamClaim() != null){
            if(shouldCancel(event.getEntity().getChunk(), player)){
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onLecternBookEvent(PlayerTakeLecternBookEvent event){
        if(shouldCancel(event.getLectern().getChunk(), event.getPlayer())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteractEvent(PlayerInteractEvent event){
        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            if(event.getClickedBlock() != null && shouldCancel(event.getClickedBlock().getChunk(), event.getPlayer())){
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
            if(event.getClickedBlock() != null && event.getClickedBlock().getType().equals(Material.FARMLAND)){
                if(shouldCancel(event.getClickedBlock().getChunk(), event.getPlayer())){
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if(event.getClickedBlock() != null){
            ClaimedChunk claimedChunk = claimManager.getClaimedChunk(event.getClickedBlock().getChunk());
            if(claimedChunk != null && claimedChunk.getTeamClaim() != null){
                if(shouldCancel(event.getPlayer().getChunk(), event.getPlayer())){
                    if(!event.getAction().equals(Action.RIGHT_CLICK_AIR)){
                        event.setCancelled(true);
                    }
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
            if(shouldCancel(event.getTo().getChunk(), event.getPlayer())){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent breakEvent) {
        Player player = breakEvent.getPlayer();
        Block block = breakEvent.getBlock();

        if(shouldCancel(block.getChunk(), player)){
            breakEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlocksPlace(BlockMultiPlaceEvent placeEvent) {
        Player player = placeEvent.getPlayer();
        for(BlockState blockState:placeEvent.getReplacedBlockStates()){
            if(shouldCancel(blockState.getChunk(), player)){
                placeEvent.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent placeEvent) {
        Player player = placeEvent.getPlayer();
        Block block = placeEvent.getBlock();
        if(shouldCancel(block.getChunk(), player)){
            placeEvent.setCancelled(true);
        }

    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event){
        ClaimedChunk claimedChunk = claimManager.getClaimedChunk(event.getEntity().getChunk());
        if(event.getRemover() instanceof Player player){
            if(shouldCancel(event.getEntity().getChunk(), player)){
                event.setCancelled(true);
            }
        }else if (event.getRemover() instanceof Projectile projectile){
            if(projectile.getShooter() instanceof Player player){
                if(shouldCancel(event.getEntity().getChunk(), player)){
                    event.setCancelled(true);
                }
            }else{
                if(claimedChunk.isClaimed()){
                    event.setCancelled(true);
                }
            }
        }else if(event.getRemover() instanceof Creeper){
            if(claimedChunk.isClaimed()){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event){
        if(shouldCancel(event.getEntity().getChunk(), event.getPlayer())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event){
        if(shouldCancel(event.getRightClicked().getChunk(), event.getPlayer())){
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onEntityBlockBreak(EntityChangeBlockEvent event){
        ClaimedChunk claimedChunk = claimManager.getClaimedChunk(event.getEntity().getChunk());
        if(claimedChunk.isClaimed()){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event){
        List<Block> blocks = event.getBlocks();

        Vector vector = event.getDirection().getDirection();

        ClaimedChunk originChunk = claimManager.getClaimedChunk(event.getBlock().getChunk());
        ClaimedChunk destChunk = claimManager.getClaimedChunk(event.getBlock().getLocation().add(vector).getBlock().getChunk());

        if(originChunk.equals(destChunk)){
            return;
        }

        if(!hasSameOwner(originChunk.getChunk(), destChunk.getChunk())){
            event.setCancelled(true);
        }

        if(originChunk.isClaimed() && originChunk.getOwner() != null){
            OfflinePlayer owner = originChunk.getOwner();
            for(Block block:blocks){
                ClaimedChunk claimedChunk = claimManager.getClaimedChunk(block.getChunk());
                if(claimedChunk.isClaimed()){
                    if(claimedChunk.getOwner() != null){
                        if(!owner.equals(claimedChunk.getOwner())){
                            event.setCancelled(true);
                        }
                    }
                }
                Block block1 = block.getLocation().add(vector).getBlock();
                ClaimedChunk claimedChunk1 = claimManager.getClaimedChunk(block1.getChunk());
                if(claimedChunk1.isClaimed()){
                    if(claimedChunk1.getOwner() != null){
                        if(!owner.equals(claimedChunk1.getOwner())){
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }else {
            for(Block block: blocks){
                if(claimManager.getClaimedChunk(block.getChunk()).isClaimed()){
                    event.setCancelled(true);
                }
                Block block1 = block.getLocation().add(vector).getBlock();
                if(claimManager.getClaimedChunk(block1.getChunk()).isClaimed()){
                    event.setCancelled(true);
                }
            }

        }
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event){
        List<Block> blocks = event.getBlocks();
        ClaimedChunk originChunk = claimManager.getClaimedChunk(event.getBlock().getChunk());
        if(originChunk.isClaimed() && originChunk.getOwner() != null){
            OfflinePlayer owner = originChunk.getOwner();
            for(Block block:blocks){
                ClaimedChunk claimedChunk = claimManager.getClaimedChunk(block.getChunk());
                if(claimedChunk.isClaimed()){
                    if(claimedChunk.getOwner() != null){
                        if(!owner.equals(claimedChunk.getOwner())){
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }else {
            for(Block block: blocks){
                if(claimManager.getClaimedChunk(block.getChunk()).isClaimed()){
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event){
        if(!hasSameOwner(event.getToBlock().getChunk(),event.getBlock().getChunk())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if(event.getEntity().getType() == EntityType.ENDERMAN){
            event.setCancelled(true);
        }else if(event.getEntity().getType() == EntityType.BOAT){
            if(claimManager.getClaimedChunk(event.getBlock().getChunk()).isClaimed()){
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onEntityInteract(EntityInteractEvent event){
        if(claimManager.getClaimedChunk(event.getBlock().getChunk()).isClaimed()){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent event){
        if (event.getBlock().getBlockData() instanceof Directional directional) {
            Chunk originChunk = event.getBlock().getChunk();
            Chunk destChunk = event.getBlock().getLocation().add(directional.getFacing().getDirection()).getChunk();

            if(!hasSameOwner(originChunk, destChunk)){
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event){
        Chunk originChunk = event.getSource().getChunk();
        Chunk destChunk = event.getBlock().getChunk();

        if(!hasSameOwner(originChunk, destChunk)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFertilize(BlockFertilizeEvent event){
        Chunk originChunk = event.getBlock().getChunk();

        for(BlockState block:event.getBlocks()){
            Chunk destChunk = block.getChunk();

            if(!hasSameOwner(originChunk, destChunk)){
                event.setCancelled(true);
            }
        }
    }


}
