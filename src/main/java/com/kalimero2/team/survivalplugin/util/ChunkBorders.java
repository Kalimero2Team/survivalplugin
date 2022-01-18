package com.kalimero2.team.survivalplugin.util;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

public class ChunkBorders implements Listener {

    public static ArrayList<Player> show_border = new ArrayList<>();
    
    @EventHandler()
    public void onJoin(PlayerJoinEvent event){
        show_border.remove(event.getPlayer());
    }

    @EventHandler()
    public void onQuit(PlayerQuitEvent event){
        show_border.remove(event.getPlayer());
    }

    public ChunkBorders(Plugin plugin){
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> show_border.forEach(ChunkBorders::render_particles), 0L, 1L);
    }

    private static void render_particles(Player player){
        Chunk chunk = player.getLocation().getChunk();
        for(int i = 0; i < 16; i++){
            player.spawnParticle(Particle.NOTE,chunk.getBlock(i,(int) player.getLocation().getY()+1,0).getLocation(),1);
            player.spawnParticle(Particle.NOTE,chunk.getBlock(i,(int) player.getLocation().getY()+1,15).getLocation().add(0,0,1),1);
            player.spawnParticle(Particle.NOTE,chunk.getBlock(0,(int) player.getLocation().getY()+1,i).getLocation(),1);
            player.spawnParticle(Particle.NOTE,chunk.getBlock(15,(int) player.getLocation().getY()+1,i).getLocation().add(1,0,0),1);
        }
    }
}
