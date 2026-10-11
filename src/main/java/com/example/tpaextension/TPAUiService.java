package com.example.tpaextension;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.example.tpaextension.TPARequestManager.RequestType;

/** Renders request/response UI while Essentials remains the teleport authority. */
public class TPAUiService {

    private final TPAExtension plugin;

    public TPAUiService(TPAExtension plugin) {
        this.plugin = plugin;
    }

    /**
     * Dispatches an Essentials request from a plugin GUI and explicitly schedules the matching UI.
     * Player#performCommand does not reliably fire PlayerCommandPreprocessEvent, so GUI requests
     * must not depend on that event to reach the notification path.
     */
    public void sendRequest(Player requester, Player target, RequestType type) {
        String command = type == RequestType.TPA ? "tpa " : "tpahere ";
        if (requester.performCommand(command + target.getName())) {
            scheduleRequestNotification(requester, target, type);
        }
    }

    public void scheduleRequestNotification(Player requester, Player target, RequestType type) {
        java.util.UUID requesterUUID = requester.getUniqueId();
        java.util.UUID targetUUID = target.getUniqueId();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Player onlineRequester = plugin.getServer().getPlayer(requesterUUID);
            Player onlineTarget = plugin.getServer().getPlayer(targetUUID);
            if (onlineRequester != null && onlineRequester.isOnline()
                    && onlineTarget != null && onlineTarget.isOnline()) {
                notifyRequest(onlineRequester, onlineTarget, type);
            }
        }, 3L);
    }

    public void notifyRequest(Player requester, Player target, RequestType type) {
        // A command issued by a GUI may also be observed by another command pipeline on some
        // server builds. Do not render duplicate chat buttons or Floodgate forms.
        if (plugin.getRequestManager().hasRequest(target.getUniqueId(), requester.getUniqueId())) {
            return;
        }
        plugin.getRequestManager().addRequest(requester, target, type);
        sendCancelUi(requester, target);
        sendResponseUi(requester, target, type);
    }

    protected void sendCancelUi(Player requester, Player target) {
        Component message = Component.text()
                .append(Component.text("已向 ", NamedTextColor.GREEN))
                .append(Component.text(target.getName(), NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(" 发送传送请求  ", NamedTextColor.GREEN))
                .append(Component.text("[取消请求]", NamedTextColor.RED, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/tpacancel")))
                .build();
        requester.sendMessage(message);
    }

    protected void sendResponseUi(Player requester, Player target, RequestType type) {
        sendJavaResponseMessage(requester, target, type);
    }

    protected void sendJavaResponseMessage(Player requester, Player target, RequestType type) {
        String direction = type == RequestType.TPA
                ? " 请求传送到你身边"
                : " 请求你传送到他身边";
        Component message = Component.text()
                .append(Component.text(requester.getName(), NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(direction + "  ", NamedTextColor.YELLOW))
                .append(Component.text("[接受]", NamedTextColor.GREEN, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/tpaccept " + requester.getName())))
                .append(Component.text("  "))
                .append(Component.text("[拒绝]", NamedTextColor.RED, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/tpdeny " + requester.getName())))
                .build();
        target.sendMessage(message);
    }

    public boolean isBedrockPlayer(UUID uuid) { return false; }

    public void closeCancelForm(UUID requesterUUID, UUID targetUUID) {
        // Native forms are available only in the optional Floodgate service.
    }
}
