package me.byquanton.survivalplugin.util;


import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordConsole extends AbstractAppender {

    public WebhookClient client;

    public DiscordConsole(String url){
        super("DiscordConsole",null,null,false, Property.EMPTY_ARRAY);

        WebhookClientBuilder builder = new WebhookClientBuilder(url);
        builder.setThreadFactory((job) -> {
            Thread thread = new Thread(job);
            thread.setName("DiscordConsole-Webhook");
            thread.setDaemon(true);
            return thread;
        });
        builder.setWait(true);
        client = builder.build();

        ((Logger) LogManager.getRootLogger()).addAppender(this);

        super.start();
    }


    @Override
    public void append(LogEvent event) {

        var ref = new Object() {
            String message = event.getMessage().getFormattedMessage();
        };
        String IPADDRESS_PATTERN =
                "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(ref.message);

        matcher.results().forEach(matchResult -> ref.message = ref.message.replaceAll(matchResult.group(), "<hidden ip>"));
        ref.message = ref.message.replaceAll("@","{at}");

        if(!ref.message.isEmpty()){
            client.send(ref.message);
        }

    }

    public void sendUpdateEmbed(String title, String message){
        WebhookEmbed embed = new WebhookEmbedBuilder()
                .setColor(0xFF00EE)
                .setDescription(message)
                .setTitle(new WebhookEmbed.EmbedTitle(title,""))
                .build();
        client.send(embed);

    }
}
