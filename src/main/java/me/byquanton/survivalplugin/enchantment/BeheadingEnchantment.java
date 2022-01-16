package me.byquanton.survivalplugin.enchantment;

import com.google.common.collect.Lists;
import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class BeheadingEnchantment extends Enchantment implements Listener {

    public BeheadingEnchantment(@NotNull NamespacedKey key) {
        super(key);
    }

    @Override
    public @NotNull String getName() {
        return "beheading";
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getStartLevel() {
        return 0;
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return EnchantmentTarget.WEAPON.includes(itemStack);
    }

    @Override
    public @NotNull Component displayName(int i) {
        return Component.text("Beheading").append(Component.text(i));
    }

    @Override
    public boolean isTradeable() {
        return true;
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return EnchantmentRarity.VERY_RARE;
    }

    @Override
    public float getDamageIncrease(int i, @NotNull EntityCategory entityCategory) {
        return 0;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        Set<EquipmentSlot> activeslots = new HashSet<>();
        activeslots.add(EquipmentSlot.HAND);
        activeslots.add(EquipmentSlot.OFF_HAND);
        return activeslots;
    }

    private ItemStack fixLore(ItemStack itemStack){
        ItemStack item = itemStack.clone();

        if(item.hasItemMeta()){
            TextComponent component = Component.text("Beheading I").decoration(TextDecoration.ITALIC,false).color(TextColor.color(168, 168, 168));
            if(item.lore() != null){
                if(item.lore().contains(component)){
                    item.addUnsafeEnchantment(this, 1);
                }
            }
            if(item.getItemMeta().hasEnchant(this)){
                item.lore(Lists.newArrayList(component));
            }else if(item.getType().equals(Material.ENCHANTED_BOOK)){
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
                if(meta.hasStoredEnchant(this)){
                    item.lore(Lists.newArrayList(component));
                }
            }
        }
        return item;
    }

    @EventHandler
    public void onAnvilPrepareEvent(PrepareAnvilEvent event){

        AnvilInventory inventory = event.getInventory();
        try{
            if(this.canEnchantItem(Objects.requireNonNull(inventory.getFirstItem()))){
                if(inventory.getSecondItem().containsEnchantment(this)){
                    ItemStack result = inventory.getFirstItem().clone();
                    result.addEnchantments(inventory.getSecondItem().getEnchantments());
                    inventory.setRepairCost(1);
                    event.setResult(fixLore(result));
                }else if(inventory.getSecondItem().getType().equals(Material.ENCHANTED_BOOK)){
                    EnchantmentStorageMeta meta = (EnchantmentStorageMeta) inventory.getSecondItem().getItemMeta();
                    if(meta.hasStoredEnchant(this)){
                        ItemStack result = inventory.getFirstItem().clone();
                        result.addEnchantment(this,1);
                        inventory.setRepairCost(1);
                        event.setResult(fixLore(result));
                    }
                }
            }
        }catch(NullPointerException ignored){

        }
    }

    @EventHandler
    public void onItemSelect(PlayerItemHeldEvent event){
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        event.getPlayer().getInventory().setItemInMainHand(fixLore(item));
    }
    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event){
        event.getEntity().setItemStack(fixLore(event.getEntity().getItemStack()));
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event){
        LivingEntity mob = event.getEntity();
        Player player = mob.getKiller();
        if(player == null)
            return;
        if(!player.getInventory().getItemInMainHand().hasItemMeta())
            return;
        else if(player.getInventory().getItemInMainHand().getItemMeta().hasEnchant(this)){
            if(mob instanceof Creeper){
                event.getDrops().add(new ItemStack(Material.CREEPER_HEAD));
            }else if (mob instanceof EnderDragon){
                event.getDrops().add(new ItemStack(Material.DRAGON_HEAD));
            }else if (mob instanceof Skeleton){
                event.getDrops().add(new ItemStack(Material.SKELETON_SKULL));
            }else if (mob instanceof Wither){
                event.getDrops().add(new ItemStack(Material.WITHER_SKELETON_SKULL));
                event.getDrops().add(new ItemStack(Material.WITHER_SKELETON_SKULL));
                event.getDrops().add(new ItemStack(Material.WITHER_SKELETON_SKULL));
            }else if (mob instanceof WitherSkeleton){
                event.getDrops().add(new ItemStack(Material.WITHER_SKELETON_SKULL));
            }else if (mob instanceof Zombie){
                event.getDrops().add(new ItemStack(Material.ZOMBIE_HEAD));
            }else if (mob instanceof Player){
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                meta.setPlayerProfile(((Player)mob).getPlayerProfile());
                head.setItemMeta(meta);
                event.getDrops().add(head);
            }
        }

    }

    @Override
    public @NotNull String translationKey() {
        return "minecraft.beheading";
    }
}
