package com.kalimero2.team.survivalplugin.command;

import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import de.jeff_media.morepersistentdatatypes.DataType;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class MuteCommand extends Command{

    protected MuteCommand(SurvivalPlugin plugin, CommandManager commandManager) {
        super(plugin, commandManager);
    }

    @Override
    public void register() {
        this.commandManager.command(this.commandManager.commandBuilder("mute").argument(OfflinePlayerArgument.of("player")).permission("commandManager").handler(this::mute));
        this.commandManager.command(this.commandManager.commandBuilder("mute").literal("global").permission("admin").handler(this::muteGlobal));
    }

    private void mute(CommandContext<CommandSender> context) {
        Player target = context.get("player");
        NamespacedKey key = new NamespacedKey(plugin,"muted");
        if(target.getPersistentDataContainer().has(key, DataType.BOOLEAN)){
            if(target.getPersistentDataContainer().get(key, DataType.BOOLEAN)){
                context.getSender().sendMessage(target.getName()+" is now no longer muted");
                target.getPersistentDataContainer().set(key, DataType.BOOLEAN, false);
            }else {
                context.getSender().sendMessage(target.getName()+" is now muted");
                target.getPersistentDataContainer().set(key, DataType.BOOLEAN, true);
            }
        }else {
            context.getSender().sendMessage(target.getName()+" is now muted");
            target.getPersistentDataContainer().set(key, DataType.BOOLEAN, true);
        }
    }

    private void muteGlobal(CommandContext<CommandSender> context){
        if(plugin.chatMute){
            plugin.chatMute = false;
            context.getSender().sendMessage("Global Chat is no longer muted");
        }else {
            plugin.chatMute = true;
            context.getSender().sendMessage("Global Chat is now muted");
        }
    }


}
