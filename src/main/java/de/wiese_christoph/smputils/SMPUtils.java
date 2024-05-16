package de.wiese_christoph.smputils;

import de.wiese_christoph.smputils.commands.VoteCommand;
import de.wiese_christoph.smputils.listeners.DeathCoordinatesListener;
import de.wiese_christoph.smputils.listeners.VoteListener;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SMPUtils extends JavaPlugin {
    public static final String Prefix = ChatColor.DARK_GRAY + "[" + ChatColor.DARK_PURPLE + "SMP Utils" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();

        PluginManager pluginManager = this.getServer().getPluginManager();

        VoteCommand voteCommand = new VoteCommand(
                config.getDouble("vote.minPlayerPercentage"),
                config.getInt("vote.cooldownSeconds"),
                config.getBoolean("vote.time.enabled"),
                config.getBoolean("vote.weather.enabled")
        );
        this.getCommand("vote").setExecutor(voteCommand);
        this.getCommand("vote").setTabCompleter(voteCommand);

        VoteListener voteListener = new VoteListener(voteCommand);
        pluginManager.registerEvents(voteListener, this);

        DeathCoordinatesListener deathCoordinatesListener = new DeathCoordinatesListener(
                config.getBoolean("deathCoordinates.enabled")
        );
        pluginManager.registerEvents(deathCoordinatesListener, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
