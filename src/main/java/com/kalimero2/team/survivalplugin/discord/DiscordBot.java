package com.kalimero2.team.survivalplugin.discord;

import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import com.kalimero2.team.survivalplugin.database.pojo.MinecraftUser;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.*;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;

import javax.security.auth.login.LoginException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DiscordBot {

    private DiscordApi discordApi;
    public DiscordTrustList discordTrustList;
    private final SurvivalPlugin plugin;

    public DiscordBot(String token, SurvivalPlugin plugin) throws LoginException {
        this.plugin = plugin;
        discordApi = new DiscordApiBuilder().setToken(token).setAllIntents().login().join();
        plugin.getLogger().info(discordApi.createBotInvite(new PermissionsBuilder().setAllAllowed().build()));
        discordApi.updateActivity(ActivityType.PLAYING, "Minecraft");
        discordTrustList = new DiscordTrustList(plugin, discordApi);

        Optional<Server> server = discordApi.getServerById(plugin.getConfig().getString("discord.server"));
        if(server.isPresent()){
            SlashCommand command;
            if(discordApi.getServerSlashCommands(server.get()).join().stream().noneMatch(slashCommand -> slashCommand.getName().equals("minecraft"))){
                command = SlashCommand.with("minecraft","Minecraft User Lookup").addOption(SlashCommandOption.createUserOption("discordUser","User",true)).setDefaultPermission(false).createForServer(server.get()).join();
            }else {
                command = discordApi.getServerSlashCommands(server.get()).join().stream().filter(slashCommand -> slashCommand.getName().equals("minecraft")).findFirst().orElse(null);
            }
            if(command != null){
                List<ApplicationCommandPermissions> commandPermissions = new ArrayList<>();
                for(long id:plugin.getConfig().getLongList("admin-roles")){
                    commandPermissions.add(ApplicationCommandPermissions.create(id, ApplicationCommandPermissionType.ROLE, true));
                }
                new ApplicationCommandPermissionsUpdater(server.get()).setPermissions(commandPermissions).update(command.getId());
                discordApi.addSlashCommandCreateListener(event -> {
                    SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
                    User user = slashCommandInteraction.getOptionUserValueByName("discordUser").orElse(null);
                    if(user != null){
                        List<MinecraftUser> minecraftUsers = plugin.getDatabase().getUsers(user.getIdAsString());
                        StringBuilder message = new StringBuilder("Minecraft Account(s): ");
                        minecraftUsers.forEach(minecraftUser -> {
                            message.append(plugin.getServer().getOfflinePlayer(UUID.fromString(minecraftUser.getUuid())).getName()).append(" ");
                        });
                        slashCommandInteraction.createImmediateResponder().setContent(message.toString()).setFlags(InteractionCallbackDataFlag.EPHEMERAL).respond();
                    }
                });
            }
            }

    }

    private CompletableFuture<Void> deleteAllSlashCommands(DiscordApi api) {
        var deleteFuture = api.getGlobalSlashCommands()
                .thenComposeAsync(sc -> CompletableFuture.allOf(sc.stream()
                        .map(SlashCommand::deleteGlobal)
                        .toList().toArray(new CompletableFuture[0])
                ));
        var serverDeleteFutures = api.getServers().stream().map(server -> {
                    return server.getSlashCommands()
                            .thenComposeAsync(sc -> CompletableFuture.allOf(sc.stream()
                                    .map(c -> {
                                        return c.deleteForServer(server);
                                    })
                                    .toList().toArray(new CompletableFuture[0])
                            ));
                })
                .toList();
        return CompletableFuture.allOf(serverDeleteFutures.toArray(new CompletableFuture[0]))
                .thenComposeAsync(unused -> deleteFuture);
    }

}
