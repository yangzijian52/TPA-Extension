package com.example.tpaextension;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Opens the native Floodgate form for Bedrock players and an inventory GUI for Java players. */
public class BedrockTPACommand implements CommandExecutor, TabCompleter {

    private final TPAExtension plugin;

    public BedrockTPACommand(TPAExtension plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c只有玩家可以使用此命令！");
            return true;
        }

        String filter = args.length == 0 ? "" : String.join(" ", args).trim();
        if (plugin.isBedrockPlayer(player.getUniqueId())) {
            openBedrockGui(player, filter);
        } else {
            plugin.getJavaGui().open(player, filter, 0);
        }
        return true;
    }

    private void openBedrockGui(Player sender, String filter) {
        if (!plugin.hasFloodgateSupport()) {
            sender.sendMessage("§cFloodgate 当前不可用，请使用 /tpa <玩家名>");
            return;
        }

        FloodgatePlayer floodgatePlayer = plugin.getFloodgateApi().getPlayer(sender.getUniqueId());
        if (floodgatePlayer == null) {
            sender.sendMessage("§c无法获取基岩版玩家信息，请稍后重试");
            return;
        }

        List<Player> players = matchingPlayers(sender, filter);
        if (players.isEmpty()) {
            sender.sendMessage(filter.isBlank() ? "§c当前没有其他在线玩家！" : "§c未找到匹配的玩家：" + filter);
            return;
        }

        SimpleForm.Builder builder = SimpleForm.builder()
                .title("选择传送目标")
                .content("§7选择你要传送到的玩家\n§7匹配玩家: §e" + players.size() + " §7人");

        for (Player target : players) {
            String edition = plugin.isBedrockPlayer(target.getUniqueId()) ? "§b[基岩] §r" : "§a[Java] §r";
            builder.button(edition + target.getName());
        }
        builder.button("§e搜索玩家");

        floodgatePlayer.sendForm(builder.validResultHandler(response ->
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    int button = response.clickedButtonId();
                    if (button == players.size()) {
                        openSearchForm(floodgatePlayer, sender);
                        return;
                    }
                    if (button < 0 || button >= players.size()) {
                        return;
                    }
                    Player target = plugin.getServer().getPlayer(players.get(button).getUniqueId());
                    if (target == null || !target.isOnline()) {
                        sender.sendMessage("§c该玩家已离线，请重新选择");
                        openBedrockGui(sender, filter);
                        return;
                    }
                    plugin.getUiService().sendRequest(
                            sender, target, TPARequestManager.RequestType.TPA);
                })).build());
    }

    private void openSearchForm(FloodgatePlayer floodgatePlayer, Player sender) {
        CustomForm form = CustomForm.builder()
                .title("搜索玩家")
                .input("输入玩家名称", "玩家名...")
                .validResultHandler(response -> plugin.getServer().getScheduler().runTask(plugin, () -> {
                    String query = response.asInput(0).trim();
                    if (query.isEmpty()) {
                        openBedrockGui(sender, "");
                    } else {
                        openBedrockGui(sender, query);
                    }
                }))
                .build();
        floodgatePlayer.sendForm(form);
    }

    private List<Player> matchingPlayers(Player sender, String filter) {
        String normalized = filter.toLowerCase(Locale.ROOT);
        List<Player> players = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        players.removeIf(player -> player.getUniqueId().equals(sender.getUniqueId())
                || (!normalized.isBlank()
                && !player.getName().toLowerCase(Locale.ROOT).contains(normalized)));
        players.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));
        return players;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player player) || args.length != 1) {
            return new ArrayList<>();
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        return plugin.getServer().getOnlinePlayers().stream()
                .filter(target -> !target.getUniqueId().equals(player.getUniqueId()))
                .map(Player::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }
}
