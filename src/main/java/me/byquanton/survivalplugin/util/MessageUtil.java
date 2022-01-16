package me.byquanton.survivalplugin.util;

import me.byquanton.survivalplugin.SurvivalPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
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

    public void sendMessage(CommandSender sender, String configString, Template... templates){
        sendMessage(sender, configString, List.of(templates));
    }
    public void sendMessage(CommandSender sender, String configString, List<Template> templates){
        sender.sendMessage(getMessage(configString, templates));
    }

    public Component getMessage(String configString, Template... templates){
        return getMessage(configString, List.of(templates));
    }

    public Component getMessage(String configString, List<Template> templates){
        return MiniMessage.get().parse(getString(configString), templates);
    }

    public String getString(String configString){
        return Objects.requireNonNullElse(messageConfiguration.getString(configString), configString);
    }

    public List<String> getStrings(String configString){
        return Objects.requireNonNullElse(messageConfiguration.getStringList(configString), List.of(configString));
    }
}
