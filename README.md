# TPA Extension

适用于 Paper 26.2 的 Essentials TPA 界面扩展，为 Java 版与基岩版（PE）玩家提供统一的玩家选择、请求发送、接受、拒绝和取消体验。

当前版本：`2.1.2`

> 已在 Paper 26.2 真实服务器完成 Java/PE 联机实测，包含请求发送、接受、拒绝、取消、请求超时以及 PE 取消表单自动关闭。

## 功能

- Java 玩家使用箱子 GUI 选择在线玩家：
  - 玩家头像与 Java/基岩版本标识
  - 45 格玩家区域、翻页、刷新和可配置底栏
  - 支持按名称筛选玩家
- 基岩版玩家使用 Floodgate 原生表单：
  - 玩家列表与搜索表单
  - 接受/拒绝请求表单
  - 发送请求后的取消表单
  - 请求处理或超时后自动关闭仍打开的取消表单
- Java 玩家收到可点击的接受、拒绝和取消按钮。
- 同时识别 Essentials `/tpa` 与 `/tpahere`，并显示正确的传送方向。
- 请求按“发送者 + 接收者”独立跟踪，避免并发请求互相覆盖。
- Essentials 继续负责权限、冷却、屏蔽、传送延迟和最终传送，本插件不绕过原有安全规则。

## 实测环境

| 项目 | 状态 |
| --- | --- |
| Paper 26.2 | 通过 |
| Java 玩家打开 GUI 并发送请求 | 通过 |
| Java 接受、拒绝、取消按钮 | 通过 |
| PE 玩家列表与搜索表单 | 通过 |
| PE 接受、拒绝、取消表单 | 通过 |
| 对方处理请求后自动关闭 PE 取消表单 | 通过 |
| 配置文件底栏 `[command]` / `[close]` | 通过 |

实测结论由服主在 Paper 26.2 服务器中完成验证。插件使用 Java 21 字节码构建；服务器运行时 Java 版本请遵循 Paper 26.2 的要求。

## 依赖

- [Paper](https://papermc.io/) 26.2
- Essentials（必需，插件名为 `Essentials`）
- Floodgate（可选；需要支持基岩版原生表单时安装）

QuickMenu 不是硬依赖。默认“返回主菜单”按钮执行 `quickmenu open`，可以在配置文件中换成服务器自己的菜单命令。

## 安装

1. 确认服务器已安装并启用 Essentials。
2. 如需 PE/基岩版表单支持，安装 Floodgate。
3. 从 [Releases](https://github.com/yangzijian52/TPA-Extension/releases) 下载插件 JAR。
4. 将 JAR 放入服务器 `plugins` 目录并完整重启服务器。
5. 首次启动后，插件会生成 `plugins/TPAExtension/config.yml`。

升级插件时，已有的 `config.yml` 不会被自动覆盖。若需要新版完整中文注释，请先备份旧配置，再删除或替换旧文件并重启服务器。

## 命令与权限

| 命令 | 别名 | 用途 | 权限 |
| --- | --- | --- | --- |
| `/btpa` | `/bedrocktp`、`/btp`、`/tpgui`、`/tgui` | 打开当前客户端对应的玩家选择界面 | `tpaextension.btpa` |
| `/btpa <名称>` | 同上 | 打开按名称筛选后的玩家选择界面 | `tpaextension.btpa` |

`tpaextension.btpa` 默认向所有玩家开放。玩家仍需拥有 Essentials 对 `/tpa`、`/tpaccept`、`/tpdeny`、`/tpacancel` 等命令要求的权限。

## GUI 底栏配置

默认配置：

```yaml
gui:
  buttons:
    back:
      slot: 48
      material: ARROW
      name: "&a返回主菜单"
      lore:
        - "&7点击返回服务器主菜单"
      commands:
        - "[command] quickmenu open"
    close:
      slot: 50
      material: BARRIER
      name: "&c关闭菜单"
      lore: []
      commands:
        - "[close]"
```

支持的动作：

- `[command] <命令>`：以点击菜单的玩家身份执行命令。
- `[close]`：关闭当前菜单。
- `{player}`：在命令中替换为点击者的玩家名。

完整的颜色代码、槽位范围、材质规则和多动作示例均写在 [`config.yml`](src/main/resources/config.yml) 的中文注释中。

## SpigotMC 发布资料

以下文件用于手动创建和维护 SpigotMC 免费资源页面：

- [`docs/SPIGOTMC-RESOURCE.md`](docs/SPIGOTMC-RESOURCE.md)：资源介绍页英文 BBCode。
- [`docs/SPIGOTMC-RESOURCE-BBCODE.txt`](docs/SPIGOTMC-RESOURCE-BBCODE.txt)：完整使用文档英文 BBCode。
- [`docs/SPIGOTMC_MANUAL_PUBLISHING.md`](docs/SPIGOTMC_MANUAL_PUBLISHING.md)：人工发布字段、警告和操作步骤。
- [`CHANGELOG.md`](CHANGELOG.md)：版本变更记录。
- [`LICENSE`](LICENSE)：MIT License 全文。

本项目建议在 SpigotMC 作为 **免费资源（US$0.00）** 发布。理由是项目采用 MIT License、源码与构建产物公开，并且不包含付费服务、授权验证或独占高级功能。SpigotMC 页面、文档与支持渠道仅使用英文，不在 SpigotMC 提供中文支持。

## 构建

需要 Java 21 或更高版本以及 Maven 3.9+：

```shell
mvn clean package
```

构建产物位于：

```text
target/tpa-extension-2.1.2.jar
```

## 项目结构

```text
src/main/java/com/example/tpaextension/
├── TPAExtension.java       # 插件入口、依赖初始化与 Floodgate 表单关闭兼容
├── BedrockTPACommand.java  # Java/PE 玩家选择命令入口
├── JavaTPAGui.java         # Java 箱子 GUI 与可配置底栏
├── TPAUiService.java       # 请求通知、Java 按钮与 PE 表单
├── TPAListener.java        # 手动 Essentials 命令监听
└── TPARequestManager.java  # 请求状态、并发隔离与超时清理

src/main/resources/
├── config.yml              # 带详细中文注释的服主配置
└── plugin.yml              # Paper 插件描述文件
```

## 版本记录

### 2.1.2

- 为服主配置补充完整中文注释与动作示例。
- 明确 UTF-8、YAML 缩进、GUI 槽位、材质和颜色代码规则。

### 2.1.1

- 对方接受、拒绝或请求超时后，自动关闭 PE 发送者仍打开的取消表单。
- 将取消表单的“关闭”按钮改为红色。

### 2.1.0

- 修复插件 GUI 通过 `Player#performCommand` 发送请求时绕过命令预处理事件，导致 Java 按钮和 PE 表单不出现的问题。
- GUI 请求显式进入统一通知服务，并增加重复通知保护。
- 底栏返回和关闭按钮改为由 `config.yml` 配置。

### 2.0.0～2.0.2

- 升级至 Paper 26.2。
- 增加 Java 玩家 GUI、分页、搜索与统一底栏布局。
- 修复重复通知、并发覆盖、离线残留、手动响应后状态未清理及 `/tpahere` 方向错误。

## 注意事项

- 插件不会隐藏 Essentials 自己发送的提示消息；它会在 Essentials 请求流程之外补充按钮和表单。
- 不建议使用 `/reload`。配置或插件更新后请完整重启服务器。
- 如果 Floodgate 未安装，Java 功能仍可使用，基岩版原生表单功能会禁用。
