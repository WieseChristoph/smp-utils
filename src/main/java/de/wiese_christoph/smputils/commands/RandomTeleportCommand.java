package de.wiese_christoph.smputils.commands;

import de.wiese_christoph.smputils.SMPUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;

public record RandomTeleportCommand(boolean randomTeleportEnabled) implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (!randomTeleportEnabled) {
            player.sendMessage(SMPUtils.Prefix + ChatColor.DARK_RED + "Random teleport is disabled!");
            return true;
        }
        if (args.length != 2) return false;

        double minDistance;
        double maxDistance;
        try {
            minDistance = Math.abs(Double.parseDouble(args[0]));
            maxDistance = Math.abs(Double.parseDouble(args[1]));
        } catch (NumberFormatException exception) {
            player.sendMessage(SMPUtils.Prefix + ChatColor.DARK_RED + "Invalid distance!");
            return false;
        }

        if (minDistance == maxDistance) {
            player.sendMessage(SMPUtils.Prefix + ChatColor.DARK_RED + "The minimum distance cant be the same as the maximum distance!");
            return false;
        }

        if (minDistance > maxDistance) {
            player.sendMessage(SMPUtils.Prefix + ChatColor.DARK_RED + "The minimum distance cant be greater than the maximum distance!");
            return false;
        }

        World world = player.getWorld();
        WorldBorder worldBorder = world.getWorldBorder();
        double worldBorderMinX = worldBorder.getCenter().getX() - (worldBorder.getSize() / 2);
        double worldBorderMaxX = worldBorder.getCenter().getX() + (worldBorder.getSize() / 2);
        double worldBorderMinZ = worldBorder.getCenter().getZ() - (worldBorder.getSize() / 2);
        double worldBorderMaxZ = worldBorder.getCenter().getZ() + (worldBorder.getSize() / 2);

        Location playerLocation = player.getLocation();
        if (playerLocation.getX() + maxDistance >= worldBorderMaxX ||
                playerLocation.getX() - maxDistance <= worldBorderMinX ||
                playerLocation.getZ() + maxDistance >= worldBorderMaxZ ||
                playerLocation.getZ() - maxDistance <= worldBorderMinZ
        ) {
            player.sendMessage(SMPUtils.Prefix + ChatColor.DARK_RED + "The maximum distance is too high and intersects with the world border!");
            return false;
        }

        // Get random location.
        double randomX = randomDoubleInRangeWithMinDistanceFromCenter(playerLocation.getX() - maxDistance, playerLocation.getX() + maxDistance, minDistance);
        double randomZ = randomDoubleInRangeWithMinDistanceFromCenter(playerLocation.getZ() - maxDistance, playerLocation.getZ() + maxDistance, minDistance);
        Location randomLocation = new Location(world, randomX, world.getHighestBlockYAt((int) randomX, (int) randomZ) + 1, randomZ);

        // Generate chunk at random location.
        Chunk randomLocationChunk = world.getChunkAt(randomLocation);
        world.loadChunk(randomLocationChunk);

        player.sendMessage(String.format(SMPUtils.Prefix + ChatColor.GOLD + "Teleporting to (" + ChatColor.RED + "X" + ChatColor.GREEN + "Y" + ChatColor.BLUE + "Z" + ChatColor.GOLD + "): " + ChatColor.RED + "%.2f " + ChatColor.GREEN + "%.2f " + ChatColor.BLUE + "%.2f", randomLocation.getX(), randomLocation.getY(), randomLocation.getZ()));
        player.teleport(randomLocation);

        return true;
    }

    private double randomDoubleInRangeWithMinDistanceFromCenter(double min, double max, double minDistanceFromCenter) {
        if (min > max) throw new IllegalArgumentException("No valid range exists with the given constraints.");
        if (minDistanceFromCenter < 0)
            throw new IllegalArgumentException("Minimum distance from zero must be non-negative.");

        double center = (min + max) / 2;

        if (center + minDistanceFromCenter >= max || center - minDistanceFromCenter <= min)
            throw new IllegalArgumentException("No valid range exists with the given constraints.");

        // Randomly select the positive or negative range and generate a random number within the given constraints.
        Random random = new Random();
        if (random.nextBoolean()) {
            double minWithDistance = center + minDistanceFromCenter;
            return minWithDistance + (max - minWithDistance) * random.nextDouble();
        } else {
            double maxWithDistance = center - minDistanceFromCenter;
            return min + (maxWithDistance - min) * random.nextDouble();
        }
    }
}
