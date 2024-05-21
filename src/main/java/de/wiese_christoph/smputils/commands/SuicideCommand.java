package de.wiese_christoph.smputils.commands;

import de.wiese_christoph.smputils.SMPUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SuicideCommand implements CommandExecutor {
    private final boolean suicideEnabled;

    public SuicideCommand(boolean suicideEnabled) {
        this.suicideEnabled = suicideEnabled;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (!suicideEnabled) {
            player.sendMessage(SMPUtils.Prefix + ChatColor.DARK_RED + "The /suicide command is disabled!");
            return true;
        }

        player.setHealth(0);
        return true;
    }
}
