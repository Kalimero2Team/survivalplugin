package com.kalimero2.team.survivalplugin.command;

import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import com.google.common.collect.ImmutableList;
import com.kalimero2.team.survivalplugin.SurvivalPlugin;
import org.bukkit.command.CommandSender;

import java.util.function.UnaryOperator;

public class CommandManager extends PaperCommandManager<CommandSender> {


    public CommandManager(final SurvivalPlugin plugin) throws Exception {
        super(
                plugin,
                CommandExecutionCoordinator.simpleCoordinator(),
                UnaryOperator.identity(),
                UnaryOperator.identity()
        );

        if (this.queryCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            try {
                this.registerBrigadier();
                final CloudBrigadierManager<?, ?> brigManager = this.brigadierManager();
                if (brigManager != null) {
                    brigManager.setNativeNumberSuggestions(false);
                }
            } catch (final Exception e) {
                plugin.getLogger().warning("Failed to initialize Brigadier support: " + e.getMessage());
            }
        }

        if (this.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            this.registerAsynchronousCompletions();
        }


        ImmutableList.of(
                new AdminCommand(plugin, this),
                new ChunkCommand(plugin, this),
                new DiscordCommand(plugin, this),
                new SpawnCommand(plugin,this),
                new MuteCommand(plugin, this),
                new IntroductionCommand(plugin, this),
                new BedCommand(plugin, this),
                new CraftingTableCommand(plugin, this),
                new EnderChestCommand(plugin, this)
        ).forEach(Command::register);


    }


}
