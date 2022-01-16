package me.byquanton.survivalplugin.command;

import me.byquanton.survivalplugin.SurvivalPlugin;

public abstract class Command {
    protected final SurvivalPlugin plugin;
    protected final CommandManager commandManager;

    protected Command(
            final SurvivalPlugin plugin,
            final CommandManager commandManager
    ) {
        this.plugin = plugin;
        this.commandManager = commandManager;
    }

    public abstract void register();
}
