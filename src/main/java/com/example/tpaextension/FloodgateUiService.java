package com.example.tpaextension;

import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.tpaextension.TPARequestManager.RequestType;

/** Renders request/response UI while Essentials remains the teleport authority. */
public class FloodgateUiService extends TPAUiService {

    private final org.geysermc.floodgate.api.FloodgateApi api;

    @Override
    public boolean isBedrockPlayer(UUID uuid) {
        return api.isFloodgatePlayer(uuid);
    }
    private final TPAExtension plugin;
    private final Map<UUID, UUID> openCancelForms = new ConcurrentHashMap<>();

    public FloodgateUiService(TPAExtension plugin) {
        super(plugin);
        this.plugin = plugin;
        api = java.util.Objects.requireNonNull(org.geysermc.floodgate.api.FloodgateApi.getInstance(),
                "Floodgate API is not initialized");
        SimpleForm.builder();
    }

    @Override
    protected void sendCancelUi(Player requester, Player target) {
        if (plugin.isBedrockPlayer(requester.getUniqueId())) {
            FloodgatePlayer floodgatePlayer = org.geysermc.floodgate.api.FloodgateApi.getInstance().getPlayer(requester.getUniqueId());
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

        super.sendCancelUi(requester, target);
    }

    @Override
    protected void sendResponseUi(Player requester, Player target, RequestType type) {
        if (plugin.isBedrockPlayer(target.getUniqueId())) {
            sendBedrockResponseForm(requester, target, type);
        } else {
            sendJavaResponseMessage(requester, target, type);
        }
    }

    private void sendBedrockResponseForm(Player requester, Player target, RequestType type) {
        FloodgatePlayer floodgatePlayer = org.geysermc.floodgate.api.FloodgateApi.getInstance().getPlayer(target.getUniqueId());
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
    @Override
    public void closeCancelForm(UUID requesterUUID, UUID targetUUID) {
        if (!openCancelForms.remove(requesterUUID, targetUUID)) {
            return;
        }
        try {
            org.geysermc.floodgate.api.FloodgateApi api = org.geysermc.floodgate.api.FloodgateApi.getInstance();
            api.getClass().getMethod("closeForm", UUID.class).invoke(api, requesterUUID);
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().warning("关闭基岩版表单失败: " + exception.getMessage());
        }
    }
}
