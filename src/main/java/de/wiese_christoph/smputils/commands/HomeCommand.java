package de.wiese_christoph.smputils.commands;

import de.wiese_christoph.smputils.SMPUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

public class HomeCommand implements CommandExecutor {
    private final SMPUtils plugin;
    private final boolean homeEnabled;
    private final int teleportDelaySeconds;
    private final HashMap<UUID, BukkitTask> commandTasks = new HashMap<>();

    public HomeCommand(SMPUtils plugin, boolean homeEnabled, int teleportDelaySeconds) {
        this.plugin = plugin;
        this.homeEnabled = homeEnabled;
        this.teleportDelaySeconds = teleportDelaySeconds;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (!homeEnabled) {
            player.sendMessage(SMPUtils.Prefix + ChatColor.DARK_RED + "The /home command is disabled!");
            return true;
        }

        Location respawnLocation = player.getRespawnLocation();
        if (respawnLocation == null) {
            player.sendMessage(SMPUtils.Prefix + ChatColor.DARK_RED + "Your respawn location is invalid! (You can set it at a bed)");
            return true;
        }

        Location worldSpawn = player.getWorld().getSpawnLocation();
        if (respawnLocation.equals(worldSpawn)) {
            player.sendMessage(SMPUtils.Prefix + ChatColor.DARK_RED + "No custom respawn location set! (You can set it at a bed)");
            return true;
        }

        if (teleportDelaySeconds > 0) {
            // Create a bukkit task to delay the command execution.
            BukkitTask commandTask = new BukkitRunnable() {
                @Override
                public void run() {
                    player.teleport(respawnLocation);
                    commandTasks.remove(player.getUniqueId());
                }
            }.runTaskLater(plugin, teleportDelaySeconds * 20L);

            commandTasks.put(player.getUniqueId(), commandTask);
            player.sendMessage(String.format(SMPUtils.Prefix + ChatColor.GOLD + "Teleporting in %d seconds.", teleportDelaySeconds));
        } else {
            player.teleport(respawnLocation);
        }

        return true;
    }

    public void cancelTeleport(Player player) {
        BukkitTask commandTask = commandTasks.get(player.getUniqueId());
        if (commandTask != null) {
            commandTask.cancel();
            commandTasks.remove(player.getUniqueId());
            player.sendMessage(SMPUtils.Prefix + ChatColor.DARK_RED + "Teleport was canceled because you moved or were attacked!");
        }
    }
}
