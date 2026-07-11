package com.example.tpaextension;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Chest-style player selector used by Java Edition clients. */
public class JavaTPAGui implements Listener {

    private static final int INVENTORY_SIZE = 54;
    private static final int PLAYERS_PER_PAGE = 45;
    private final TPAExtension plugin;

    public JavaTPAGui(TPAExtension plugin) {
        this.plugin = plugin;
    }

    public void open(Player viewer, String filter, int requestedPage) {
        String safeFilter = filter == null ? "" : filter.trim();
        List<Player> players = matchingPlayers(viewer, safeFilter);
        int pageCount = Math.max(1, (players.size() + PLAYERS_PER_PAGE - 1) / PLAYERS_PER_PAGE);
        int page = Math.max(0, Math.min(requestedPage, pageCount - 1));

        MenuHolder holder = new MenuHolder(viewer.getUniqueId(), safeFilter, page, pageCount);
        Inventory inventory = holder.getInventory();
        int start = page * PLAYERS_PER_PAGE;
        int end = Math.min(players.size(), start + PLAYERS_PER_PAGE);

        for (int index = start; index < end; index++) {
            Player target = players.get(index);
            int slot = index - start;
            inventory.setItem(slot, playerHead(target));
            holder.targetsBySlot.put(slot, target.getUniqueId());
        }

        if (players.isEmpty()) {
            inventory.setItem(22, item(Material.BARRIER, "没有匹配的在线玩家", NamedTextColor.RED,
                    List.of(safeFilter.isBlank()
                            ? "等待其他玩家上线后点击刷新"
                            : "当前搜索：" + safeFilter)));
        }
        if (page > 0) {
            inventory.setItem(45, item(Material.ARROW, "上一页", NamedTextColor.GREEN,
                    List.of("返回第 " + page + " 页")));
        }
        ConfiguredButton backButton = configuredButton("gui.buttons.back", 48, Material.ARROW,
                "&a返回主菜单", List.of("&7点击返回服务器主菜单"),
                List.of("[command] quickmenu open"));
        setConfiguredButton(inventory, holder, backButton);
        List<String> pageLore = new java.util.ArrayList<>();
        pageLore.add("共 " + players.size() + " 名匹配玩家");
        if (!safeFilter.isBlank()) {
            pageLore.add("当前筛选: " + safeFilter);
        }
        pageLore.add("点击刷新在线列表");
        inventory.setItem(49, item(Material.PAPER,
                "第 " + (page + 1) + "/" + pageCount + " 页", NamedTextColor.WHITE, pageLore));
        ConfiguredButton closeButton = configuredButton("gui.buttons.close", 50, Material.BARRIER,
                "&c关闭菜单", List.of(), List.of("[close]"));
        setConfiguredButton(inventory, holder, closeButton);
        if (page + 1 < pageCount) {
            inventory.setItem(53, item(Material.ARROW, "下一页", NamedTextColor.GREEN,
                    List.of("前往第 " + (page + 2) + " 页")));
        }
        fillEmpty(inventory);
        viewer.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof MenuHolder holder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)
                || !player.getUniqueId().equals(holder.viewerUUID)
                || event.getClickedInventory() != top) {
            return;
        }

        int slot = event.getRawSlot();
        List<String> configuredActions = holder.actionsBySlot.get(slot);
        if (configuredActions != null) {
            executeActions(player, configuredActions);
            return;
        }
        if (slot == 45 && holder.page > 0) {
            reopenNextTick(player, holder.filter, holder.page - 1);
            return;
        }
        if (slot == 49) {
            reopenNextTick(player, holder.filter, holder.page);
            return;
        }
        if (slot == 53) {
            reopenNextTick(player, holder.filter, holder.page + 1);
            return;
        }

        UUID targetUUID = holder.targetsBySlot.get(slot);
        if (targetUUID == null) {
            return;
        }
        Player target = plugin.getServer().getPlayer(targetUUID);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§c该玩家已离线，列表已刷新");
            reopenNextTick(player, holder.filter, holder.page);
            return;
        }

        player.closeInventory();
        // Essentials remains authoritative for permissions, cooldowns and the teleport itself;
        // the UI service also handles the notification path that performCommand may bypass.
        plugin.getUiService().sendRequest(player, target, TPARequestManager.RequestType.TPA);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof MenuHolder) {
            event.setCancelled(true);
        }
    }

    private void reopenNextTick(Player player, String filter, int page) {
        plugin.getServer().getScheduler().runTask(plugin, () -> open(player, filter, page));
    }

    private List<Player> matchingPlayers(Player viewer, String filter) {
        String normalized = filter.toLowerCase(Locale.ROOT);
        List<Player> players = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        players.removeIf(player -> player.getUniqueId().equals(viewer.getUniqueId())
                || (!normalized.isBlank()
                && !player.getName().toLowerCase(Locale.ROOT).contains(normalized)));
        players.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));
        return players;
    }

    private ItemStack playerHead(Player target) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(target);
        meta.displayName(text(target.getName(), NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        String edition = plugin.isBedrockPlayer(target.getUniqueId()) ? "基岩版玩家" : "Java 版玩家";
        meta.lore(List.of(
                text("玩家类型: ", NamedTextColor.GRAY)
                        .append(text(edition, plugin.isBedrockPlayer(target.getUniqueId())
                                ? NamedTextColor.AQUA : NamedTextColor.GREEN)),
                text("当前状态: ", NamedTextColor.GRAY).append(text("在线", NamedTextColor.GREEN)),
                Component.empty(),
                text("点击发送传送请求", NamedTextColor.YELLOW)
        ));
        meta.addItemFlags(ItemFlag.values());
        head.setItemMeta(meta);
        return head;
    }

    private ItemStack item(Material material, String name, NamedTextColor color, List<String> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(text(name, color).decorate(TextDecoration.BOLD));
        if (lore != null && !lore.isEmpty()) {
            meta.lore(lore.stream().map(line -> text(line, NamedTextColor.GRAY)).toList());
        }
        meta.addItemFlags(ItemFlag.values());
        stack.setItemMeta(meta);
        return stack;
    }

    private ConfiguredButton configuredButton(String path, int defaultSlot, Material defaultMaterial,
                                              String defaultName, List<String> defaultLore,
                                              List<String> defaultActions) {
        int slot = plugin.getConfig().getInt(path + ".slot", defaultSlot);
        String materialName = plugin.getConfig().getString(path + ".material", defaultMaterial.name());
        Material material = Material.matchMaterial(materialName == null ? "" : materialName);
        if (material == null || !material.isItem()) {
            plugin.getLogger().warning("GUI 按钮材质无效: " + path + ".material=" + materialName
                    + "，已使用 " + defaultMaterial.name());
            material = defaultMaterial;
        }

        String name = plugin.getConfig().getString(path + ".name", defaultName);
        List<String> lore = plugin.getConfig().getStringList(path + ".lore");
        if (!plugin.getConfig().contains(path + ".lore")) {
            lore = defaultLore;
        }
        List<String> actions = plugin.getConfig().getStringList(path + ".commands");
        if (!plugin.getConfig().contains(path + ".commands")) {
            actions = defaultActions;
        }

        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(legacy(name == null ? defaultName : name));
        if (!lore.isEmpty()) {
            meta.lore(lore.stream().map(this::legacy).toList());
        }
        meta.addItemFlags(ItemFlag.values());
        stack.setItemMeta(meta);
        return new ConfiguredButton(slot, stack, List.copyOf(actions));
    }

    private void setConfiguredButton(Inventory inventory, MenuHolder holder, ConfiguredButton button) {
        if (button.slot < 0 || button.slot >= inventory.getSize()) {
            plugin.getLogger().warning("GUI 按钮槽位越界，已跳过: " + button.slot);
            return;
        }
        inventory.setItem(button.slot, button.item);
        holder.actionsBySlot.put(button.slot, button.actions);
    }

    private void executeActions(Player player, List<String> actions) {
        for (String rawAction : actions) {
            if (rawAction == null) {
                continue;
            }
            String action = rawAction.trim();
            String lower = action.toLowerCase(Locale.ROOT);
            if (lower.equals("[close]")) {
                player.closeInventory();
                continue;
            }
            if (lower.startsWith("[command]")) {
                String command = action.substring("[command]".length()).trim()
                        .replace("{player}", player.getName());
                if (!command.isEmpty()) {
                    if (command.startsWith("/")) {
                        command = command.substring(1);
                    }
                    player.performCommand(command);
                }
            }
        }
    }

    private Component legacy(String value) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(value)
                .decoration(TextDecoration.ITALIC, false);
    }

    private void fillEmpty(Inventory inventory) {
        ItemStack fill = item(Material.GRAY_STAINED_GLASS_PANE, " ", NamedTextColor.DARK_GRAY, List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, fill);
            }
        }
    }

    private Component text(String value, NamedTextColor color) {
        return Component.text(value, color).decoration(TextDecoration.ITALIC, false);
    }

    private static final class MenuHolder implements InventoryHolder {
        private final UUID viewerUUID;
        private final String filter;
        private final int page;
        private final Map<Integer, UUID> targetsBySlot = new HashMap<>();
        private final Map<Integer, List<String>> actionsBySlot = new HashMap<>();
        private final Inventory inventory;

        private MenuHolder(UUID viewerUUID, String filter, int page, int pageCount) {
            this.viewerUUID = viewerUUID;
            this.filter = filter;
            this.page = page;
            String context = filter.isBlank() ? "传送请求" : "搜索: " + filter;
            this.inventory = Bukkit.createInventory(this, INVENTORY_SIZE,
                    Component.text(context, NamedTextColor.BLACK)
                            .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                            .append(Component.text("第 " + (page + 1) + "/" + pageCount + " 页",
                                    NamedTextColor.GRAY)));
        }

        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }
    }

    private record ConfiguredButton(int slot, ItemStack item, List<String> actions) {
    }
}
