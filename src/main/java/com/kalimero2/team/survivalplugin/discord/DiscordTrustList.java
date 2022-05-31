package com.kalimero2.team.survivalplugin.discord;

import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import com.kalimero2.team.survivalplugin.database.MongoDB;
import com.kalimero2.team.survivalplugin.database.pojo.DiscordUser;
import com.kalimero2.team.survivalplugin.database.pojo.MinecraftUser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class DiscordTrustList extends ListenerAdapter {

    private final SurvivalPlugin plugin;
    private final MongoDB database;
    private final JDA jda;
    private final String whitelist_channel;
    private final String whitelist_category;
    private final String whitelist_add_channel_message;

    private Role whitelist_role;
    private Guild guild;

    public DiscordTrustList(SurvivalPlugin plugin, JDA jda){
        this.plugin = plugin;
        this.jda = jda;
        this.database = plugin.getDatabase();
        assert this.database != null;
        whitelist_channel = plugin.getConfig().getString("discord.whitelist_channel");
        whitelist_category = plugin.getConfig().getString("discord.whitelist_category");
        whitelist_add_channel_message = plugin.messageUtil.getString("message.discord.whitelist_add_channel");

        String roleId = plugin.getConfig().getString("discord.role");
        if(roleId == null){
            plugin.getLogger().warning("no role id found in config");
            return;
        }
        whitelist_role = jda.getRoleById(roleId);
        if(whitelist_role == null){
            plugin.getLogger().warning("role not found");
            return;
        }
        String serverId = plugin.getConfig().getString("discord.server");
        if(serverId == null){
            plugin.getLogger().warning("no server id found in config");
            return;
        }
        guild = jda.getGuildById(serverId);
        if(guild == null){
            plugin.getLogger().warning("server not found");
            return;
        }


        jda.addEventListener(this);

        sendTrustListMessage();
    }

    private void sendTrustListMessage(){
        TextChannel channel = jda.getTextChannelById(whitelist_channel);
        if(channel != null){

            AtomicBoolean isSend = new AtomicBoolean(false);
            channel.retrievePinnedMessages().queue(messages -> messages.forEach(message -> isSend.set(message.getAuthor().getId().equals(jda.getSelfUser().getId()))));

            if(!isSend.get()){
                channel.sendMessage(whitelist_add_channel_message).setActionRows(ActionRow.of(Button.primary("add", plugin.messageUtil.getString("message.discord.add")).asEnabled())).queue();
            }

        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        boolean isSelf = event.getAuthor().getId().equals(jda.getSelfUser().getId());
        if(event.getMessage().getChannel().getId().equals(whitelist_channel)){
            if(event.getMessage().getContentRaw().equals(whitelist_add_channel_message)){
                event.getMessage().pin().queue();
            }else {
                if(isSelf && event.getMessage().getType().equals(MessageType.CHANNEL_PINNED_ADD)){
                    event.getMessage().delete().queue();
                }
            }
        }

        if(event.getMessage().getAuthor().isBot()){
            return;
        }

        if(isSelf){
            return;
        }

        if(event.getChannel().getType().equals(ChannelType.TEXT)){
            TextChannel textChannel = event.getTextChannel();

            if(textChannel.getParentCategory() != null){
                if(textChannel.getId().equals(whitelist_channel) && textChannel.getParentCategory().getId().equals(whitelist_category)){
                    if(!textChannel.getName().equals("whitelist")){
                        return;
                    }

                    String code = event.getMessage().getContentStripped();

                    if(code.matches("-?\\d+")){

                        for(Map.Entry<String,MinecraftUser> entry: plugin.codeIdMap.entrySet()){
                            if(code.equals(entry.getKey())){
                                MinecraftUser user = entry.getValue();

                                plugin.codeIdMap.remove(code);
                                user.setDiscordUser(new DiscordUser(event.getMessage().getAuthor().getId()));
                                database.updateUser(user);

                                StringBuilder rules = new StringBuilder(plugin.messageUtil.getString("message.discord.rules_message"));
                                for(String rule:plugin.getConfig().getStringList("rules")){
                                    rules.append("\n").append(rule);
                                }

                                List<MinecraftUser> alts = database.getUserAlts(user);
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
                                    database.updateUser(user);
                                    event.getChannel().sendMessage(plugin.messageUtil.getString("message.discord.max_bedrock")).queue();
                                    return;
                                }if(java > 1 && !user.isBedrock()){
                                    user.setDiscordUser(null);
                                    database.updateUser(user);
                                    event.getChannel().sendMessage(plugin.messageUtil.getString("message.discord.max_java")).queue();
                                    return;
                                }

                                event.getChannel().sendMessage(rules).setActionRows(
                                        ActionRow.of(Button.success("accept", plugin.messageUtil.getString("message.discord.accept")),
                                                Button.danger("deny", plugin.messageUtil.getString("message.discord.deny")))
                                ).queue();


                            }
                        }
                    }

                }
            }
        }


    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        database.getUsers(event.getUser().getId()).forEach(this::removeDiscordUser);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        TextChannel textChannel = null;
        if(event.getComponentId().equals("add")){
            Optional<TextChannel> optionalTextChannel = guild.getTextChannelsByName("whitelist", false).stream().filter(channel -> Objects.equals(channel.getTopic(), event.getUser().getId())).findFirst();
            if(optionalTextChannel.isPresent()){
                textChannel = optionalTextChannel.get();
            }

            if(textChannel == null){
                Category category = guild.getCategoryById(whitelist_category);
                if(category != null){

                    EnumSet<Permission> allow = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);
                    textChannel = category.createTextChannel("whitelist").setTopic(event.getUser().getId()).addMemberPermissionOverride(event.getUser().getIdLong(), allow,null).complete();
                    textChannel.sendMessage(event.getUser().getAsMention()).queue();
                    textChannel.sendMessage(plugin.messageUtil.getString("message.discord.whitelist_code_channel")).
                            setActionRows(ActionRow.of(Button.primary("cancel", plugin.messageUtil.getString("message.discord.cancel")))).queue();


                    // Deletes Channel after 10 minutes
                    String id = textChannel.getId();
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            TextChannel channel = jda.getTextChannelById(id);
                            if (channel != null) {
                                channel.delete().queue();
                            }
                        }
                    }.runTaskLater(plugin, 20*60*10);
                }
            }


        }else if(event.getChannel().getName().equals("whitelist")){
            if(event.getComponentId().equals("cancel")){
                event.getChannel().delete().queue();
                return;
            }

            for(MinecraftUser user:database.getUsers(event.getUser().getId())){
                if(event.getComponentId().equals("accept")){
                    plugin.getLogger().info(event.getUser().getName() + "/"+user.getName()+" accepted the rules");
                    user.setRulesAccepted(true);
                    database.updateUser(user);
                    event.reply(plugin.messageUtil.getString("message.discord.rules_accepted")).setEphemeral(true).queue();
                    giveRole(user);
                } else if (event.getComponentId().equals("deny")){
                    plugin.getLogger().info(event.getUser().getName() + "/"+user.getName()+" denied the rules");
                    removeDiscordUser(user);
                    event.reply(plugin.messageUtil.getString("message.discord.rules_denied")).setEphemeral(true).queue();
                }
            }

        }


    }

    public void giveRole(MinecraftUser user){
        Member member = guild.getMemberById(user.getDiscordUser().getDiscordId());
        if(member == null){
            plugin.getLogger().warning("member not found");
            return;
        }
        guild.addRoleToMember(member, whitelist_role).queue();
    }

    public void removeDiscordUser(MinecraftUser user) {
        if(user != null){
            Member member = guild.getMemberById(user.getDiscordUser().getDiscordId());
            if(member == null){
                plugin.getLogger().warning("member not found");
                return;
            }
            if(user.getDiscordUser().getDiscordId() != null){
                guild.removeRoleFromMember(member, whitelist_role).queue();
            }
            user.setDiscordUser(null);
            user.setRulesAccepted(false);
            new BukkitRunnable(){

                @Override
                public void run() {
                    OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(UUID.fromString(user.getUuid()));
                    if(offlinePlayer.getPlayer() != null){
                        offlinePlayer.getPlayer().kick(plugin.messageUtil.getMessage("message.discord.quit"));
                    }

                }
            }.runTaskLater(plugin, 0);

            database.updateUser(user);
        }
    }

    public Member getMember(DiscordUser user){
        return guild.getMemberById(user.getDiscordId());
    }

    public List<Role> getRoles(DiscordUser user){
        Member member = guild.getMemberById(user.getDiscordId());
        if(member != null){
            return member.getRoles();
        }
        return null;
    }

    public boolean checkDiscord(DiscordUser user){
        return getMember(user) != null;
    }
}
