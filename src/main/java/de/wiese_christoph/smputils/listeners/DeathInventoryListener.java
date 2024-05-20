package de.wiese_christoph.smputils.listeners;

import de.wiese_christoph.smputils.SMPUtils;
import de.wiese_christoph.smputils.commands.DeathInventoryCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

public class DeathInventoryListener implements Listener {
    private final DeathInventoryCommand deathInventoryCommand;

    public DeathInventoryListener(DeathInventoryCommand deathInventoryCommand) {
        this.deathInventoryCommand = deathInventoryCommand;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        deathInventoryCommand.addDeathInventory(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity player = event.getWhoClicked();

        // Prevent non-op players from moving the items in the death inventory
        if (!player.isOp() && event.getView().getTitle().startsWith(deathInventoryCommand.INV_PREFIX)) {
            event.setCancelled(true);
            player.sendMessage(SMPUtils.Prefix + ChatColor.DARK_RED + "You must be a server operator to move items from a death inventory!");
        }
    }
}
