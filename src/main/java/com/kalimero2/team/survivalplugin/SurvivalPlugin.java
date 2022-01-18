package com.kalimero2.team.survivalplugin;

import com.kalimero2.team.survivalplugin.command.CommandManager;
import com.kalimero2.team.survivalplugin.database.MongoDB;
import com.kalimero2.team.survivalplugin.database.pojo.MinecraftUser;
import com.kalimero2.team.survivalplugin.discord.DiscordBot;
import com.kalimero2.team.survivalplugin.listener.ClaimListener;
import com.kalimero2.team.survivalplugin.listener.MainListener;
import com.kalimero2.team.survivalplugin.util.*;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class SurvivalPlugin extends JavaPlugin {

    private static SurvivalPlugin plugin;

    private MongoDB database;
    private DiscordBot discordBot;
    private DiscordConsole discordConsole;
    public boolean chatMute;
    public Component maintenance;
    public FloodgateApi floodgateApi;
    public Component motd;
    public MessageUtil messageUtil;
    public IntroductionUtil introductionUtil;
    public ClaimManager claimManager;
    public File playerDataFolder;
    public Map<String, MinecraftUser> codeIdMap = new HashMap<>();


    @Override
    public void onLoad(){

        plugin = this;

        plugin.saveDefaultConfig();

        this.messageUtil = new MessageUtil(plugin, new File(this.getDataFolder()+"/"+ plugin.getConfig().getString("messages")));

        this.playerDataFolder = new File(this.getDataFolder()+ "/playerdata/");

        if(!playerDataFolder.exists()){
            playerDataFolder.mkdirs();
        }

        this.database = new MongoDB(plugin.getConfig().getString("mongodb.url"),plugin.getConfig().getString("mongodb.database"));

        try {
            this.discordBot = new DiscordBot(plugin.getConfig().getString("discord.token"), plugin);
        } catch (LoginException e) {
            e.printStackTrace();
        }

        floodgateApi = FloodgateApi.getInstance();
    }

    @Override
    public void onEnable() {
        motd = this.getServer().motd();

        claimManager = new ClaimManager(plugin);
        plugin.getLogger().info("Loaded ClaimManager");

        plugin.getServer().getPluginManager().registerEvents(new MainListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ClaimListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ChunkBorders(plugin), plugin);

        plugin.getLogger().info("Registered Events");

        /*new EnchantmentManager(this);
        plugin.getLogger().info("Registered Enchantments");*/
        introductionUtil = new IntroductionUtil(this);

        try {
            new CommandManager(plugin);
            plugin.getLogger().info("Registered Commands");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize Commands" + e.getMessage());
        }


        discordConsole = new DiscordConsole(plugin.getConfig().getString("discord.webhook"));
        discordConsole.sendUpdateEmbed("Server Status Update","Server Starting");
        plugin.getLogger().info("Started Discord Console Relay");

    }

    @Override
    public void onDisable() {
        discordConsole.sendUpdateEmbed("Server Status Update","Server Stopping");
        discordBot.discordApi.disconnect();
    }


    public DiscordBot getDiscordBot() {
        return discordBot;
    }

    public MongoDB getDatabase() {
        return database;
    }

}
