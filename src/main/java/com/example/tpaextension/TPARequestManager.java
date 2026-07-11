package com.example.tpaextension;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TPARequestManager {

    private final TPAExtension plugin;
    private final Map<RequestKey, TPARequest> pendingRequests;

    public TPARequestManager(TPAExtension plugin) {
        this.plugin = plugin;
        this.pendingRequests = new HashMap<>();
    }

    public TPARequest addRequest(Player requester, Player target, RequestType type) {
        RequestKey key = new RequestKey(requester.getUniqueId(), target.getUniqueId());
        removeRequest(key);

        TPARequest request = new TPARequest(key, type, System.currentTimeMillis());
        pendingRequests.put(key, request);
        BukkitTask expiryTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> expire(request), 20L * 30);
        request.setExpiryTask(expiryTask);
        return request;
    }

    public boolean hasRequest(UUID targetUUID, UUID requesterUUID) {
        return pendingRequests.containsKey(new RequestKey(requesterUUID, targetUUID));
    }

    public Optional<TPARequest> findNewestForTarget(UUID targetUUID) {
        return pendingRequests.values().stream()
                .filter(request -> request.getTargetUUID().equals(targetUUID))
                .max(Comparator.comparingLong(TPARequest::getTimestamp));
    }

    public void removeRequest(UUID targetUUID, UUID requesterUUID) {
        removeRequest(new RequestKey(requesterUUID, targetUUID));
    }

    public void removeAllForTarget(UUID targetUUID) {
        removeMatching(request -> request.getTargetUUID().equals(targetUUID));
    }

    public void removeAllForRequester(UUID requesterUUID) {
        removeMatching(request -> request.getRequesterUUID().equals(requesterUUID));
    }

    public void removeInvolving(UUID playerUUID) {
        removeMatching(request -> request.getRequesterUUID().equals(playerUUID)
                || request.getTargetUUID().equals(playerUUID));
    }

    public void clear() {
        for (TPARequest request : new ArrayList<>(pendingRequests.values())) {
            cancelExpiry(request);
        }
        pendingRequests.clear();
    }

    private void expire(TPARequest request) {
        if (pendingRequests.get(request.key) != request) {
            return;
        }
        pendingRequests.remove(request.key);
        plugin.getUiService().closeCancelForm(
                request.getRequesterUUID(), request.getTargetUUID());
        Player requester = plugin.getServer().getPlayer(request.getRequesterUUID());
        if (requester != null && requester.isOnline()) {
            requester.sendMessage("§c发给该玩家的传送请求已过期");
        }
    }

    private void removeMatching(java.util.function.Predicate<TPARequest> predicate) {
        List<RequestKey> keys = pendingRequests.entrySet().stream()
                .filter(entry -> predicate.test(entry.getValue()))
                .map(Map.Entry::getKey)
                .toList();
        keys.forEach(this::removeRequest);
    }

    private void removeRequest(RequestKey key) {
        TPARequest removed = pendingRequests.remove(key);
        cancelExpiry(removed);
    }

    private void cancelExpiry(TPARequest request) {
        if (request != null && request.expiryTask != null) {
            request.expiryTask.cancel();
        }
    }

    private record RequestKey(UUID requesterUUID, UUID targetUUID) {
    }

    public enum RequestType {
        TPA,
        TPA_HERE
    }

    public static class TPARequest {
        private final RequestKey key;
        private final RequestType type;
        private final long timestamp;
        private BukkitTask expiryTask;

        private TPARequest(RequestKey key, RequestType type, long timestamp) {
            this.key = key;
            this.type = type;
            this.timestamp = timestamp;
        }

        public UUID getRequesterUUID() {
            return key.requesterUUID();
        }

        public UUID getTargetUUID() {
            return key.targetUUID();
        }

        public RequestType getType() {
            return type;
        }

        public long getTimestamp() {
            return timestamp;
        }

        private void setExpiryTask(BukkitTask expiryTask) {
            this.expiryTask = expiryTask;
        }
    }
}
