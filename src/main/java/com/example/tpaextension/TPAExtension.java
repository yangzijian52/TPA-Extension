package com.example.tpaextension;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class TPAExtension extends JavaPlugin {

    private TPARequestManager requestManager;
    private TPAUiService uiService;
    private JavaTPAGui javaGui;
    private boolean floodgateSupport;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // 检查依赖
        if (!setupEssentials()) {
            getLogger().severe("未找到 Essentials 插件！禁用插件...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        uiService = new TPAUiService(this);
        BedrockTPACommand commandHandler = new BedrockTPACommand(this);
        boolean hasFloodgate = false;
        var floodgate = getServer().getPluginManager().getPlugin("floodgate");
        if (floodgate != null && floodgate.isEnabled()) {
            try {
                uiService = new FloodgateUiService(this);
                commandHandler = new FloodgateTPACommand(this);
                hasFloodgate = true;
            } catch (LinkageError | RuntimeException exception) {
                uiService = new TPAUiService(this);
                commandHandler = new BedrockTPACommand(this);
                getLogger().warning("Floodgate 初始化失败，使用 Java 界面: " + exception);
            }
        }
        floodgateSupport = hasFloodgate;
        if (!hasFloodgate) {
            getLogger().info("Floodgate 不可用，使用 Java 箱子 GUI 和聊天按钮；原生基岩版表单未启用");
        }

        requestManager = new TPARequestManager(this);
        javaGui = new JavaTPAGui(this);

        getServer().getPluginManager().registerEvents(new TPAListener(this), this);
        getServer().getPluginManager().registerEvents(javaGui, this);

        PluginCommand command = getCommand("btpa");
        if (command == null) {
            getLogger().severe("plugin.yml 中缺少 btpa 命令，插件无法启动");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        command.setExecutor(commandHandler);
        command.setTabCompleter(commandHandler);

        if (hasFloodgate) {
            getLogger().info("TPA Extension 已启用！（支持基岩版）");
        } else {
            getLogger().info("TPA Extension 已启用！（仅 Java 版）");
        }
    }

    @Override
    public void onDisable() {
        if (requestManager != null) {
            requestManager.clear();
        }
        getLogger().info("TPA Extension 已禁用！");
    }

    private boolean setupEssentials() {
        var plugin = getServer().getPluginManager().getPlugin("Essentials");
        return plugin != null && plugin.isEnabled();
    }

    public TPARequestManager getRequestManager() {
        return requestManager;
    }

    public TPAUiService getUiService() {
        return uiService;
    }

    public JavaTPAGui getJavaGui() {
        return javaGui;
    }

    public boolean isBedrockPlayer(UUID uuid) {
        return uiService != null && uiService.isBedrockPlayer(uuid);
    }
    public boolean hasFloodgateSupport() {
        return floodgateSupport;
    }
}
