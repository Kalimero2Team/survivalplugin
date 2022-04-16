package com.kalimero2.team.survivalplugin.command;

import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatusCommand extends CommandHandler{


    protected StatusCommand(SurvivalPlugin plugin, CommandManager commandManager) {
        super(plugin, commandManager);
    }

    @Override
    public void register() {
        commandManager.command(
                commandManager.commandBuilder("status")
                        .argument(StringArgument.<CommandSender>newBuilder("status").asOptional().withSuggestionsProvider(
                                (commandSender, s) -> plugin.playerStatus.allowed()
                        ).build())
                        .handler(this::statusCommand)
        );
    }

    private void statusCommand(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            if(!context.contains("status")){
                plugin.playerStatus.setStatus(player,null);

            }else if(plugin.playerStatus.isAllowed(context.get("status"))){
                plugin.playerStatus.setStatus(player, context.get("status"));
            }

            Component playerlistname ;
            if(player.hasPermission("team")){
                playerlistname = Component.text("Team ").color(NamedTextColor.DARK_AQUA).append(player.name().color(NamedTextColor.WHITE));
            }else if(plugin.claimManager.getExtraPlayerData(player).vip){
                playerlistname = Component.text("VIP ").color(NamedTextColor.GOLD).append(player.name().color(NamedTextColor.WHITE));
            }else {
                playerlistname = Component.text("Spieler ").color(NamedTextColor.GRAY).append(player.name().color(NamedTextColor.WHITE));
            }
            if(plugin.playerStatus.getStatus(player) != null){
                Component suffix = MiniMessage.miniMessage().deserialize(" <gray>[<status>]</gray>", TagResolver.builder().resolver(Placeholder.unparsed("status",plugin.playerStatus.getStatus(player))).build());
                playerlistname = playerlistname.append(suffix);
            }

            player.playerListName(playerlistname);
        }
    }
}
