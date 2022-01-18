package com.kalimero2.team.survivalplugin.command;

import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
