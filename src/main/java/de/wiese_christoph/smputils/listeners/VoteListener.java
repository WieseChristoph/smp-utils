package de.wiese_christoph.smputils.listeners;

import de.wiese_christoph.smputils.commands.VoteCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class VoteListener implements Listener {
    private final VoteCommand voteCommand;

    public VoteListener(VoteCommand voteCommand) {
        this.voteCommand = voteCommand;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.voteCommand.removePlayerFromAllVotes(player);
        this.voteCommand.checkAllVotePass(player.getWorld());
    }
}
