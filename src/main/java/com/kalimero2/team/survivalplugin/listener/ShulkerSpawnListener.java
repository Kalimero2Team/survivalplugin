package com.kalimero2.team.survivalplugin.listener;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.Random;


public class ShulkerSpawnListener implements Listener {

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event){
        if(event.getEntity() instanceof Enderman enderman) {
            Location location = enderman.getLocation();
            if(location.getWorld().getEnvironment() == World.Environment.THE_END){
                if(location.getBlock().getBiome() == Biome.END_HIGHLANDS || location.getBlock().getBiome() == Biome.END_MIDLANDS) {
                    if(enderman.getNearbyEntities(50,50,50).stream().filter(entity -> entity.getType().equals(EntityType.SHULKER)).toList().size() <= 1){
                        event.setCancelled(true);
                        Shulker shulker = location.getWorld().spawn(location, Shulker.class);
                        Random random = new Random();
                        shulker.setColor(DyeColor.values()[random.nextInt(DyeColor.values().length)]);
                    }
                }
            }
        }
    }


}
