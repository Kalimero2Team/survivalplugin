package com.kalimero2.team.survivalplugin.command;

import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BedCommand extends Command{

    protected BedCommand(SurvivalPlugin plugin, CommandManager commandManager) {
        super(plugin, commandManager);
    }

    @Override
    public void register() {
        commandManager.command(commandManager.commandBuilder("bed").handler(this::bed));
        commandManager.command(commandManager.commandBuilder("home").handler(this::bed));
    }


    private void bed(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            if(player.getBedSpawnLocation() != null){
                player.teleportAsync(player.getBedSpawnLocation());
            }

        }
    }
}
