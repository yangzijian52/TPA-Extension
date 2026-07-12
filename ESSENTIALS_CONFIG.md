# 如何禁用 Essentials 的 TPA 消息

如果你想完全去掉 Essentials 的原始 TPA 提示消息，只显示本插件的美化消息，可以按照以下步骤操作：

## 方法 1：修改 Essentials 语言文件（推荐）

1. 找到 Essentials 插件目录：`plugins/Essentials/`
2. 打开或创建语言文件，例如 `messages_zh_CN.properties`（中文）或 `messages.properties`（英文）
3. 添加或修改以下配置，将 TPA 消息设置为空：

```properties
# 传送请求相关消息设置为空
requestSent=
requestSentAlready=
typeTpaccept=
typeTpdeny=
```

4. 保存文件并重启服务器或重载 Essentials

## ProtocolLib 说明

本插件没有实现 ProtocolLib 数据包拦截，也不依赖 ProtocolLib。仅安装 ProtocolLib 不会自动隐藏 Essentials 消息，请不要为此功能额外安装它。

如果修改 Essentials 语言文件后仍有原始提示，请检查实际使用的 Essentials 语言、消息键以及是否有其他聊天插件改写消息。不要通过取消 Essentials 命令或绕过其权限检查来隐藏提示。

## 当前实现

目前插件会在 Essentials 请求流程之外补充自定义界面：
- Java 版玩家：显示带有可点击按钮的美化消息框
- 基岩版玩家：弹出表单界面

这样即使看到 Essentials 的原始消息，用户也会优先注意到我们的美化消息。
