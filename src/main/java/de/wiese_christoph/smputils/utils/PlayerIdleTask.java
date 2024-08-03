package de.wiese_christoph.smputils.utils;

import de.wiese_christoph.smputils.SMPUtils;
import de.wiese_christoph.smputils.exceptions.PlayerTaskAlreadyExistsException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A base class for implementing player idle tasks in Bukkit.
 * Subclasses can extend this class to create tasks that are executed when players
 * remain idle for a certain period of time.
 */
public abstract class PlayerIdleTask implements Listener {
    public enum CancelReason {
        Movement,
        Damage,
        Interaction,
        Overwrite
    }

    private final SMPUtils plugin;
    private final boolean overwriteTasks;
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();

    /**
     * Constructs a new PlayerIdleTask.
     *
     * @param plugin          The SMPUtils plugin instance.
     * @param overwriteTasks  Whether to overwrite existing tasks for the same player.
     */
    public PlayerIdleTask(SMPUtils plugin, boolean overwriteTasks) {
        this.plugin = plugin;
        this.overwriteTasks = overwriteTasks;
    }

    /**
     * Called when a player's idle task is finished.
     *
     * @param player The player associated with the idle task.
     */
    public abstract void onTaskFinished(Player player);

    /**
     * Called when a player's idle task is canceled.
     *
     * @param player       The player associated with the idle task.
     * @param cancelReason The reason why the task was canceled.
     */
    public abstract void onTaskCanceled(Player player, CancelReason cancelReason);

    /**
     * Adds a new idle task for the specified player.
     *
     * @param player       The player to add the task for.
     * @param runnable     The task to execute when the player is idle.
     * @param delaySeconds The delay before executing the task, in seconds.
     * @throws PlayerTaskAlreadyExistsException If a task already exists for the player and overwriteTasks is false.
     */
    public void addTask(Player player, Runnable runnable, long delaySeconds) throws PlayerTaskAlreadyExistsException {
        UUID playerId = player.getUniqueId();

        if (tasks.containsKey(playerId)) {
            if (overwriteTasks) {
                cancelTask(playerId);
                onTaskCanceled(player, CancelReason.Overwrite);
            } else throw new PlayerTaskAlreadyExistsException();
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();

                tasks.remove(player.getUniqueId());
                onTaskFinished(player);
            }
        }.runTaskLater(plugin, delaySeconds * 20L);

        tasks.put(playerId, task);
    }

    /**
     * Cancels the idle task for the specified player.
     *
     * @param playerId The UUID of the player whose task should be canceled.
     */
    public void cancelTask(UUID playerId) {
        BukkitTask task = tasks.get(playerId);
        if (task != null) {
            task.cancel();
            tasks.remove(playerId);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (
            tasks.containsKey(player.getUniqueId())
            && event.getTo() != null
            && !event.getFrom().getBlock().equals(event.getTo().getBlock())
        ) {
            cancelTask(player.getUniqueId());
            onTaskCanceled(player, CancelReason.Movement);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            UUID playerID = player.getUniqueId();

            if (tasks.containsKey(playerID)) {
                cancelTask(playerID);
                onTaskCanceled(player, CancelReason.Damage);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerID = player.getUniqueId();

        if (tasks.containsKey(playerID)) {
            cancelTask(playerID);
            onTaskCanceled(player, CancelReason.Interaction);
        }
    }
}
