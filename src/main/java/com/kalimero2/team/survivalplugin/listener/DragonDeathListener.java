package com.kalimero2.team.survivalplugin.listener;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class DragonDeathListener implements Listener {

    @EventHandler
    public void onDragonDeath(EntityDeathEvent event){
        if(event.getEntity().getType().equals(EntityType.ENDER_DRAGON)){
            event.getDrops().add(new ItemStack(Material.DRAGON_HEAD));
        }
    }
}
