package com.kalimero2.team.survivalplugin.listener;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import com.kalimero2.team.survivalplugin.discord.DiscordBot;
import com.kalimero2.team.survivalplugin.util.ExtraPlayerData;
import de.jeff_media.morepersistentdatatypes.DataType;
import io.papermc.paper.event.player.AsyncChatEvent;
import com.kalimero2.team.survivalplugin.database.MongoDB;
import com.kalimero2.team.survivalplugin.database.pojo.MinecraftUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;

public class MainListener implements Listener {

    private final MongoDB database;
    private final DiscordBot discordBot;
    private final SurvivalPlugin plugin;
    private final Random random;

    public MainListener(SurvivalPlugin plugin){
        this.database = plugin.getDatabase();
        this.discordBot = plugin.getDiscordBot();
        this.random = new Random();
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        ExtraPlayerData extraPlayerData = plugin.claimManager.getExtraPlayerData(player);

        String header_string = plugin.getConfig().getString("tab.header");

        if(header_string != null){
            player.sendPlayerListHeader(MiniMessage.miniMessage().deserialize(header_string));
        }

        String footer_string = plugin.getConfig().getString("tab.footer");

        if(footer_string != null){
            player.sendPlayerListFooter(MiniMessage.miniMessage().deserialize(footer_string));
        }

        Component playerlistname ;
        if(player.hasPermission("team")){
            playerlistname = Component.text("Team ").color(NamedTextColor.DARK_AQUA).append(player.name().color(NamedTextColor.WHITE));
        }else if(extraPlayerData.vip){
            playerlistname = Component.text("VIP ").color(NamedTextColor.GOLD).append(player.name().color(NamedTextColor.WHITE));
        }else {
            playerlistname = Component.text("Spieler ").color(NamedTextColor.GRAY).append(player.name().color(NamedTextColor.WHITE));
        }
        player.playerListName(playerlistname);
        player.displayName(null);

        Component joinmessage = Component.text("[+] ").color(NamedTextColor.GREEN).append(Component.text(player.getName()).color(NamedTextColor.WHITE));

        if(plugin.getServer().getPluginManager().isPluginEnabled("floodgate")){
            if(plugin.floodgateApi.isFloodgatePlayer(player.getUniqueId())){
                joinmessage = joinmessage.append(Component.text(" (Bedrock)").color(NamedTextColor.GRAY));
            }
        }

        if(player.isDead()){
            player.spigot().respawn();
        }

        event.joinMessage(joinmessage);
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        Component quitmessage = Component.text("[-] ").color(NamedTextColor.RED).append(Component.text(player.getName()).color(NamedTextColor.WHITE));
        event.quitMessage(quitmessage);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event){
        NamespacedKey key = new NamespacedKey(plugin,"muted");
        if(plugin.chatMute){
            if(!event.getPlayer().hasPermission("mute-bypass")){
                event.setCancelled(true);
            }
        }
        if(event.getPlayer().getPersistentDataContainer().has(key, DataType.BOOLEAN)){
            if(event.getPlayer().getPersistentDataContainer().get(key, DataType.BOOLEAN)){
                event.setCancelled(true);
            }
        }
        event.renderer((source, sourceDisplayName, message, viewer) -> {
            Component prefix = Component.text("Spieler ").color(NamedTextColor.GRAY);
            ExtraPlayerData extraPlayerData = plugin.claimManager.getExtraPlayerData(source);
            if(source.hasPermission("team")){
                prefix = Component.text("Team ").color(NamedTextColor.DARK_AQUA);
            }else if(extraPlayerData.vip){
                prefix = Component.text("VIP ").color(NamedTextColor.GOLD);
            }

            return MiniMessage.miniMessage().deserialize("<prefix><player> <bold>»</bold> <message>",Placeholder.component("prefix", prefix),Placeholder.unparsed("player", event.getPlayer().getName()), Placeholder.component("message",message));

        });
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        Component component = event.deathMessage();
        if(component != null){
            event.deathMessage(Component.text("☠ ").append(component));
            if(event.getPlayer().getWorld().getEnvironment().equals(World.Environment.THE_END)){
                if(event.getPlayer().getWorld().getNearbyEntitiesByType(EnderDragon.class, event.getPlayer().getLocation(), 1000).size() >= 1){
                    event.setKeepInventory(true);
                    event.setKeepLevel(false);
                    event.getDrops().clear();
                }
            }
        }
    }

    @EventHandler
    public void onServerListPing(PaperServerListPingEvent event){
        event.motd(plugin.motd);
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event){
        List<MinecraftUser> list = database.getUsers();
        PlayerProfile player = event.getPlayerProfile();

        if(player.getId() == null){
            return;
        }

        MinecraftUser minecraftUser;

        Optional<MinecraftUser> minecraftUserOptional = list.stream().filter(user -> Objects.equals(user.getUuid(), player.getId().toString())).findAny();

        if(minecraftUserOptional.isEmpty()) {
            minecraftUser = database.getUser(player.getId());
            boolean bedrock = plugin.floodgateApi.isFloodgatePlayer(player.getId());
            if(minecraftUser == null){
                minecraftUser = new MinecraftUser(player.getId().toString(), player.getName(), bedrock, false);
                database.addUser(minecraftUser);
            }

        }else {
            minecraftUser = minecraftUserOptional.get();
        }

        if(minecraftUser.isRulesAccepted()){
            if(!discordBot.discordTrustList.checkDiscord(minecraftUser.getDiscordUser())){
                minecraftUser.setDiscordUser(null);
                database.updateUser(minecraftUser);
            }else {
                discordBot.discordTrustList.getRoles(minecraftUser.getDiscordUser()).forEach(role -> {
                    if(plugin.getConfig().getStringList("vip_roles").contains(role.getIdAsString())){
                        ExtraPlayerData extraPlayerData = plugin.claimManager.getExtraPlayerData(plugin.getServer().getOfflinePlayer(player.getId()));
                        extraPlayerData.vip = true;
                        plugin.claimManager.setExtraPlayerData(plugin.getServer().getOfflinePlayer(player.getId()), extraPlayerData);
                    }
                });
            }
        }else{
            Map<String,MinecraftUser> codeIdMap = plugin.codeIdMap;

            String randomCode = null;
            boolean found = false;

            for(Map.Entry<String,MinecraftUser> entry:codeIdMap.entrySet()){
                if(entry.getValue().equals(minecraftUser)){
                    randomCode = entry.getKey();
                    found = true;
                    break;
                }
            }

            if(!found){
                randomCode = String.format("%04d", random.nextInt(1001));

                while (codeIdMap.containsKey(randomCode)){
                    randomCode = String.format("%04d", random.nextInt(1001));
                }
            }

            codeIdMap.put(randomCode, minecraftUser);

            TagResolver.Single codetemplate = Placeholder.unparsed("code", randomCode);
            Component message = plugin.messageUtil.getMessage("message.join.fail_whitelist",codetemplate);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, message);
        }

        discordBot.discordTrustList.giveRole(minecraftUser);

        if(!plugin.getServer().getOfflinePlayer(event.getUniqueId()).isOp()){
            if(plugin.maintenance != null){
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, plugin.maintenance);
            }
        }

        if(plugin.vipOnly){
            ExtraPlayerData extraPlayerData = plugin.claimManager.getExtraPlayerData(plugin.getServer().getOfflinePlayer(player.getId()));
            if(!extraPlayerData.vip){
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, plugin.messageUtil.getMessage("vip_mode.kick"));
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if(event.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)){
            if(!plugin.getConfig().getBoolean("portal.end",false)){
                event.setCancelled(true);
            }
        }else if(event.getCause().equals(PlayerTeleportEvent.TeleportCause.END_GATEWAY)){
            if(!plugin.getConfig().getBoolean("portal.end_gateway",false)){
                event.setCancelled(true);
            }
        }else if(event.getCause().equals(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL)){
            if(!plugin.getConfig().getBoolean("portal.nether",false)){
                event.setCancelled(true);
            }
        }
    }

}
