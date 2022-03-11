package com.kalimero2.team.survivalplugin.util;

import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class MessageUtil {

    private final SurvivalPlugin plugin;
    private FileConfiguration messageConfiguration;

    public MessageUtil(SurvivalPlugin plugin, File messageConfig){
        this.plugin = plugin;
        if (!messageConfig.exists()) {
            plugin.saveResource(messageConfig.getName(), false);
        }
        messageConfiguration = YamlConfiguration.loadConfiguration(messageConfig);
    }

    public void sendMessage(CommandSender sender, String configString, TagResolver... tagResolvers){
        sender.sendMessage(getMessage(configString,tagResolvers));
    }

    public Component getMessage(String configString, TagResolver... tagResolvers){
        return MiniMessage.miniMessage().deserialize(getString(configString), tagResolvers);
    }


    public String getString(String configString){
        return Objects.requireNonNullElse(messageConfiguration.getString(configString), configString);
    }

    public List<String> getStrings(String configString){
        return Objects.requireNonNullElse(messageConfiguration.getStringList(configString), List.of(configString));
    }
}
