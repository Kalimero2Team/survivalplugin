package com.kalimero2.team.survivalplugin.util;

import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

public class MessageUtil {

    private final FileConfiguration externalConfig;
    private final FileConfiguration internalConfig;

    public MessageUtil(SurvivalPlugin plugin, File messageConfig){
        if (!messageConfig.exists()) {
            plugin.saveResource(messageConfig.getName(), false);
        }
        externalConfig = YamlConfiguration.loadConfiguration(messageConfig);

        InputStream inputStream = plugin.getResource(messageConfig.getName());

        assert inputStream != null;
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        internalConfig = YamlConfiguration.loadConfiguration(inputStreamReader);

    }

    public void sendMessage(CommandSender sender, String configString, TagResolver... tagResolvers){
        sender.sendMessage(getMessage(configString,tagResolvers));
    }

    public Component getMessage(String configString, TagResolver... tagResolvers){
        return MiniMessage.miniMessage().deserialize(getString(configString), tagResolvers);
    }


    public String getString(String configString){
        if(externalConfig.contains(configString)){
            return externalConfig.getString(configString);
        }else {
            return internalConfig.getString(configString);
        }
    }

    public List<String> getStrings(String configString){
        if(externalConfig.contains(configString)){
            return externalConfig.getStringList(configString);
        }else {
            return internalConfig.getStringList(configString);
        }
    }
}
