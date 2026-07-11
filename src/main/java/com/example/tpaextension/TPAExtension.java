package com.example.tpaextension;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;

import java.lang.reflect.Method;
import java.util.UUID;

public class TPAExtension extends JavaPlugin {

    private FloodgateApi floodgateApi;
    private TPARequestManager requestManager;
    private TPAUiService uiService;
    private JavaTPAGui javaGui;
    private Method floodgateCloseFormMethod;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // 检查依赖
        if (!setupEssentials()) {
            getLogger().severe("未找到 Essentials 插件！禁用插件...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        boolean hasFloodgate = setupFloodgate();
        if (!hasFloodgate) {
            getLogger().warning("未找到 Floodgate 插件！基岩版功能将被禁用");
            getLogger().warning("如需支持基岩版玩家，请安装 Floodgate");
        }

        requestManager = new TPARequestManager(this);
        uiService = new TPAUiService(this);
        javaGui = new JavaTPAGui(this);

        getServer().getPluginManager().registerEvents(new TPAListener(this), this);
        getServer().getPluginManager().registerEvents(javaGui, this);

        PluginCommand command = getCommand("btpa");
        if (command == null) {
            getLogger().severe("plugin.yml 中缺少 btpa 命令，插件无法启动");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        BedrockTPACommand commandHandler = new BedrockTPACommand(this);
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

    private boolean setupFloodgate() {
        if (getServer().getPluginManager().getPlugin("floodgate") != null) {
            floodgateApi = FloodgateApi.getInstance();
            try {
                // Floodgate 2.2.3 implements closeForm(UUID) on SimpleFloodgateApi although the
                // method is missing from that version's public FloodgateApi interface.
                floodgateCloseFormMethod = floodgateApi.getClass().getMethod("closeForm", UUID.class);
            } catch (NoSuchMethodException exception) {
                getLogger().warning("当前 Floodgate 版本不支持服务端主动关闭表单");
            }
            return true;
        }
        return false;
    }

    public FloodgateApi getFloodgateApi() {
        return floodgateApi;
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

    public boolean isBedrockPlayer(java.util.UUID uuid) {
        return floodgateApi != null && floodgateApi.isFloodgatePlayer(uuid);
    }

    public boolean hasFloodgateSupport() {
        return floodgateApi != null;
    }

    public boolean closeFloodgateForm(UUID playerUUID) {
        if (floodgateApi == null || floodgateCloseFormMethod == null) {
            return false;
        }
        try {
            Object result = floodgateCloseFormMethod.invoke(floodgateApi, playerUUID);
            return !(result instanceof Boolean) || (Boolean) result;
        } catch (ReflectiveOperationException exception) {
            getLogger().warning("关闭基岩版表单失败: " + exception.getMessage());
            return false;
        }
    }
}
