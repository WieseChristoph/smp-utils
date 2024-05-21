package de.wiese_christoph.smputils.commands;

import de.wiese_christoph.smputils.SMPUtils;
import de.wiese_christoph.smputils.exceptions.PlayerTaskAlreadyExistsException;
import de.wiese_christoph.smputils.utils.PlayerIdleTask;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand extends PlayerIdleTask implements CommandExecutor {
    private final boolean homeEnabled;
    private final int teleportDelaySeconds;

    private static final String HOME_DISABLED_MSG = SMPUtils.Prefix + ChatColor.DARK_RED + "The /home command is disabled!";
    private static final String INVALID_LOCATION_MSG = SMPUtils.Prefix + ChatColor.DARK_RED + "Your respawn location is invalid or not set! (You can set it at a bed)";
    private static final String TELEPORT_DELAY_MSG_FORMAT = SMPUtils.Prefix + ChatColor.GOLD + "Teleporting in %d seconds.";
    private static final String TELEPORT_IMMEDIATE_MSG = SMPUtils.Prefix + ChatColor.GOLD + "Teleporting home...";
    private static final String TELEPORT_ALREADY_IN_PROGRESS_MSG = SMPUtils.Prefix + ChatColor.DARK_RED + "Teleport already in progress!";
    private static final String TELEPORT_FINISH_MSG = SMPUtils.Prefix + ChatColor.DARK_GREEN + "Successfully teleported home!";
    private static final String CANCEL_REASON_MSG_FORMAT = SMPUtils.Prefix + ChatColor.DARK_RED + "Teleport was canceled because you %s!";

    public HomeCommand(SMPUtils plugin, boolean homeEnabled, int teleportDelaySeconds) {
        super(plugin, false);
        this.homeEnabled = homeEnabled;
        this.teleportDelaySeconds = teleportDelaySeconds;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (!homeEnabled) {
            player.sendMessage(HOME_DISABLED_MSG);
            return true;
        }

        Location respawnLocation = player.getRespawnLocation();
        if (respawnLocation == null || respawnLocation.equals(player.getWorld().getSpawnLocation())) {
            player.sendMessage(INVALID_LOCATION_MSG);
            return true;
        }

        if (teleportDelaySeconds > 0) {
            Runnable commandRunnable = () -> player.teleport(respawnLocation);

            try {
                addTask(player, commandRunnable, teleportDelaySeconds);
                player.sendMessage(String.format(TELEPORT_DELAY_MSG_FORMAT, teleportDelaySeconds));
            } catch (PlayerTaskAlreadyExistsException exception) {
                player.sendMessage(TELEPORT_ALREADY_IN_PROGRESS_MSG);
                return true;
            }
        } else {
            player.teleport(respawnLocation);
            player.sendMessage(TELEPORT_IMMEDIATE_MSG);
        }

        return true;
    }

    @Override
    public void onTaskFinished(Player player) {
        player.sendMessage(TELEPORT_FINISH_MSG);
    }

    @Override
    public void onTaskCanceled(Player player, CancelReason cancelReason) {
        String reason;
        switch (cancelReason) {
            case Movement -> reason = "moved";
            case Damage -> reason = "took damage";
            case Overwrite -> reason = "executed the command again";
            default -> reason = "unknown reason";
        }

        player.sendMessage(String.format(CANCEL_REASON_MSG_FORMAT, reason));
    }
}
