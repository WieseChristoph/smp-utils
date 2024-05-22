package de.wiese_christoph.smputils.commands;

import de.wiese_christoph.smputils.SMPUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public record SuicideCommand(boolean suicideEnabled) implements CommandExecutor {
    private static final String SUICIDE_DISABLED_MSG = SMPUtils.Prefix + ChatColor.DARK_RED + "Suicide is disabled.";
    private static final String SUICIDE_SUCCESS_MSG = SMPUtils.Prefix + ChatColor.DARK_GREEN + "You have ended your own life.";
    private static final String ALREADY_DEAD_MSG = SMPUtils.Prefix + ChatColor.RED + "You are already dead.";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (!suicideEnabled) {
            player.sendMessage(SUICIDE_DISABLED_MSG);
            return true;
        }

        if (player.getHealth() > 0) {
            player.setHealth(0);
            player.sendMessage(SUICIDE_SUCCESS_MSG);
        } else {
            player.sendMessage(ALREADY_DEAD_MSG);
        }

        return true;
    }
}
