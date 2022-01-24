package com.kalimero2.team.survivalplugin.command;

import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import com.kalimero2.team.survivalplugin.util.ClaimManager;
import com.kalimero2.team.survivalplugin.util.ExtraPlayerData;
import com.kalimero2.team.survivalplugin.util.MessageUtil;
import com.kalimero2.team.survivalplugin.util.ChunkBorders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ChunkCommand extends Command{

    private final ClaimManager claimManager;
    private final MessageUtil messageUtil;
    private final ArrayList<UUID> forcedOwner = new ArrayList<>();

    protected ChunkCommand(SurvivalPlugin plugin, CommandManager commandManager) {
        super(plugin, commandManager);
        this.claimManager = plugin.claimManager;
        this.messageUtil = plugin.messageUtil;
    }

    @Override
    public void register() {
        commandManager.command(commandManager.commandBuilder("chunk").handler(this::info));
        commandManager.command(commandManager.commandBuilder("chunk").literal("add").argument(OfflinePlayerArgument.of("player")).handler(this::add));
        commandManager.command(commandManager.commandBuilder("chunk").literal("remove").argument(OfflinePlayerArgument.of("player")).handler(this::remove));
        commandManager.command(commandManager.commandBuilder("chunk").literal("claim").handler(this::claim));
        commandManager.command(commandManager.commandBuilder("chunk").literal("unclaim").handler(this::unClaim));
        commandManager.command(commandManager.commandBuilder("chunk").literal("border").handler(this::border));
        commandManager.command(commandManager.commandBuilder("chunkborder").handler(this::border));
        commandManager.command(commandManager.commandBuilder("cb").handler(this::border));
        commandManager.command(commandManager.commandBuilder("chunk").literal("force").permission("admin").handler(this::force));
    }

    private String getConfigString(String path){
        String returnString = this.plugin.getConfig().getString(path);
        if (returnString == null)
            returnString = path;
        return returnString;
    }

    private void info(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            Chunk chunk = player.getLocation().getChunk();

            messageUtil.sendMessage(player,"message.command.chunk.info", Template.of("chunk_x", Component.text(chunk.getX())), Template.of("chunk_z", Component.text(chunk.getZ())));

            if(claimManager.isClaimed(chunk)){
                messageUtil.sendMessage(player, "message.command.chunk.claimed_true");

                if(claimManager.isTeamClaim(chunk)){
                    messageUtil.sendMessage(player, "message.command.chunk.team_chunk");
                }else {
                    messageUtil.sendMessage(player, "message.command.chunk.claim_owner", Template.of("player", Objects.requireNonNullElse(claimManager.getOwner(chunk).getName(),"player")));
                }

                List<OfflinePlayer> trustedPlayers = claimManager.getTrustedList(chunk);
                if(trustedPlayers.size() >= 1){
                    messageUtil.sendMessage(player, "message.command.chunk.claim_trusted_start");
                    for(OfflinePlayer trustedPlayer:trustedPlayers){
                        messageUtil.sendMessage(player, "message.command.chunk.claim_trusted_player", Template.of("player", Objects.requireNonNullElse(trustedPlayer.getName(),"player")));
                    }
                }
            }else {
                messageUtil.sendMessage(player,"message.command.chunk.claimed_false");
            }
        }
    }

    private void add(CommandContext<CommandSender> context){
        if(context.getSender() instanceof Player player) {
            Chunk chunk = player.getLocation().getChunk();
            OfflinePlayer target = context.get("player");
            Template target_template = Template.of("player", Objects.requireNonNullElse(target.getName(),"target_name"));

            if (claimManager.isClaimed(chunk)) {
                if(claimManager.getOwner(player.getLocation().getChunk()).equals(player) || forcedOwner.contains(player.getUniqueId())){
                    if(!claimManager.getTrustedList(chunk).contains(target)){
                        claimManager.trust(chunk,target);
                        messageUtil.sendMessage(player,"message.command.chunk.claim_add_success", target_template);
                    }else {
                        messageUtil.sendMessage(player,"message.command.chunk.claim_add_fail_already_added", target_template);
                    }
                }else {
                    messageUtil.sendMessage(player,"message.command.chunk.claim_fail_owner", target_template);
                }
            } else {
                messageUtil.sendMessage(player,"message.command.chunk.claim_add_fail_not_claimed", target_template);
            }

        }
    }

    private void remove(CommandContext<CommandSender> context){
        if(context.getSender() instanceof Player player) {
            Chunk chunk = player.getLocation().getChunk();
            OfflinePlayer target = context.get("player");
            Template target_template = Template.of("player", Objects.requireNonNullElse(target.getName(),"target_name"));

            if (claimManager.isClaimed(chunk)) {
                if(claimManager.getOwner(player.getLocation().getChunk()).equals(player) || forcedOwner.contains(player.getUniqueId())){
                    if(claimManager.getTrustedList(chunk).contains(target)){
                        claimManager.unTrust(chunk,target);
                        messageUtil.sendMessage(player,"message.command.chunk.claim_remove_success",target_template);
                    }else {
                        messageUtil.sendMessage(player,"message.command.chunk.claim_remove_fail_already_removed",target_template);
                    }
                }else {
                    messageUtil.sendMessage(player,"message.command.chunk.claim_fail_owner",target_template);
                }
            } else {
                messageUtil.sendMessage(player,"message.command.chunk.claim_remove_fail_not_claimed",target_template);

            }
        }
    }

    private void claim(CommandContext<CommandSender> context){
        if(context.getSender() instanceof Player player) {
            Chunk chunk = player.getLocation().getChunk();

            if(!chunk.getWorld().getEnvironment().equals(World.Environment.NORMAL)){
                messageUtil.sendMessage(player, "message.command.chunk.claim_fail_dimension");
                return;
            }

            if(!claimManager.canClaim(player)){
                ExtraPlayerData extraPlayerData = claimManager.getExtraPlayerData(player);
                List<Template> templates = List.of(Template.of("count", Component.text(extraPlayerData.chunks.size())), Template.of("max_count", Component.text(extraPlayerData.maxclaims)));
                messageUtil.sendMessage(player,"message.command.chunk.claim_fail_too_many_claims",templates);
                return;
            }


            if(claimManager.claimChunk(chunk,player)){
                messageUtil.sendMessage(player,"message.command.chunk.claim_success");
            }else{
                messageUtil.sendMessage(player,"message.command.chunk.claim_fail_already_claimed");
            }
        }
    }

    private void unClaim(CommandContext<CommandSender> context){
        if(context.getSender() instanceof Player player) {
            Chunk chunk = player.getLocation().getChunk();

            if(claimManager.isTeamClaim(chunk)){
                messageUtil.sendMessage(player,"message.command.chunk.unclaim_fail_team_claim");
                return;
            }

            if(claimManager.unClaimChunk(chunk,player)){
                messageUtil.sendMessage(player,"message.command.chunk.unclaim_success");
            }else{
                if(forcedOwner.contains(player.getUniqueId())){
                    claimManager.forceUnClaimChunk(chunk);
                    messageUtil.sendMessage(player,"message.command.chunk.unclaim_force_success");
                }else {
                    messageUtil.sendMessage(player,"message.command.chunk.unclaim_fail_not_claimed");
                }
            }
        }
    }

    private void border(CommandContext<CommandSender> context){
        if(context.getSender() instanceof Player player) {
            if(ChunkBorders.show_border.contains(player)){
                ChunkBorders.show_border.remove(player);
                messageUtil.sendMessage(player,"message.command.chunk.border_off");
            }else{
                ChunkBorders.show_border.add(player);
                messageUtil.sendMessage(player,"message.command.chunk.border_on");
            }
        }
    }

    private void force(CommandContext<CommandSender> context){
        if(context.getSender() instanceof Player player) {
            if (forcedOwner.contains(player.getUniqueId())) {
                messageUtil.sendMessage(player,"message.command.chunk.force_off");
                forcedOwner.remove(player.getUniqueId());
            } else {
                messageUtil.sendMessage(player,"message.command.chunk.force_on");
                forcedOwner.add(player.getUniqueId());
            }
        }
    }

}
