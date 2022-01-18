package com.kalimero2.team.survivalplugin.command;

import cloud.commandframework.arguments.standard.BooleanArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import cloud.commandframework.bukkit.parsers.location.Location2D;
import cloud.commandframework.bukkit.parsers.location.Location2DArgument;
import cloud.commandframework.context.CommandContext;
import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import com.kalimero2.team.survivalplugin.util.ClaimManager;
import com.kalimero2.team.survivalplugin.util.ExtraPlayerData;
import com.kalimero2.team.survivalplugin.util.SerializableChunk;
import com.kalimero2.team.survivalplugin.database.pojo.MinecraftUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class AdminCommand extends Command{

    protected AdminCommand(SurvivalPlugin plugin, CommandManager commandManager) {
        super(plugin, commandManager);
    }

    @Override
    public void register() {
        this.commandManager.command(this.commandManager.commandBuilder("admin").literal("reload").permission("admin").handler(this::reload));
        this.commandManager.command(this.commandManager.commandBuilder("admin").literal("database").literal("reload").permission("admin").handler(this::reloadDatabase));
        this.commandManager.command(this.commandManager.commandBuilder("admin").literal("database").literal("purge").argument(OfflinePlayerArgument.of("player")).permission("admin").handler(this::purgeDatabase));
        this.commandManager.command(this.commandManager.commandBuilder("admin").literal("alts").argument(OfflinePlayerArgument.of("player")).permission("admin").handler(this::alts));
        this.commandManager.command(this.commandManager.commandBuilder("admin").literal("tab").literal("header").argument(StringArgument.greedy("header")).permission("admin").handler(this::header));
        this.commandManager.command(this.commandManager.commandBuilder("admin").literal("tab").literal("footer").argument(StringArgument.greedy("footer")).permission("admin").handler(this::footer));
        this.commandManager.command(this.commandManager.commandBuilder("admin").literal("claims").argument(OfflinePlayerArgument.of("player")).permission("admin").handler(this::extraPlayerData));
        this.commandManager.command(this.commandManager.commandBuilder("admin").literal("unclaim-all").argument(OfflinePlayerArgument.of("player")).permission("admin").handler(this::unClaimAll));
        this.commandManager.command(this.commandManager.commandBuilder("admin").literal("chunk").literal("claim").argument(StringArgument.greedy("message")).permission("admin").handler(this::claim));
        this.commandManager.command(this.commandManager.commandBuilder("admin").literal("chunk").literal("unclaim").permission("admin").handler(this::unClaim));
        this.commandManager.command(this.commandManager.commandBuilder("admin").literal("set-max-claims").argument(OfflinePlayerArgument.of("player")).argument(IntegerArgument.of("claims")).permission("admin").handler(this::setMaxClaims));
        this.commandManager.command(this.commandManager.commandBuilder("admin").literal("tpchunk").argument(Location2DArgument.of("location")).permission("admin").handler(this::teleportChunk));
        this.commandManager.command(this.commandManager.commandBuilder("admin").literal("portal").literal("end").argument(BooleanArgument.of("bool")).permission("admin").handler(this::setEnableEndPortal));
        this.commandManager.command(this.commandManager.commandBuilder("admin").literal("portal").literal("end-gateway").argument(BooleanArgument.of("bool")).permission("admin").handler(this::setEnableEndGateway));;
        this.commandManager.command(this.commandManager.commandBuilder("admin").literal("portal").literal("nether").argument(BooleanArgument.of("bool")).permission("admin").handler(this::setEnableNetherPortal));
        this.commandManager.command(this.commandManager.commandBuilder("admin").literal("max-players").argument(IntegerArgument.of("amount")).permission("admin").handler(this::setMaxPlayers));
        this.commandManager.command(this.commandManager.commandBuilder("admin").literal("maintenance").literal("on").argument(StringArgument.greedy("text")).permission("admin").handler(this::maintenanceOn));
        this.commandManager.command(this.commandManager.commandBuilder("admin").literal("maintenance").literal("off").permission("admin").handler(this::maintenanceOff));
    }

    private void reload(CommandContext<CommandSender> context){
        plugin.reloadConfig();
        context.getSender().sendMessage(Component.text("Reloaded Config").color(NamedTextColor.AQUA));
    }

    private void reloadDatabase(CommandContext<CommandSender> context){
        plugin.getDatabase().getUsers(true);
        context.getSender().sendMessage(Component.text("Reloaded Database").color(NamedTextColor.AQUA));
    }

    private void purgeDatabase(CommandContext<CommandSender> context){
        OfflinePlayer player = context.get("player");
        plugin.getDiscordBot().removeDiscordUser(plugin.getDatabase().getUser(player.getUniqueId()));
        context.getSender().sendMessage(Component.text("Removed User from Database").color(NamedTextColor.AQUA));
    }

    private void alts(CommandContext<CommandSender> context){
        OfflinePlayer player = context.get("player");

        MinecraftUser user = plugin.getDatabase().getUser(player.getUniqueId());
        if(user != null){
            plugin.getDatabase().getUserAlts(user).forEach(u -> context.getSender().sendMessage(u.getName()));
        }
    }

    private void header(CommandContext<CommandSender> context){
        String headerString = context.get("header");

        plugin.getConfig().set("tab.header", headerString);
        plugin.saveConfig();

        context.getSender().getServer().getOnlinePlayers().forEach(player -> {
            player.sendPlayerListHeader(MiniMessage.get().parse(headerString));
        });
    }

    private void footer(CommandContext<CommandSender> context){
        String footerString = context.get("footer");

        plugin.getConfig().set("tab.footer", footerString);
        plugin.saveConfig();

        context.getSender().getServer().getOnlinePlayers().forEach(player -> {
            player.sendPlayerListFooter(MiniMessage.get().parse(footerString));
        });
    }

    private void teleportChunk(CommandContext<CommandSender> context) {
        if(context.getSender() instanceof Player player){
            Location2D location2D = context.get("location");
            int x = 16 * location2D.getBlockX() + 1;
            int z = 16 * location2D.getBlockZ() + 1;
            Location location = location2D.clone().set(x, player.getLocation().getY(), z);
            player.teleport(location.toHighestLocation());
        }
    }

    private void setMaxClaims(CommandContext<CommandSender> context) {
        CommandSender sender =  context.getSender();
        ExtraPlayerData extraPlayerData = plugin.claimManager.getExtraPlayerData(context.get("player"));
        Integer newmaxclaims = context.get("claims");
        sender.sendMessage("Old Max Claims: "+extraPlayerData.maxclaims);
        sender.sendMessage("New Max Claims: "+newmaxclaims);
        extraPlayerData.maxclaims = newmaxclaims;
        plugin.claimManager.setExtraPlayerData(context.get("player"),extraPlayerData);
    }

    private void claim(CommandContext<CommandSender> context){
        if(context.getSender() instanceof Player player) {
            Chunk chunk = player.getLocation().getChunk();
            ClaimManager claimManager = plugin.claimManager;

            ExtraPlayerData extraPlayerData = plugin.claimManager.getExtraPlayerData(player);
            int updated = extraPlayerData.maxclaims + 1;
            player.sendMessage("Team Claim");
            player.sendMessage("Old Max Claims: "+extraPlayerData.maxclaims);
            player.sendMessage("New Max Claims: "+updated);
            extraPlayerData.maxclaims = updated;
            plugin.claimManager.setExtraPlayerData(player,extraPlayerData);

            claimManager.teamClaimChunk(chunk ,player, context.get("message"));
            plugin.messageUtil.sendMessage(player,"message.command.chunk.claim_success");

        }
    }

    private void unClaim(CommandContext<CommandSender> context){
        if(context.getSender() instanceof Player player) {
            Chunk chunk = player.getLocation().getChunk();

            if(plugin.claimManager.isTeamClaim(chunk)){
                plugin.claimManager.forceUnClaimChunk(chunk);
                plugin.messageUtil.sendMessage(player,"message.command.chunk.unclaim_force_success");
            }

        }
    }

    private void unClaimAll(CommandContext<CommandSender> context){
        OfflinePlayer player = context.get("player");
        ExtraPlayerData extraPlayerData = plugin.claimManager.getExtraPlayerData(player);

        for(SerializableChunk chunk: extraPlayerData.chunks){
            Chunk bukkit_chunk =  plugin.claimManager.getChunk(chunk);
            if(plugin.claimManager.getOwner(bukkit_chunk) == null)
                context.getSender().sendMessage("Wrong Player owns this Chunk");
            else if(plugin.claimManager.getOwner(bukkit_chunk).equals(player)){
                plugin.claimManager.forceUnClaimChunk(bukkit_chunk);
                context.getSender().sendMessage("Unclaimed Chunk. "+chunk.x +" "+ chunk.z);
            }else {
                context.getSender().sendMessage("Wrong Player owns this Chunk");
            }

        }
    }

    private void extraPlayerData(CommandContext<CommandSender> context) {
        CommandSender sender =  context.getSender();
        OfflinePlayer player = context.get("player");
        ExtraPlayerData extraPlayerData = plugin.claimManager.getExtraPlayerData(player);
        Integer maxclaims = extraPlayerData.maxclaims;

        Component maxplayers = Component.text("Max-Claims: " +maxclaims.toString());
        maxplayers = maxplayers.hoverEvent(HoverEvent.showText(Component.text("Click to Change")));
        maxplayers = maxplayers.clickEvent(ClickEvent.suggestCommand("/admin set-max-claims "+player.getName()+" "+maxclaims));
        sender.sendMessage(maxplayers);
        sender.sendMessage("Claims: ");
        for(SerializableChunk chunk: extraPlayerData.chunks){
            Chunk bukkitchunk = plugin.claimManager.getChunk(chunk);
            Component location = Component.text("   World: "+bukkitchunk.getWorld().getName()+ " X:"+chunk.x+" Z"+chunk.z);
            location = location.hoverEvent(HoverEvent.showText(Component.text("Click to TP")));
            location = location.clickEvent(ClickEvent.runCommand("/admin tpchunk "+ chunk.x + " "+ chunk.z));
            sender.sendMessage(location);
            if(plugin.claimManager.getOwner(plugin.claimManager.getChunk(chunk)) != null){
                sender.sendMessage("   Owner: "+ plugin.claimManager.getOwner(plugin.claimManager.getChunk(chunk)).getName());
            }else {
                sender.sendMessage("   Owner: (NOT FOUND)");
            }

            List<OfflinePlayer> trustedPlayers = plugin.claimManager.getTrustedList(plugin.claimManager.getChunk(chunk));
            if(trustedPlayers.size() >= 1){
                sender.sendMessage("   Trusted: ");
                for(OfflinePlayer trusted: trustedPlayers){
                    sender.sendMessage("      "+ trusted.getName());
                }
            }
        }

    }
    private void setEnableEndPortal(CommandContext<CommandSender> context){
        boolean bool = context.get("bool");
        plugin.getConfig().set("portal.end",bool);
        plugin.saveConfig();
    }

    private void setEnableEndGateway(CommandContext<CommandSender> context){
        boolean bool = context.get("bool");
        plugin.getConfig().set("portal.end_gateway",bool);
        plugin.saveConfig();
   }

    private void setEnableNetherPortal(CommandContext<CommandSender> context){
        boolean bool = context.get("bool");
        plugin.getConfig().set("portal.nether",bool);
        plugin.saveConfig();
    }

    private void setMaxPlayers(CommandContext<CommandSender> context){
        int amount = context.get("amount");
        plugin.getServer().setMaxPlayers(amount);
    }

    private void maintenanceOn(CommandContext<CommandSender> context){
        String text = context.get("text");
        plugin.maintenance = MiniMessage.get().parse(text);
    }
    private void maintenanceOff(CommandContext<CommandSender> context){
        plugin.maintenance = null;
    }
}

