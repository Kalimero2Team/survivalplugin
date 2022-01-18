package com.kalimero2.team.survivalplugin.util;

import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.geysermc.cumulus.Form;
import org.geysermc.cumulus.SimpleForm;

import java.util.ArrayList;
import java.util.List;

public class IntroductionUtil {

    private final SurvivalPlugin plugin;

    public IntroductionUtil(SurvivalPlugin plugin){
        this.plugin = plugin;
    }

    public Book getBook(){
        List<Component> pages = new ArrayList<>();
        plugin.getConfig().getStringList("introduction").forEach(s -> pages.add(MiniMessage.get().parse(s, Template.of("br","\n"))));
        return Book.book(Component.text("introduction"), Component.text("Server"),pages);
    }

    public Form getForm(){
        List<Component> pages = new ArrayList<>();
        plugin.getConfig().getStringList("introduction").forEach(s -> pages.add(MiniMessage.get().parse(s, Template.of("br","\n"))));

        StringBuilder content = new StringBuilder();
        pages.forEach(component -> {content.append(LegacyComponentSerializer.legacySection().serialize(component));content.append("\n");});
        return SimpleForm.builder()
                .title(plugin.getConfig().getString("introduction-title","Introduction"))
                .content(content.toString()).responseHandler((customForm, s) -> {}).build();
    }
}
