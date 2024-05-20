package de.wiese_christoph.smputils.listeners;

import de.wiese_christoph.smputils.SMPUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathCoordinatesListener implements Listener {
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location location = player.getLocation();
        player.sendMessage(String.format(SMPUtils.Prefix + ChatColor.GOLD + "You died at coordinates (" + ChatColor.RED + "X" + ChatColor.GREEN + "Y" + ChatColor.BLUE + "Z" + ChatColor.GOLD + "): " + ChatColor.RED + "%.2f " + ChatColor.GREEN + "%.2f " + ChatColor.BLUE + "%.2f", location.getX(), location.getY(), location.getZ()));
    }
}
