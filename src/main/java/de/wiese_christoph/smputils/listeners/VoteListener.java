package de.wiese_christoph.smputils.listeners;

import de.wiese_christoph.smputils.commands.VoteCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public record VoteListener(VoteCommand voteCommand) implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        voteCommand.removePlayerFromAllVotes(player);
        voteCommand.checkAllVotePass(player.getWorld());
    }
}
