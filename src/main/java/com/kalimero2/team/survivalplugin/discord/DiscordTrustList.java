package com.kalimero2.team.survivalplugin.discord;

import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import com.kalimero2.team.survivalplugin.database.pojo.DiscordUser;
import com.kalimero2.team.survivalplugin.database.pojo.MinecraftUser;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageType;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.MessageComponentCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.server.member.ServerMemberLeaveEvent;
import org.javacord.api.interaction.*;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import org.javacord.api.listener.interaction.MessageComponentCreateListener;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.server.member.ServerMemberLeaveListener;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DiscordTrustList implements MessageCreateListener, ServerMemberLeaveListener, MessageComponentCreateListener {

    private SurvivalPlugin plugin;
    private DiscordApi discordApi;
    private final String whitelist_channel;
    private final String whitelist_category;
    private final String whitelist_add_channel_message;

    public DiscordTrustList(SurvivalPlugin plugin, DiscordApi discordApi){
        this.plugin = plugin;
        this.discordApi = discordApi;
        whitelist_channel = plugin.getConfig().getString("discord.whitelist_channel");
        whitelist_category = plugin.getConfig().getString("discord.whitelist_category");
        whitelist_add_channel_message = plugin.messageUtil.getString("message.discord.whitelist_add_channel");

        discordApi.addMessageCreateListener(this);
        discordApi.addServerMemberLeaveListener(this);
        discordApi.addMessageComponentCreateListener(this);

        sendTrustListMessage();
    }

    private void sendTrustListMessage(){
        Optional<Channel> optionalChannel = discordApi.getChannelById(whitelist_channel);
        if(optionalChannel.isPresent()){
            Optional<ServerTextChannel> optionalServerTextChannel = optionalChannel.get().asServerTextChannel();
            if(optionalServerTextChannel.isPresent()){
                try {
                    if(optionalServerTextChannel.get().getPins().get().getOldestMessage().isEmpty()){
                        new MessageBuilder().setContent(whitelist_add_channel_message).addComponents(
                                ActionRow.of(Button.primary("add",plugin.messageUtil.getString("message.discord.add")))
                        ).send(optionalServerTextChannel.get());
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if(event.getMessage().getChannel().getIdAsString().equals(whitelist_channel)){
            if(event.getMessage().getContent().equals(whitelist_add_channel_message)){
                event.getMessage().pin();
            }else if(event.getMessage().getType().equals(MessageType.CHANNEL_PINNED_MESSAGE)){
                event.getMessage().delete();
            }
        }

        if(event.getMessage().getAuthor().isBotUser()){
            return;
        }

        if(event.getMessage().getAuthor().isYourself()){
            return;
        }

        if(event.getChannel().getType().equals(ChannelType.SERVER_TEXT_CHANNEL)){
            ServerTextChannel textChannel = event.getServerTextChannel().get();

            if(textChannel.getCategory().isPresent()){
                if(textChannel.getCategory().get().getIdAsString().equals(whitelist_category)){

                    if(textChannel.getIdAsString().equals(whitelist_channel)){
                        return;
                    }
                    if(!textChannel.getName().equals("whitelist")){
                        return;
                    }

                    String code = event.getMessage().getContent();

                    if(code.matches("-?\\d+")){

                        for(Map.Entry<String,MinecraftUser> entry: plugin.codeIdMap.entrySet()){
                            if(code.equals(entry.getKey())){
                                MinecraftUser user = entry.getValue();

                                plugin.codeIdMap.remove(code);
                                user.setDiscordUser(new DiscordUser(event.getMessage().getAuthor().getIdAsString()));
                                plugin.getDatabase().updateUser(user);

                                StringBuilder rules = new StringBuilder(plugin.messageUtil.getString("message.discord.rules_message"));
                                for(String rule:plugin.getConfig().getStringList("rules")){
                                    rules.append("\n").append(rule);
                                }

                                List<MinecraftUser> alts = plugin.getDatabase().getUserAlts(user);
                                int java = 0;
                                int bedrock = 0;
                                for(MinecraftUser alt:alts){
                                    if(alt.isBedrock()){
                                        bedrock += 1;
                                    }else{
                                        java += 1;
                                    }
                                }

                                if(bedrock > 1 && user.isBedrock()){
                                    user.setDiscordUser(null);
                                    plugin.getDatabase().updateUser(user);
                                    event.getChannel().sendMessage(plugin.messageUtil.getString("message.discord.max_bedrock"));
                                    return;
                                }if(java > 1 && !user.isBedrock()){
                                    user.setDiscordUser(null);
                                    plugin.getDatabase().updateUser(user);
                                    event.getChannel().sendMessage(plugin.messageUtil.getString("message.discord.max_java"));
                                    return;
                                }

                                new MessageBuilder().setContent(rules.toString()).addComponents(
                                        ActionRow.of(Button.success("accept", plugin.messageUtil.getString("message.discord.accept")), Button.danger("deny", plugin.messageUtil.getString("message.discord.deny")))
                                ).send(event.getChannel());


                            }
                        }
                    }

                }
            }
        }


    }

    @Override
    public void onServerMemberLeave(ServerMemberLeaveEvent event) {
        for(MinecraftUser user:plugin.getDatabase().getUsers()){
            if(user.getDiscordUser() != null){
                if(Objects.equals(user.getDiscordUser().getDiscordId(), event.getUser().getIdAsString())){
                    removeDiscordUser(user);
                }
            }
        };
    }

    @Override
    public void onComponentCreate(MessageComponentCreateEvent event) {
        MessageComponentInteraction messageComponentInteraction = event.getMessageComponentInteraction();
        String customId = messageComponentInteraction.getCustomId();

        if(messageComponentInteraction.getChannel().isPresent()){
            if(messageComponentInteraction.getChannel().get().getIdAsString().equals(whitelist_channel)){

                if(customId.equals("add")){
                    Server server = messageComponentInteraction.getServer().get();
                    ServerTextChannel textChannel = null;
                    Optional<ChannelCategory> optionalChannelCategory = server.getChannelCategoryById(whitelist_category);

                    for(ServerChannel serverChannel:server.getChannelsByName("whitelist")){
                        if(serverChannel.asServerTextChannel().isPresent()){
                            if(serverChannel.asServerTextChannel().get().getCategory().isPresent()){
                                if(serverChannel.asServerTextChannel().get().getCategory().get().getIdAsString().equals(whitelist_category)){
                                    if(serverChannel.asServerTextChannel().get().getTopic().equals(messageComponentInteraction.getUser().getIdAsString()))
                                        textChannel = serverChannel.asServerTextChannel().get();
                                    break;
                                }
                            }
                        }
                    }

                    if(textChannel == null){
                        if(optionalChannelCategory.isPresent()){
                            CompletableFuture<ServerTextChannel> channel = server.createTextChannelBuilder().setCategory(optionalChannelCategory.get()).setName("whitelist").create();
                            try {
                                textChannel = channel.get();
                                textChannel.createUpdater().addPermissionOverwrite(messageComponentInteraction.getUser(), new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES).build()).update();
                                textChannel.createUpdater().setTopic(messageComponentInteraction.getUser().getIdAsString()).update();
                                textChannel.sendMessage(messageComponentInteraction.getUser().getMentionTag());
                                new MessageBuilder().setContent(plugin.messageUtil.getString("message.discord.whitelist_code_channel")).
                                        addComponents(
                                                ActionRow.of(Button.secondary("cancel", plugin.messageUtil.getString("message.discord.cancel")))
                                        ).
                                        send(textChannel);

                                String id = textChannel.getIdAsString();
                                new BukkitRunnable(){
                                    @Override
                                    public void run() {
                                        if(discordApi.getChannelById(id).isPresent()){
                                            if(discordApi.getChannelById(id).get().asServerTextChannel().isPresent()){
                                                discordApi.getChannelById(id).get().asServerTextChannel().get().delete();
                                            }
                                        }
                                    }
                                }.runTaskLater(plugin, 20*60*10);

                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if(textChannel != null)
                        messageComponentInteraction.createImmediateResponder().setContent(textChannel.getMentionTag()).setFlags(InteractionCallbackDataFlag.EPHEMERAL).respond();
                    return;
                }
            }
            if(event.getMessageComponentInteraction().getChannel().get().asServerTextChannel().get().getName().equals("whitelist") && event.getMessageComponentInteraction().getChannel().get().asServerTextChannel().get().getTopic().equals(messageComponentInteraction.getUser().getIdAsString())){
                if(customId.equals("cancel")){
                    event.getMessageComponentInteraction().getChannel().get().asServerTextChannel().get().delete();
                    return;
                }

                for (MinecraftUser user : plugin.getDatabase().getUsers()) {
                    if (user.getDiscordUser() != null) {
                        if (Objects.equals(user.getDiscordUser().getDiscordId(), event.getMessageComponentInteraction().getUser().getIdAsString())) {
                            if (!user.isRulesAccepted()) {
                                switch (customId) {
                                    case "accept" -> {
                                        messageComponentInteraction.createImmediateResponder()
                                                .setContent(plugin.messageUtil.getString("message.discord.rules_accepted"))
                                                .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                                                .respond();
                                        plugin.getLogger().info(user.getName() + " accepted the rules");
                                        user.setRulesAccepted(true);
                                        plugin.getDatabase().updateUser(user);

                                        Role role = event.getMessageComponentInteraction().getServer().get().getRoleById(plugin.getConfig().getString("discord.role")).orElse(null);
                                        if(role != null) {
                                            event.getMessageComponentInteraction().getUser().addRole(role);
                                        }
                                        messageComponentInteraction.getMessage().getChannel().asServerTextChannel().get().delete();
                                        return;
                                    }
                                    case "deny" -> {
                                        messageComponentInteraction.createImmediateResponder()
                                                .setContent(plugin.messageUtil.getString("message.discord.rules_denied"))
                                                .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                                                .respond();
                                        plugin.getLogger().info(user.getName() + " denied the rules");
                                        removeDiscordUser(user);
                                        messageComponentInteraction.getMessage().delete();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            plugin.getLogger().warning("failed interaction with id: "+messageComponentInteraction.getCustomId());
        }
    }

    public void giveRole(MinecraftUser user){
        discordApi.getServers().forEach(server -> {
            Role role = server.getRoleById(plugin.getConfig().getString("discord.role")).orElse(null);
            if(role != null) {
                if(user.getDiscordUser() != null){
                    if(server.getMemberById(user.getDiscordUser().getDiscordId()).isPresent()){
                        server.getMemberById(user.getDiscordUser().getDiscordId()).get().addRole(role);
                    }
                }
            }
        });
    }

    public void removeDiscordUser(MinecraftUser user) {
        if(user != null){

            discordApi.getServers().forEach(server -> {
                Role role = server.getRoleById(plugin.getConfig().getString("discord.role")).orElse(null);
                if(role != null) {
                    if(user.getDiscordUser() != null){
                        if(server.getMemberById(user.getDiscordUser().getDiscordId()).isPresent()){
                            server.getMemberById(user.getDiscordUser().getDiscordId()).get().removeRole(role);
                        }
                    }
                }
            });

            user.setDiscordUser(null);
            user.setRulesAccepted(false);
            new BukkitRunnable(){

                @Override
                public void run() {
                    OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(UUID.fromString(user.getUuid()));
                    if(offlinePlayer.isOnline()){
                        offlinePlayer.getPlayer().kick(plugin.messageUtil.getMessage("message.discord.quit"));
                    }

                }
            }.runTaskLater(plugin, 0);

            plugin.getDatabase().updateUser(user);
        }
    }

    public User getMember(DiscordUser user){
        for(Server server:discordApi.getServers()){
            Optional<User> optionalUser = server.getMemberById(user.getDiscordId());
            if(optionalUser.isPresent()){
                return optionalUser.get();
            }
        }
        return null;
    }

    public List<Role> getRoles(DiscordUser user){
        List<Role> roles = new ArrayList<>();
        for(Server server:discordApi.getServers()){
            Optional<User> optionalUser = server.getMemberById(user.getDiscordId());
            optionalUser.ifPresent(value -> roles.addAll(server.getRoles(value)));
        }
        return roles;
    }

    public boolean checkDiscord(DiscordUser user){
        return getMember(user) != null;
    }
}
