package com.kalimero2.team.survivalplugin.command;

import com.kalimero2.team.survivalplugin.SurvivalPlugin;

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
