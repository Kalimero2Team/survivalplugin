package com.kalimero2.team.survivalplugin.command;

import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import com.kalimero2.team.survivalplugin.discord.DiscordBot;
import com.kalimero2.team.survivalplugin.database.MongoDB;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.javacord.api.entity.user.User;


public class DiscordCommand extends CommandHandler {

    private final DiscordBot discordBot;
    private final MongoDB database;

    protected DiscordCommand(SurvivalPlugin plugin, CommandManager commandManager) {
        super(plugin, commandManager);
        this.discordBot = plugin.getDiscordBot();
        this.database = plugin.getDatabase();
    }

    @Override
    public void register() {
        if(this.discordBot != null && this.database != null){
            commandManager.command(commandManager.commandBuilder("discord").argument(OfflinePlayerArgument.optional("player")).handler(this::info));
        }
    }

    private void info(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            OfflinePlayer target = context.getOrDefault("player",player);

            User user = discordBot.discordTrustList.getMember(database.getUser(target.getUniqueId()).getDiscordUser());
            String name = user.getDiscriminatedName();
            String id = user.getIdAsString();
            plugin.messageUtil.sendMessage(player, "message.command.discord.info", Placeholder.unparsed("discord_name",name),Placeholder.unparsed("discord_id",id));
        }
    }


}
