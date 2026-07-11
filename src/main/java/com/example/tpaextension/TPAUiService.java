package com.example.tpaextension;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.tpaextension.TPARequestManager.RequestType;

/** Renders request/response UI while Essentials remains the teleport authority. */
public class TPAUiService {

    private final TPAExtension plugin;
    private final Map<UUID, UUID> openCancelForms = new ConcurrentHashMap<>();

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

    private void sendCancelUi(Player requester, Player target) {
        if (plugin.isBedrockPlayer(requester.getUniqueId())) {
            FloodgatePlayer floodgatePlayer = plugin.getFloodgateApi().getPlayer(requester.getUniqueId());
            if (floodgatePlayer != null) {
                SimpleForm form = SimpleForm.builder()
                        .title("传送请求已发送")
                        .content("§7你已向 §e" + target.getName()
                                + "§7 发送传送请求\n\n§7等待对方响应...")
                        .button("§c取消请求")
                        .button("§c关闭")
                        .closedResultHandler(() ->
                                openCancelForms.remove(requester.getUniqueId(), target.getUniqueId()))
                        .validResultHandler(response -> plugin.getServer().getScheduler().runTask(plugin, () -> {
                            openCancelForms.remove(requester.getUniqueId(), target.getUniqueId());
                            if (response.clickedButtonId() == 0 && requester.isOnline()) {
                                if (requester.performCommand("tpacancel")) {
                                    plugin.getRequestManager().removeAllForRequester(requester.getUniqueId());
                                }
                            }
                        }))
                        .build();
                openCancelForms.put(requester.getUniqueId(), target.getUniqueId());
                floodgatePlayer.sendForm(form);
                return;
            }
        }

        Component message = Component.text()
                .append(Component.text("已向 ", NamedTextColor.GREEN))
                .append(Component.text(target.getName(), NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(" 发送传送请求  ", NamedTextColor.GREEN))
                .append(Component.text("[取消请求]", NamedTextColor.RED, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/tpacancel")))
                .build();
        requester.sendMessage(message);
    }

    private void sendResponseUi(Player requester, Player target, RequestType type) {
        if (plugin.isBedrockPlayer(target.getUniqueId())) {
            sendBedrockResponseForm(requester, target, type);
        } else {
            sendJavaResponseMessage(requester, target, type);
        }
    }

    private void sendJavaResponseMessage(Player requester, Player target, RequestType type) {
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

    private void sendBedrockResponseForm(Player requester, Player target, RequestType type) {
        FloodgatePlayer floodgatePlayer = plugin.getFloodgateApi().getPlayer(target.getUniqueId());
        if (floodgatePlayer == null) {
            sendJavaResponseMessage(requester, target, type);
            return;
        }

        String requestText = type == RequestType.TPA
                ? " 请求传送到你这里"
                : " 请求你传送到他那里";
        SimpleForm form = SimpleForm.builder()
                .title("传送请求")
                .content("玩家 §e" + requester.getName() + "§r" + requestText + "\n\n你要接受吗？")
                .button("§a接受")
                .button("§c拒绝")
                .validResultHandler(response -> plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (!target.isOnline() || !requester.isOnline()) {
                        return;
                    }
                    if (!plugin.getRequestManager().hasRequest(target.getUniqueId(), requester.getUniqueId())) {
                        target.sendMessage("§c该传送请求已处理或已过期");
                        return;
                    }
                    if (response.clickedButtonId() == 0) {
                        if (target.performCommand("tpaccept " + requester.getName())) {
                            closeCancelForm(requester.getUniqueId(), target.getUniqueId());
                            plugin.getRequestManager().removeRequest(
                                    target.getUniqueId(), requester.getUniqueId());
                        }
                    } else if (response.clickedButtonId() == 1) {
                        if (target.performCommand("tpdeny " + requester.getName())) {
                            closeCancelForm(requester.getUniqueId(), target.getUniqueId());
                            plugin.getRequestManager().removeRequest(
                                    target.getUniqueId(), requester.getUniqueId());
                        }
                    }
                }))
                .build();
        floodgatePlayer.sendForm(form);
    }

    /** Closes only the still-tracked cancellation form for this exact request. */
    public void closeCancelForm(UUID requesterUUID, UUID targetUUID) {
        if (!openCancelForms.remove(requesterUUID, targetUUID)) {
            return;
        }
        plugin.closeFloodgateForm(requesterUUID);
    }
}
