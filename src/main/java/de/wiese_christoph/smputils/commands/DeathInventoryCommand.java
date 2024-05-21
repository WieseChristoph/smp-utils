package de.wiese_christoph.smputils.commands;

import de.wiese_christoph.smputils.SMPUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DeathInventoryCommand implements CommandExecutor {
    private final boolean deathInventoryEnabled;
    private final Map<UUID, ItemStack[]> deathInventories = new HashMap<>();

    public static final String INVENTORY_PREFIX = ChatColor.DARK_RED + "Death inventory";
    private static final String DEATH_INVENTORY_DISABLED_MSG = SMPUtils.Prefix + ChatColor.DARK_RED + "Death inventories are disabled!";
    private static final String NO_OP_MSG = SMPUtils.Prefix + ChatColor.DARK_RED + "You must be a server operator to see the death inventories of other players!";
    private static final String PLAYER_NOT_FOUND_MSG = SMPUtils.Prefix + ChatColor.DARK_RED + "No online player found with this username!";
    private static final String NO_INVENTORY_MSG_FORMAT = SMPUtils.Prefix + ChatColor.DARK_RED + "No inventory saved for player '%s'!";

    public DeathInventoryCommand(boolean deathInventoryEnabled) {
        this.deathInventoryEnabled = deathInventoryEnabled;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (!deathInventoryEnabled) {
            player.sendMessage(DEATH_INVENTORY_DISABLED_MSG);
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
                    player.sendMessage(NO_OP_MSG);
                }
            } else {
                player.sendMessage(PLAYER_NOT_FOUND_MSG);
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
            deathInventories.put(player.getUniqueId(), items.toArray(new ItemStack[0]));
        }
    }

    public void showDeathInventory(Player requestPlayer, Player targetPlayer) {
        if (!deathInventories.containsKey(targetPlayer.getUniqueId())) {
            requestPlayer.sendMessage(String.format(NO_INVENTORY_MSG_FORMAT, targetPlayer.getDisplayName()));
            return;
        }

        Inventory deathInventory = Bukkit.createInventory(null, 45, INVENTORY_PREFIX + " of " + targetPlayer.getDisplayName());
        ItemStack[] deathInventoryItems = deathInventories.get(targetPlayer.getUniqueId());
        deathInventory.setContents(deathInventoryItems);

        requestPlayer.openInventory(deathInventory);
    }

    public static boolean isDeathInventory(InventoryView inventoryView) {
        return inventoryView.getTitle().startsWith(INVENTORY_PREFIX);
    }
}
