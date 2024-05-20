package de.wiese_christoph.smputils.commands;

import de.wiese_christoph.smputils.SMPUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DeathInventoryCommand implements CommandExecutor {
    private final boolean deathInventoryEnabled;
    public final String INV_PREFIX = ChatColor.DARK_RED + "Death inventory";
    private final HashMap<String, ItemStack[]> deathInventories = new HashMap<>();

    public DeathInventoryCommand(boolean deathInventoryEnabled) {
        this.deathInventoryEnabled = deathInventoryEnabled;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (!deathInventoryEnabled) {
            player.sendMessage(SMPUtils.Prefix + ChatColor.DARK_RED + "Death inventories are disabled!");
            return true;
        }

        if (args.length == 0) {
            showDeathInventory(player, player);
        } else if (args.length == 1) {
            Player targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer != null) {
                if (targetPlayer == player || player.isOp()) {
                    showDeathInventory(player, targetPlayer);
                } else {
                    player.sendMessage(SMPUtils.Prefix + ChatColor.DARK_RED + "You must be a server operator to see the death inventories of other players!");
                }
            } else {
                player.sendMessage(SMPUtils.Prefix + ChatColor.DARK_RED + "No online player found with this username!");
            }
        } else return false;

        return true;
    }

    public void addDeathInventory(Player player) {
        List<ItemStack> items = new ArrayList<>();

        ItemStack[] mainContents = player.getInventory().getContents();
        ItemStack[] extraContents = player.getInventory().getExtraContents();

        Collections.addAll(items, mainContents);
        Collections.addAll(items, extraContents);

        if (!items.isEmpty()) {
            deathInventories.put(player.getName(), items.toArray(new ItemStack[0]));
        }
    }

    public void showDeathInventory(Player requestPlayer, Player targetPlayer) {
        if (!deathInventories.containsKey(targetPlayer.getName())) {
            requestPlayer.sendMessage(SMPUtils.Prefix + ChatColor.DARK_RED + "No inventory saved for player '" + targetPlayer.getDisplayName() + "'!");
            return;
        }

        Inventory deathInventory = Bukkit.createInventory(null, 45, INV_PREFIX + " of " + targetPlayer.getDisplayName());
        ItemStack[] deathInventoryItems = deathInventories.get(targetPlayer.getName());
        deathInventory.setContents(deathInventoryItems);

        requestPlayer.openInventory(deathInventory);
    }
}
