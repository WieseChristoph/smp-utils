package de.wiese_christoph.smputils.commands;

import de.wiese_christoph.smputils.SMPUtils;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class TeleportRequestCommand implements CommandExecutor {
    private final boolean teleportRequestEnabled;
    private enum TeleportRequestType {NORMAL, HERE}
    private final Map<UUID, Map<UUID, TeleportRequestType>> teleportRequests = new HashMap<>();

    private static final String TELEPORT_REQUEST_DISABLED_MSG = SMPUtils.Prefix + ChatColor.DARK_RED + "Teleport requests are disabled.";
    private static final String PLAYER_NOT_FOUND_MSG = SMPUtils.Prefix + ChatColor.DARK_RED + "No online player found with this username.";
    private static final String MULTIPLE_OPEN_REQUESTS_MSG = SMPUtils.Prefix + ChatColor.DARK_RED + "You have sent multiple teleport requests. Please specify the player or use '*' to cancel all requests.";
    private static final String NO_OPEN_TELEPORT_REQUESTS_MSG = SMPUtils.Prefix + ChatColor.DARK_RED + "You do not have any open teleport request.";
    private static final String NO_OPEN_TELEPORT_REQUEST_TO_PLAYER_MSG_FORMAT = SMPUtils.Prefix + ChatColor.DARK_RED + "You do not have an open teleport request to " + ChatColor.GRAY + "%s" + ChatColor.GOLD + ".";
    private static final String NO_OPEN_TELEPORT_REQUEST_FROM_PLAYER_MSG_FORMAT = SMPUtils.Prefix + ChatColor.DARK_RED + "You do not have an open teleport request from " + ChatColor.GRAY + "%s" + ChatColor.GOLD + ".";

    private static final String REQUEST_ALREADY_SENT_MSG_FORMAT = SMPUtils.Prefix + ChatColor.DARK_RED + "You already sent a teleport request to " + ChatColor.GRAY + "%s" + ChatColor.GOLD + ".";
    private static final String SENT_TELEPORT_REQUEST_MSG_FORMAT = SMPUtils.Prefix + ChatColor.GOLD + "Sent teleport request to " + ChatColor.GRAY + "%s" + ChatColor.GOLD + ".";
    private static final String RECEIVED_TELEPORT_REQUEST_NORMAL_MSG_FORMAT = SMPUtils.Prefix + ChatColor.GRAY + "%s" + ChatColor.GOLD + " has sent a request to teleport to you.";
    private static final String RECEIVED_TELEPORT_REQUEST_HERE_MSG_FORMAT = SMPUtils.Prefix + ChatColor.GRAY + "%s" + ChatColor.GOLD + " has sent a request to teleport to them.";

    private static final String TELEPORT_REQUEST_CANCELED_SENDER_MSG_FORMAT = SMPUtils.Prefix + ChatColor.GOLD + "Teleport request to " + ChatColor.GRAY + "%s" + ChatColor.GOLD + " has been canceled.";
    private static final String TELEPORT_REQUEST_CANCELED_RECEIVER_MSG_FORMAT = SMPUtils.Prefix + ChatColor.GRAY + "%s" + ChatColor.GOLD + " has canceled the teleport request.";
    private static final String ONLY_TELEPORT_REQUEST_CANCELED_SENDER_MSG = SMPUtils.Prefix + ChatColor.GOLD + "Teleport request has been canceled.";
    private static final String ALL_TELEPORT_REQUESTS_CANCELED_MSG = SMPUtils.Prefix + ChatColor.GOLD + "All open teleport requests have been canceled.";

    private static final String TELEPORT_REQUEST_ACCEPTED_SENDER_MSG_FORMAT = SMPUtils.Prefix + ChatColor.GRAY + "%s" + ChatColor.DARK_GREEN + " has accepted your teleport request.";
    private static final String TELEPORT_REQUEST_ACCEPTED_RECEIVER_MSG_FORMAT = SMPUtils.Prefix + ChatColor.DARK_GREEN + "Teleport request by " + ChatColor.GRAY + "%s" + ChatColor.DARK_GREEN + " has been accepted.";
    private static final String ALL_TELEPORT_REQUESTS_ACCEPTED_MSG = SMPUtils.Prefix + ChatColor.DARK_GREEN + "All open teleport requests have been accepted.";
    private static final String TELEPORT_REQUEST_ACCEPTED_PLAYER_NOT_FOUND_MSG = SMPUtils.Prefix + ChatColor.GOLD + "Can't find the player for the teleport (possibly disconnected).";

    private static final String TELEPORT_REQUEST_DENIED_SENDER_MSG_FORMAT = SMPUtils.Prefix + ChatColor.GRAY + "%s" + ChatColor.GOLD + " has denied your teleport request.";
    private static final String TELEPORT_REQUEST_DENIED_RECEIVER_MSG_FORMAT = SMPUtils.Prefix + ChatColor.GOLD + "Teleport request by " + ChatColor.GRAY + "%s" + ChatColor.GOLD + " has been denied.";
    private static final String ONLY_TELEPORT_REQUEST_DENIED_RECEIVER_MSG = SMPUtils.Prefix + ChatColor.GOLD + "Teleport request has been denied.";
    private static final String ALL_TELEPORT_REQUESTS_DENIED_MSG = SMPUtils.Prefix + ChatColor.GOLD + "All open teleport requests have been denied.";

    public TeleportRequestCommand(boolean teleportRequestEnabled) {
        this.teleportRequestEnabled = teleportRequestEnabled;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (!teleportRequestEnabled) {
            player.sendMessage(TELEPORT_REQUEST_DISABLED_MSG);
            return true;
        }
        if (args.length > 1) return false;

        Player targetPlayer = args.length == 1 && !args[0].equals("*") ? Bukkit.getPlayer(args[0]) : null;

        switch (command.getName().toLowerCase()) {
            case "tpa":
                if (args.length != 1) return false;
                if (targetPlayer != null) {
                    teleportAsk(player, targetPlayer, TeleportRequestType.NORMAL);
                } else player.sendMessage(PLAYER_NOT_FOUND_MSG);
                break;
            case "tpahere":
                if (args.length != 1) return false;
                if (targetPlayer != null) {
                    teleportAsk(player, targetPlayer, TeleportRequestType.HERE);
                } else player.sendMessage(PLAYER_NOT_FOUND_MSG);
                break;
            case "tpacancel":
                if (args.length == 1) {
                    if (args[0].equals("*")) teleportAskCancelAll(player);
                    else if (targetPlayer != null) teleportAskCancel(player, targetPlayer);
                    else player.sendMessage(PLAYER_NOT_FOUND_MSG);
                } else teleportAskCancel(player);
                break;
            case "tpaccept":
                if (args.length == 1) {
                    if (args[0].equals("*")) teleportAcceptAll(player);
                    else if (targetPlayer != null) teleportAccept(player, targetPlayer);
                    else player.sendMessage(PLAYER_NOT_FOUND_MSG);
                } else teleportAccept(player);
                break;
            case "tpdeny":
                if (args.length == 1) {
                    if (args[0].equals("*")) teleportDenyAll(player);
                    else if (targetPlayer != null) teleportDeny(player, targetPlayer);
                    else player.sendMessage(PLAYER_NOT_FOUND_MSG);
                } else teleportDeny(player);
                break;
            default:
                return false;
        }

        return true;
    }

    private void teleportAsk(Player player, Player targetPlayer, TeleportRequestType teleportRequestType) {
        Map<UUID, TeleportRequestType> targetPlayerRequests = teleportRequests.computeIfAbsent(targetPlayer.getUniqueId(), k -> new HashMap<>());
        if (targetPlayerRequests.containsKey(player.getUniqueId())) {
            player.sendMessage(String.format(REQUEST_ALREADY_SENT_MSG_FORMAT, targetPlayer.getDisplayName()));
            return;
        }

        targetPlayerRequests.put(player.getUniqueId(), teleportRequestType);

        player.sendMessage(String.format(SENT_TELEPORT_REQUEST_MSG_FORMAT, targetPlayer.getDisplayName()));

        // === Create request received message with clickable accept and deny buttons ===
        TextComponent receivedTeleportRequestMessage = new TextComponent(String.format(
                teleportRequestType.equals(TeleportRequestType.NORMAL)
                        ? RECEIVED_TELEPORT_REQUEST_NORMAL_MSG_FORMAT
                        : RECEIVED_TELEPORT_REQUEST_HERE_MSG_FORMAT,
                player.getDisplayName()
        ) + " - ");

        TextComponent acceptMessage = new TextComponent("Accept");
        acceptMessage.setColor(ChatColor.DARK_GREEN.asBungee());
        acceptMessage.setUnderlined(true);
        acceptMessage.setBold(true);
        acceptMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + player.getName()));
        acceptMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Accept the teleport request")));

        TextComponent denyMessage = new TextComponent("Deny");
        denyMessage.setColor(ChatColor.DARK_RED.asBungee());
        denyMessage.setUnderlined(true);
        denyMessage.setBold(true);
        denyMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny " + player.getName()));
        denyMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Deny the teleport request")));

        TextComponent divider = new TextComponent("/");
        divider.setColor(ChatColor.GOLD.asBungee());

        targetPlayer.spigot().sendMessage(
                receivedTeleportRequestMessage,
                acceptMessage,
                divider,
                denyMessage
        );
        // === End of request received message ===
    }

    private void teleportAskCancel(Player player, Player targetPlayer) {
        Map<UUID, TeleportRequestType> targetPlayerRequests = teleportRequests.get(targetPlayer.getUniqueId());
        if (targetPlayerRequests == null || !targetPlayerRequests.containsKey(player.getUniqueId())) {
            player.sendMessage(String.format(NO_OPEN_TELEPORT_REQUEST_TO_PLAYER_MSG_FORMAT, targetPlayer.getDisplayName()));
            return;
        }

        targetPlayerRequests.remove(player.getUniqueId());

        player.sendMessage(String.format(TELEPORT_REQUEST_CANCELED_SENDER_MSG_FORMAT, targetPlayer.getDisplayName()));
        targetPlayer.sendMessage(String.format(TELEPORT_REQUEST_CANCELED_RECEIVER_MSG_FORMAT, player.getDisplayName()));
    }

    private void teleportAskCancel(Player player) {
        UUID targetPlayerId = null;
        for (Map.Entry<UUID, Map<UUID, TeleportRequestType>> entry : teleportRequests.entrySet()) {
            if (!entry.getValue().containsKey(player.getUniqueId())) continue;
            if (targetPlayerId != null) {
                player.sendMessage(MULTIPLE_OPEN_REQUESTS_MSG);
                return;
            }
            targetPlayerId = entry.getKey();
        }

        if (targetPlayerId == null) {
            player.sendMessage(NO_OPEN_TELEPORT_REQUESTS_MSG);
            return;
        }

        teleportRequests.get(targetPlayerId).remove(player.getUniqueId());

        Player targetPlayer = Bukkit.getPlayer(targetPlayerId);
        if (targetPlayer != null) {
            player.sendMessage(String.format(TELEPORT_REQUEST_CANCELED_SENDER_MSG_FORMAT, targetPlayer.getDisplayName()));
            targetPlayer.sendMessage(String.format(TELEPORT_REQUEST_CANCELED_RECEIVER_MSG_FORMAT, player.getDisplayName()));
        } else player.sendMessage(ONLY_TELEPORT_REQUEST_CANCELED_SENDER_MSG);
    }

    private void teleportAskCancelAll(Player player) {
        boolean foundRequest = false;
        for (Map.Entry<UUID, Map<UUID, TeleportRequestType>> entry : teleportRequests.entrySet()) {
            Map<UUID, TeleportRequestType> teleportRequest = entry.getValue();
            if (teleportRequest.remove(player.getUniqueId()) == null) continue;

            foundRequest = true;
            Player canceledRequestPlayer = Bukkit.getPlayer(entry.getKey());
            if (canceledRequestPlayer != null) {
                canceledRequestPlayer.sendMessage(String.format(TELEPORT_REQUEST_CANCELED_RECEIVER_MSG_FORMAT, player.getDisplayName()));
            }
        }

        if (foundRequest) player.sendMessage(ALL_TELEPORT_REQUESTS_CANCELED_MSG);
        else player.sendMessage(NO_OPEN_TELEPORT_REQUESTS_MSG);
    }

    private void teleportAccept(Player player, Player targetPlayer) {
        Map<UUID, TeleportRequestType> playerRequests = teleportRequests.get(player.getUniqueId());
        if (playerRequests == null || !playerRequests.containsKey(targetPlayer.getUniqueId())) {
            player.sendMessage(String.format(NO_OPEN_TELEPORT_REQUEST_FROM_PLAYER_MSG_FORMAT, targetPlayer.getDisplayName()));
            return;
        }

        TeleportRequestType type = playerRequests.remove(targetPlayer.getUniqueId());
        teleportPlayer(player, targetPlayer, type);

        player.sendMessage(String.format(TELEPORT_REQUEST_ACCEPTED_RECEIVER_MSG_FORMAT, targetPlayer.getDisplayName()));
        targetPlayer.sendMessage(String.format(TELEPORT_REQUEST_ACCEPTED_SENDER_MSG_FORMAT, player.getDisplayName()));
    }

    private void teleportAccept(Player player) {
        UUID targetPlayerId = null;
        for (Map.Entry<UUID, Map<UUID, TeleportRequestType>> entry : teleportRequests.entrySet()) {
            if (!entry.getValue().containsKey(player.getUniqueId())) continue;
            if (targetPlayerId != null) {
                player.sendMessage(MULTIPLE_OPEN_REQUESTS_MSG);
                return;
            }
            targetPlayerId = entry.getKey();
        }

        if (targetPlayerId == null) {
            player.sendMessage(NO_OPEN_TELEPORT_REQUESTS_MSG);
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(targetPlayerId);
        if (targetPlayer == null) {
            player.sendMessage(TELEPORT_REQUEST_ACCEPTED_PLAYER_NOT_FOUND_MSG);
            return;
        }

        TeleportRequestType type = teleportRequests.get(targetPlayerId).remove(player.getUniqueId());
        teleportPlayer(player, targetPlayer, type);

        player.sendMessage(String.format(TELEPORT_REQUEST_ACCEPTED_RECEIVER_MSG_FORMAT, targetPlayer.getDisplayName()));
        targetPlayer.sendMessage(String.format(TELEPORT_REQUEST_ACCEPTED_SENDER_MSG_FORMAT, player.getDisplayName()));
    }

    private void teleportAcceptAll(Player player) {
        boolean foundRequests = false;
        for (Map.Entry<UUID, Map<UUID, TeleportRequestType>> entry : teleportRequests.entrySet()) {
            Map<UUID, TeleportRequestType> playerRequests = entry.getValue();
            if (!playerRequests.containsKey(player.getUniqueId())) continue;

            Player targetPlayer = Bukkit.getPlayer(entry.getKey());
            if (targetPlayer == null) continue;

            foundRequests = true;
            TeleportRequestType type = playerRequests.remove(player.getUniqueId());
            teleportPlayer(player, targetPlayer, type);
            targetPlayer.sendMessage(String.format(TELEPORT_REQUEST_ACCEPTED_SENDER_MSG_FORMAT, player.getDisplayName()));
        }

        if (foundRequests) player.sendMessage(ALL_TELEPORT_REQUESTS_ACCEPTED_MSG);
        else player.sendMessage(NO_OPEN_TELEPORT_REQUESTS_MSG);
    }

    private void teleportDeny(Player player, Player targetPlayer) {
        Map<UUID, TeleportRequestType> playerRequests = teleportRequests.get(player.getUniqueId());
        if (playerRequests == null || !playerRequests.containsKey(targetPlayer.getUniqueId())) {
            player.sendMessage(String.format(NO_OPEN_TELEPORT_REQUEST_FROM_PLAYER_MSG_FORMAT, targetPlayer.getDisplayName()));
            return;
        }

        playerRequests.remove(targetPlayer.getUniqueId());

        player.sendMessage(String.format(TELEPORT_REQUEST_DENIED_RECEIVER_MSG_FORMAT, targetPlayer.getDisplayName()));
        targetPlayer.sendMessage(String.format(TELEPORT_REQUEST_DENIED_SENDER_MSG_FORMAT, player.getDisplayName()));
    }

    private void teleportDeny(Player player) {
        UUID targetPlayerId = null;
        for (Map.Entry<UUID, Map<UUID, TeleportRequestType>> entry : teleportRequests.entrySet()) {
            if (!entry.getValue().containsKey(player.getUniqueId())) continue;
            if (targetPlayerId != null) {
                player.sendMessage(MULTIPLE_OPEN_REQUESTS_MSG);
                return;
            }
            targetPlayerId = entry.getKey();
        }

        if (targetPlayerId == null) {
            player.sendMessage(NO_OPEN_TELEPORT_REQUESTS_MSG);
            return;
        }

        teleportRequests.get(targetPlayerId).remove(player.getUniqueId());

        Player targetPlayer = Bukkit.getPlayer(targetPlayerId);
        if (targetPlayer != null) {
            player.sendMessage(String.format(TELEPORT_REQUEST_DENIED_RECEIVER_MSG_FORMAT, targetPlayer.getDisplayName()));
            targetPlayer.sendMessage(String.format(TELEPORT_REQUEST_DENIED_SENDER_MSG_FORMAT, player.getDisplayName()));
        } else player.sendMessage(ONLY_TELEPORT_REQUEST_DENIED_RECEIVER_MSG);
    }

    private void teleportDenyAll(Player player) {
        boolean foundRequests = false;
        for (Map.Entry<UUID, Map<UUID, TeleportRequestType>> entry : teleportRequests.entrySet()) {
            if (entry.getValue().remove(player.getUniqueId()) == null) continue;

            foundRequests = true;
            Player targetPlayer = Bukkit.getPlayer(entry.getKey());
            if (targetPlayer != null) {
                targetPlayer.sendMessage(String.format(TELEPORT_REQUEST_DENIED_SENDER_MSG_FORMAT, player.getDisplayName()));
            }
        }

        if (foundRequests) player.sendMessage(ALL_TELEPORT_REQUESTS_DENIED_MSG);
        else player.sendMessage(NO_OPEN_TELEPORT_REQUESTS_MSG);
    }

    private void teleportPlayer(Player player, Player targetPlayer, TeleportRequestType teleportRequestType) {
        switch (teleportRequestType) {
            case NORMAL -> player.teleport(targetPlayer);
            case HERE -> targetPlayer.teleport(player);
        }
    }
}
