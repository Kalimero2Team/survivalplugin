package com.kalimero2.team.survivalplugin.command;

import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import com.kalimero2.team.survivalplugin.database.pojo.DiscordUser;
import com.kalimero2.team.survivalplugin.discord.DiscordBot;
import com.kalimero2.team.survivalplugin.database.MongoDB;
import net.dv8tion.jda.api.entities.Member;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

            Member member;
            if (target != null) {
                DiscordUser discordUser = database.getUser(target.getUniqueId()).getDiscordUser();
                if(discordUser != null){
                    member = discordBot.discordTrustList.getMember(discordUser);
                    if(member != null){
                        String name = member.getUser().getName()+"#"+member.getUser().getDiscriminator();
                        String id = member.getId();
                        plugin.messageUtil.sendMessage(player, "message.command.discord.info", Placeholder.unparsed("discord_name",name),Placeholder.unparsed("discord_id",id));
                    }else{
                        plugin.messageUtil.sendMessage(player, "message.command.discord.info", Placeholder.unparsed("discord_name","null"),Placeholder.unparsed("discord_id","null"));
                    }
                }
            }
        }
    }


}
