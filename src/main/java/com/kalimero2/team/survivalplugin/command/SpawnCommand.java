package com.kalimero2.team.survivalplugin.command;

import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand extends Command{


    protected SpawnCommand(SurvivalPlugin plugin, CommandManager commandManager) {
        super(plugin, commandManager);
    }

    @Override
    public void register() {
        this.commandManager.command(this.commandManager.commandBuilder("spawn").handler(this::spawn));
    }

    private void spawn(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            if(player.getWorld().getEnvironment().equals(World.Environment.NORMAL)){
                player.teleportAsync(player.getWorld().getSpawnLocation());
            }
        }
    }
}
