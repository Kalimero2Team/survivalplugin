package com.kalimero2.team.survivalplugin.recipe;

import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class CustomRecipes implements Listener {

    private final SurvivalPlugin plugin;

    public CustomRecipes(SurvivalPlugin plugin){
        this.plugin = plugin;
        plugin.getServer().addRecipe(getElytraRecipe());
        plugin.getServer().getPluginManager().registerEvents(this,plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        if(!event.getPlayer().hasDiscoveredRecipe(getElytraRecipe().getKey())){
            event.getPlayer().discoverRecipe(getElytraRecipe().getKey());
        }
    }

    public ShapedRecipe getElytraRecipe(){
        ItemStack eltrya = new ItemStack(Material.ELYTRA);
        NamespacedKey elytraKey = new NamespacedKey(plugin,"elytra");
        ShapedRecipe elytraRecipe = new ShapedRecipe(elytraKey, eltrya);
        elytraRecipe.shape("SPS","PNP","S#S");
        elytraRecipe.setIngredient('S', Material.SHULKER_SHELL);
        elytraRecipe.setIngredient('P', Material.PHANTOM_MEMBRANE);
        elytraRecipe.setIngredient('N', Material.NETHERITE_INGOT);
        elytraRecipe.setIngredient('#', Material.AIR);
        return elytraRecipe;
    }


}
