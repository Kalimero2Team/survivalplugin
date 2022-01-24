package com.kalimero2.team.survivalplugin.command;

import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CraftingTableCommand extends Command{

    protected CraftingTableCommand(SurvivalPlugin plugin, CommandManager commandManager) {
        super(plugin, commandManager);
    }

    @Override
    public void register() {
        commandManager.command(commandManager.commandBuilder("craftingtable").handler(this::craftingTable));
        commandManager.command(commandManager.commandBuilder("ct").handler(this::craftingTable));

    }


    private void craftingTable(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            if(player.getStatistic(Statistic.CRAFT_ITEM, Material.CRAFTING_TABLE) > 0){
                player.openWorkbench(player.getLocation(), true);
            }else {
                plugin.messageUtil.sendMessage(player, "message.command.craftingtable.fail_never_crafted");
            }
        }
    }
}
