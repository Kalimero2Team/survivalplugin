package com.kalimero2.team.survivalplugin.discord;

import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import com.kalimero2.team.survivalplugin.database.MongoDB;
import com.kalimero2.team.survivalplugin.database.pojo.MinecraftUser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.UUID;

public class DiscordBot extends ListenerAdapter {

    private final JDA jda;
    public DiscordTrustList discordTrustList;
    private final SurvivalPlugin plugin;

    public DiscordBot(String token, SurvivalPlugin plugin) throws LoginException, InterruptedException {
        this.plugin = plugin;
        JDABuilder builder = JDABuilder.createDefault(token);
        builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
        builder.setBulkDeleteSplittingEnabled(false);
        builder.disableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_TYPING);
        builder.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS);
        builder.setLargeThreshold(50);
        builder.addEventListeners(this);
        builder.setActivity(Activity.playing("Minecraft"));
        jda = builder.build();

        jda.awaitReady();

        discordTrustList = new DiscordTrustList(plugin, jda);


        jda.upsertCommand("minecraft", "Minecraft User Lookup").queue();
        String serverId = plugin.getConfig().getString("discord.server");
        if(serverId == null){
            plugin.getLogger().severe("No server id found in config.yml");
            return;
        }
        Guild guild = jda.getGuildById(serverId);
        if(guild != null){
            guild.upsertCommand("minecraft", "Minecraft User Lookup").queue();
        }else{
            plugin.getLogger().severe("Could not find server with id " + serverId);
        }
        jda.addEventListener(this);

    }

    public void disconnect(){
        jda.shutdownNow();
    }


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
        MongoDB database = plugin.getDatabase();
        if (event.getName().equals("minecraft") && database != null) {
            User user = event.getUser();
            List<MinecraftUser> minecraftUsers = database.getUsers(user.getId());
            StringBuilder message = new StringBuilder("Minecraft Account(s): ");
            minecraftUsers.forEach(minecraftUser -> message.append(plugin.getServer().getOfflinePlayer(UUID.fromString(minecraftUser.getUuid())).getName()).append(" "));
            event.reply(message.toString()).setEphemeral(true).queue();
        }else{
            event.reply("I can't handle that command right now :(").setEphemeral(true).queue();
            plugin.getLogger().warning("Unhandled command " + event.getName());
        }
    }
}
