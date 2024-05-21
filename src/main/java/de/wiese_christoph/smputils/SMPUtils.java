package de.wiese_christoph.smputils;

import de.wiese_christoph.smputils.commands.DeathInventoryCommand;
import de.wiese_christoph.smputils.commands.HomeCommand;
import de.wiese_christoph.smputils.commands.SuicideCommand;
import de.wiese_christoph.smputils.commands.VoteCommand;
import de.wiese_christoph.smputils.listeners.DeathCoordinatesListener;
import de.wiese_christoph.smputils.listeners.DeathInventoryListener;
import de.wiese_christoph.smputils.listeners.HomeListener;
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

        /*
        *********************
        * Time/Weather Vote *
        *********************
        */
        boolean timeVoteEnabled = config.getBoolean("vote.time.enabled", true);
        boolean weatherVoteEnabled = config.getBoolean("vote.weather.enabled", true);
        double minPlayerVotePercentage = config.getDouble("vote.minPlayerPercentage", 0.33);
        int voteCooldown = config.getInt("vote.cooldownSeconds", 500);

        VoteCommand voteCommand = new VoteCommand(minPlayerVotePercentage, voteCooldown, timeVoteEnabled, weatherVoteEnabled);
        this.getCommand("vote").setExecutor(voteCommand);
        this.getCommand("vote").setTabCompleter(voteCommand);

        if (timeVoteEnabled || weatherVoteEnabled) {
            VoteListener voteListener = new VoteListener(voteCommand);
            pluginManager.registerEvents(voteListener, this);
        }

        /*
         *********************
         * Death Coordinates *
         *********************
         */
        boolean deathCoordinatesEnabled = config.getBoolean("deathCoordinates.enabled", true);

        if (deathCoordinatesEnabled) {
            DeathCoordinatesListener deathCoordinatesListener = new DeathCoordinatesListener();
            pluginManager.registerEvents(deathCoordinatesListener, this);
        }

        /*
         *******************
         * Death Inventory *
         *******************
         */
        boolean deathInventoryEnabled = config.getBoolean("deathInventory.enabled", true);

        DeathInventoryCommand deathInventoryCommand = new DeathInventoryCommand(deathInventoryEnabled);
        this.getCommand("di").setExecutor(deathInventoryCommand);

        if (deathInventoryEnabled) {
            DeathInventoryListener deathInventoryListener = new DeathInventoryListener(deathInventoryCommand);
            pluginManager.registerEvents(deathInventoryListener, this);
        }

        /*
         ********
         * Home *
         ********
         */
        boolean homeEnabled = config.getBoolean("home.enabled", true);
        int homeTeleportDelay = config.getInt("home.teleportDelaySeconds", 5);

        HomeCommand homeCommand = new HomeCommand(this, homeEnabled, homeTeleportDelay);
        this.getCommand("home").setExecutor(homeCommand);

        if (homeEnabled) {
            HomeListener homeListener = new HomeListener(homeCommand);
            pluginManager.registerEvents(homeListener, this);
        }

        /*
         ***********
         * Suicide *
         ***********
         */
        boolean suicideEnabled = config.getBoolean("suicide.enabled", true);

        SuicideCommand suicideCommand = new SuicideCommand(suicideEnabled);
        this.getCommand("suicide").setExecutor(suicideCommand);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
