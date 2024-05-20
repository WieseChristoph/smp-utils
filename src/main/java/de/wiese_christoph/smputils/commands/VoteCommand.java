package de.wiese_christoph.smputils.commands;

import de.wiese_christoph.smputils.SMPUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class VoteCommand implements CommandExecutor, TabCompleter {
    private interface VoteState {}
    private enum VoteType {TIME, WEATHER}
    private enum TimeVoteState implements VoteState {DAY, NIGHT}
    private enum WeatherVoteState implements  VoteState {CLEAR, RAIN, THUNDER}

    private final HashMap<VoteType, HashMap<VoteState, ArrayList<UUID>>> votes = new HashMap<>();

    private LocalDateTime lastTimeVote;
    private LocalDateTime lastWeatherVote;

    private final double minPlayerPercentage;
    private final int cooldownSeconds;
    private final boolean timeVoteEnabled;
    private final boolean weatherVoteEnabled;

    public VoteCommand(double minPlayerPercentage, int cooldownSeconds, boolean timeVoteEnabled, boolean weatherVoteEnabled) {
        this.minPlayerPercentage = minPlayerPercentage;
        this.cooldownSeconds = cooldownSeconds;
        this.timeVoteEnabled = timeVoteEnabled;
        this.weatherVoteEnabled = weatherVoteEnabled;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (args.length < 2) return false;

        VoteType voteType = VoteType.valueOf(args[0].toUpperCase());
        VoteState voteState;
        try {
            voteState = TimeVoteState.valueOf(args[1].toUpperCase());
        } catch (Exception e) {
            try {
                voteState = WeatherVoteState.valueOf(args[1].toUpperCase());
            } catch (Exception ex) {
                return false;
            }
        }

        vote(player, voteType, voteState);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return switch (args.length) {
            case 1 -> Arrays.stream(VoteType.values())
                    .map(Enum::name)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
            case 2 -> {
                VoteType voteType = VoteType.valueOf(args[0].toUpperCase());
                yield Arrays.stream(voteType.equals(VoteType.TIME) ? TimeVoteState.values() : WeatherVoteState.values())
                        .map(Enum::name)
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());
            }
            default -> Collections.emptyList();
        };
    }

    private void vote(Player player, VoteType voteType, VoteState voteState) {
        // Check if weather/time vote is disabled.
        if ((voteType.equals(VoteType.TIME) && !timeVoteEnabled) || (voteType.equals(VoteType.WEATHER) && !weatherVoteEnabled)) {
            player.sendMessage(SMPUtils.Prefix + ChatColor.DARK_RED + "This vote is disabled!");
            return;
        }

        // Check for cooldown.
        LocalDateTime lastVote = voteType.equals(VoteType.TIME) ? lastTimeVote : lastWeatherVote;
        if (lastVote != null) {
            LocalDateTime lastVoteWithCooldown = lastVote.plusSeconds(cooldownSeconds);
            if (LocalDateTime.now().isBefore(lastVoteWithCooldown)) {
                player.sendMessage(SMPUtils.Prefix + ChatColor.DARK_RED + "This vote is on cooldown for " + LocalDateTime.now().until(lastVoteWithCooldown, ChronoUnit.SECONDS) + " Seconds!");
                return;
            }
        }

        if (!votes.containsKey(voteType)) {
            votes.put(voteType, new HashMap<>());
        }

        // Check if the player already voted for this type. If he voted for another state, remove him.
        for (Map.Entry<VoteState, ArrayList<UUID>> entry : votes.get(voteType).entrySet()) {
            if (entry.getValue().contains(player.getUniqueId())) {
                if (entry.getKey().equals(voteState)) {
                    player.sendMessage(SMPUtils.Prefix + ChatColor.DARK_RED + "You already voted for " + voteState.toString().toLowerCase() + (voteType.equals(VoteType.WEATHER) ? " weather" : " time") + "!");
                    return;
                }

                entry.getValue().remove(player.getUniqueId());
            }
        }

        if (!votes.get(voteType).containsKey(voteState)) {
            votes.get(voteType).put(voteState, new ArrayList<>());
        }

        votes.get(voteType).get(voteState).add(player.getUniqueId());

        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        Bukkit.broadcastMessage(SMPUtils.Prefix + ChatColor.GRAY + player.getDisplayName() + ChatColor.GOLD + " has voted for " + voteState.toString().toLowerCase() + (voteType.equals(VoteType.WEATHER) ? " weather" : " time") + "!" + ChatColor.DARK_RED + " (" + votes.get(voteType).get(voteState).size() + "/" + (int)Math.ceil(onlinePlayers * minPlayerPercentage) + ")");

        checkVotePass(voteType, voteState, player.getWorld());
    }

    private void checkVotePass(VoteType voteType, VoteState voteState, World world) {
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        if (onlinePlayers == 0) return;

        if (votes.get(voteType).get(voteState).size() >= (onlinePlayers * minPlayerPercentage)) {
            Bukkit.broadcastMessage(SMPUtils.Prefix + ChatColor.DARK_GREEN + "Enough people voted. Changing " + (voteType.equals(VoteType.TIME) ? "time" : "weather") + " to " + voteState.toString().toLowerCase() + "!");

            switch (voteType) {
                case TIME:
                    // Set time to day/night.
                    world.setTime(voteState.equals(TimeVoteState.DAY) ? 0L : 15000L);
                    lastTimeVote = LocalDateTime.now();
                    break;
                case WEATHER:
                    // Set weather to clear/rain/thunder.
                    world.setThundering(voteState.equals(WeatherVoteState.THUNDER));
                    world.setStorm(voteState.equals(WeatherVoteState.RAIN) || voteState.equals(WeatherVoteState.THUNDER));
                    lastWeatherVote = LocalDateTime.now();
                    break;
            }

            // Clear all votes from this type.
            for (Map.Entry<VoteState, ArrayList<UUID>> entry : votes.get(voteType).entrySet()) {
                entry.getValue().clear();
            }
        }
    }

    public void checkAllVotePass(World world) {
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        if (onlinePlayers == 0) return;

        for (Map.Entry<VoteType, HashMap<VoteState, ArrayList<UUID>>> voteTypeEntry : votes.entrySet()) {
            for (Map.Entry<VoteState, ArrayList<UUID>> voteStateEntry : voteTypeEntry.getValue().entrySet()) {
                checkVotePass(voteTypeEntry.getKey(), voteStateEntry.getKey(), world);
            }
        }
    }

    public void removePlayerFromAllVotes(Player player) {
        for (Map.Entry<VoteType, HashMap<VoteState, ArrayList<UUID>>> voteTypeEntry : votes.entrySet()) {
            for (Map.Entry<VoteState, ArrayList<UUID>> voteStateEntry : voteTypeEntry.getValue().entrySet()) {
                if (voteStateEntry.getValue().contains(player.getUniqueId())) {
                    voteStateEntry.getValue().remove(player.getUniqueId());
                    break;
                }
            }
        }
    }
}
