package com.example.tpaextension;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Arrays;
import java.util.Locale;

import static com.example.tpaextension.TPARequestManager.RequestType;

/** Observes successful command dispatches and adds UI around Essentials' TPA workflow. */
public class TPAListener implements Listener {

    private final TPAExtension plugin;

    public TPAListener(TPAExtension plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        ParsedCommand command = parse(event.getMessage());
        if (command == null) {
            return;
        }

        switch (command.name) {
            case "tpa" -> observeRequest(event.getPlayer(), command, RequestType.TPA);
            case "tpahere" -> observeRequest(event.getPlayer(), command, RequestType.TPA_HERE);
            case "tpaccept", "tpdeny" -> observeResponse(event.getPlayer(), command);
            case "tpacancel" -> plugin.getRequestManager().removeAllForRequester(event.getPlayer().getUniqueId());
            default -> {
                // Not part of the Essentials teleport request flow.
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getRequestManager().removeInvolving(event.getPlayer().getUniqueId());
    }

    private void observeRequest(Player requester, ParsedCommand command, RequestType type) {
        if (command.arguments.length < 1) {
            return;
        }
        Player target = plugin.getServer().getPlayerExact(command.arguments[0]);
        if (target == null || !target.isOnline() || target.getUniqueId().equals(requester.getUniqueId())) {
            return;
        }

        // Manual chat commands enter through this event; plugin GUI commands explicitly call the
        // same service because Player#performCommand can bypass PlayerCommandPreprocessEvent.
        plugin.getUiService().scheduleRequestNotification(requester, target, type);
    }

    private void observeResponse(Player target, ParsedCommand command) {
        if (command.arguments.length > 0) {
            Player requester = plugin.getServer().getPlayerExact(command.arguments[0]);
            if (requester != null) {
                plugin.getUiService().closeCancelForm(
                        requester.getUniqueId(), target.getUniqueId());
                plugin.getRequestManager().removeRequest(target.getUniqueId(), requester.getUniqueId());
                return;
            }
        }
        plugin.getRequestManager().findNewestForTarget(target.getUniqueId())
                .ifPresent(request -> {
                    plugin.getUiService().closeCancelForm(
                            request.getRequesterUUID(), target.getUniqueId());
                    plugin.getRequestManager().removeRequest(
                            target.getUniqueId(), request.getRequesterUUID());
                });
    }

    private ParsedCommand parse(String rawMessage) {
        if (rawMessage == null || rawMessage.length() < 2 || rawMessage.charAt(0) != '/') {
            return null;
        }
        String[] parts = rawMessage.substring(1).trim().split("\\s+");
        if (parts.length == 0 || parts[0].isBlank()) {
            return null;
        }
        String name = parts[0].toLowerCase(Locale.ROOT);
        int namespaceSeparator = name.indexOf(':');
        if (namespaceSeparator >= 0 && namespaceSeparator + 1 < name.length()) {
            name = name.substring(namespaceSeparator + 1);
        }
        return new ParsedCommand(name, Arrays.copyOfRange(parts, 1, parts.length));
    }

    private record ParsedCommand(String name, String[] arguments) {
    }
}
