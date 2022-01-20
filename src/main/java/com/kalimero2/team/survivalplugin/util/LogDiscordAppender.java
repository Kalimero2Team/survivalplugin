package com.kalimero2.team.survivalplugin.util;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogDiscordAppender extends AbstractAppender {

    private WebhookClient client;
    private final String url;
    private boolean connected;

    public LogDiscordAppender(String url){
        super("LogDiscordAppender",null,null,false, Property.EMPTY_ARRAY);

        this.url = url;
        this.connected = false;

        ((Logger) LogManager.getRootLogger()).addAppender(this);

        super.start();
    }

    public boolean connect(){
        if(!connected){
            WebhookClientBuilder builder;
            try{
                builder = new WebhookClientBuilder(url);
            }catch (IllegalArgumentException ignored){ // java.lang.IllegalArgumentException: Failed to parse webhook URL (When no Url is provided)
                return false;
            }

            builder.setThreadFactory((job) -> {
                Thread thread = new Thread(job);
                thread.setName("LogDiscordAppender-Webhook");
                thread.setDaemon(true);
                return thread;
            });
            builder.setWait(true);
            client = builder.build();
            connected = true;
            return true;
        }
        return false;
    }


    @Override
    public void append(LogEvent event) {
        if(connected){
            String message = event.getMessage().getFormattedMessage();


            String IPADDRESS_PATTERN =
                    "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

            Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
            Matcher matcher = pattern.matcher(message);

            for(MatchResult matchResult: matcher.results().toList()){
                message = message.replaceAll(matchResult.group(), "<hidden ip>");
            }

            message = message.replaceAll("@","{at}");

            if(!message.isEmpty()){
                client.send(message);
            }
        }
    }

    public void sendUpdateEmbed(String title, String message){
        if(connected){
            WebhookEmbed embed = new WebhookEmbedBuilder()
                    .setColor(0xFF00EE)
                    .setDescription(message)
                    .setTitle(new WebhookEmbed.EmbedTitle(title,""))
                    .build();
            client.send(embed);
        }
    }

    public void sendMessage(String message){
        client.send(message);
    }
}
