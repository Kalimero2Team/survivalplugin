package com.kalimero2.team.survivalplugin.enchantment;

import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.Field;

public class EnchantmentManager {
    public EnchantmentManager(SurvivalPlugin plugin){
        /* Doing Evil Stuff */
        try {
            Field f = org.bukkit.enchantments.Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
        } catch (Exception e) {
            e.printStackTrace();
        }


        BeheadingEnchantment enchantBeheading = new BeheadingEnchantment(NamespacedKey.minecraft("beheading"));
        Enchantment.registerEnchantment(enchantBeheading);
        plugin.getServer().getPluginManager().registerEvents(enchantBeheading,plugin);

    }
}
