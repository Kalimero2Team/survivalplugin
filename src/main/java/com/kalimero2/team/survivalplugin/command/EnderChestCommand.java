package com.kalimero2.team.survivalplugin.command;

import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnderChestCommand extends CommandHandler {

    protected EnderChestCommand(SurvivalPlugin plugin, CommandManager commandManager) {
        super(plugin, commandManager);
    }


    @Override
    public void register() {
        commandManager.command(commandManager.commandBuilder("enderchest","ec").handler(this::enderChest));

    }


    private void enderChest(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            if(player.getStatistic(Statistic.CRAFT_ITEM, Material.ENDER_CHEST) > 0){
                player.openInventory(player.getEnderChest());
            }else {
                plugin.messageUtil.sendMessage(player, "message.command.enderchest.fail_never_crafted");
            }
        }
    }
}
