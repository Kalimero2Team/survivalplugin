package me.byquanton.survivalplugin.command;

import cloud.commandframework.context.CommandContext;
import me.byquanton.survivalplugin.SurvivalPlugin;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class IntroductionCommand extends Command{


    protected IntroductionCommand(SurvivalPlugin plugin, CommandManager commandManager) {
        super(plugin, commandManager);
    }

    @Override
    public void register() {
        this.commandManager.command(this.commandManager.commandBuilder("introduction").handler(this::introduction));
        this.commandManager.command(this.commandManager.commandBuilder("info").handler(this::introduction));
        this.commandManager.command(this.commandManager.commandBuilder("intro").handler(this::introduction));
        this.commandManager.command(this.commandManager.commandBuilder("einführung").handler(this::introduction));
    }

    private void introduction(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            if(plugin.floodgateApi.isFloodgatePlayer(player.getUniqueId())){
                plugin.floodgateApi.sendForm(player.getUniqueId(), plugin.introductionUtil.getForm());
            }else {
                player.openBook(plugin.introductionUtil.getBook());
            }

       }
    }
}
