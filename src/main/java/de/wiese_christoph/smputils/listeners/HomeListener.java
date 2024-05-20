package de.wiese_christoph.smputils.listeners;

import de.wiese_christoph.smputils.commands.HomeCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class HomeListener implements Listener {
    private final HomeCommand homeCommand;

    public HomeListener(HomeCommand homeCommand) {
        this.homeCommand = homeCommand;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (event.getTo() != null &&
                (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                event.getFrom().getBlockY() != event.getTo().getBlockY() ||
                event.getFrom().getBlockZ() != event.getTo().getBlockZ())) {
            homeCommand.cancelTeleport(player);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            homeCommand.cancelTeleport(player);
        }
    }
}
