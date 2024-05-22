package de.wiese_christoph.smputils.commands;

import de.wiese_christoph.smputils.SMPUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;

public record RandomTeleportCommand(boolean randomTeleportEnabled) implements CommandExecutor {
    private static final String RANDOM_TELEPORT_DISABLED_MSG = SMPUtils.Prefix + ChatColor.DARK_RED + "Random teleport is disabled.";
    private static final String INVALID_DISTANCE_MSG = SMPUtils.Prefix + ChatColor.DARK_RED + "Invalid distance.";
    private static final String SAME_MIN_MAX_DISTANCE_MSG = SMPUtils.Prefix + ChatColor.DARK_RED + "The minimum distance can't be the same as the maximum distance.";
    private static final String MIN_GREATER_THAN_MAX_MSG = SMPUtils.Prefix + ChatColor.DARK_RED + "The minimum distance can't be greater than the maximum distance.";
    private static final String MAX_DISTANCE_WORLD_BORDER_MSG = SMPUtils.Prefix + ChatColor.DARK_RED + "The maximum distance is too high and intersects with the world border.";
    private static final String TELEPORT_MSG_FORMAT = SMPUtils.Prefix + ChatColor.GOLD + "Teleporting to (" +
            ChatColor.RED + "X" + ChatColor.GOLD + ", " +
            ChatColor.GREEN + "Y" + ChatColor.GOLD + ", " +
            ChatColor.BLUE + "Z" + ChatColor.GOLD + "): " +
            ChatColor.RED + "%.2f " +
            ChatColor.GREEN + "%.2f " +
            ChatColor.BLUE + "%.2f";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (!randomTeleportEnabled) {
            player.sendMessage(RANDOM_TELEPORT_DISABLED_MSG);
            return true;
        }
        if (args.length != 2) return false;

        double minDistance;
        double maxDistance;
        try {
            minDistance = Math.abs(Double.parseDouble(args[0]));
            maxDistance = Math.abs(Double.parseDouble(args[1]));
        } catch (NumberFormatException exception) {
            player.sendMessage(INVALID_DISTANCE_MSG);
            return false;
        }

        if (minDistance == maxDistance) {
            player.sendMessage(SAME_MIN_MAX_DISTANCE_MSG);
            return false;
        }

        if (minDistance > maxDistance) {
            player.sendMessage(MIN_GREATER_THAN_MAX_MSG);
            return false;
        }

        World world = player.getWorld();
        Location playerLocation = player.getLocation();
        if (!isWithinWorldBorder(playerLocation, maxDistance, world)) {
            player.sendMessage(MAX_DISTANCE_WORLD_BORDER_MSG);
            return false;
        }

        Location randomLocation = generateRandomLocation(playerLocation, minDistance, maxDistance, world);
        player.sendMessage(String.format(TELEPORT_MSG_FORMAT, randomLocation.getX(), randomLocation.getY(), randomLocation.getZ()));
        player.teleport(randomLocation);

        return true;
    }

    private boolean isWithinWorldBorder(Location location, double distance, World world) {
        WorldBorder worldBorder = world.getWorldBorder();
        double minX = worldBorder.getCenter().getX() - (worldBorder.getSize() / 2);
        double maxX = worldBorder.getCenter().getX() + (worldBorder.getSize() / 2);
        double minZ = worldBorder.getCenter().getZ() - (worldBorder.getSize() / 2);
        double maxZ = worldBorder.getCenter().getZ() + (worldBorder.getSize() / 2);

        return location.getX() + distance < maxX && location.getX() - distance > minX &&
                location.getZ() + distance < maxZ && location.getZ() - distance > minZ;
    }

    private Location generateRandomLocation(Location playerLocation, double minDistance, double maxDistance, World world) {
        double randomX = randomDoubleInRangeWithMinDistanceFromCenter(playerLocation.getX() - maxDistance, playerLocation.getX() + maxDistance, minDistance);
        double randomZ = randomDoubleInRangeWithMinDistanceFromCenter(playerLocation.getZ() - maxDistance, playerLocation.getZ() + maxDistance, minDistance);
        return new Location(world, randomX, world.getHighestBlockYAt((int) randomX, (int) randomZ) + 1, randomZ);
    }

    private double randomDoubleInRangeWithMinDistanceFromCenter(double min, double max, double minDistanceFromCenter) {
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
