# MinecraftOSC

MinecraftOSC 是一个面向 Minecraft 1.20.1 Forge 服务器的模组，用于接收 VRChat 或其他 OSC 客户端发来的 UDP 消息，并把聊天内容安全注入到游戏聊天栏。

## 功能

- 监听可配置 UDP 端口接收 OSC 1.0 消息
- 支持 `/vrchat/chat` 与 `/chatbox/input` 两类默认地址
- 解析字符串、整数、浮点、布尔、Blob、Long、Double 等基础 OSC 参数
- 对来源 IP 做白名单校验与速率限制
- 对聊天内容做清洗、长度限制和命令前缀转义
- 通过线程安全队列在服务器主线程分发消息
- 聊天注入失败时按 tick 延迟重试

## 配置

Forge 会生成 `minecraftosc-common.toml`，核心配置项包括：

- `listenPort`: OSC 监听端口，默认 `9000`
- `acceptedOscAddresses`: 允许注入聊天的 OSC 地址
- `allowAnySender`: 是否跳过来源白名单
- `allowedSenders`: 允许的来源 IP，默认仅本机
- `chatPrefix`: Minecraft 中显示的前缀，默认 `[VRChat] `
- `maxRetries`: 注入失败后的最大重试次数

## 本地开发

项目基于 ForgeGradle 6 和 JDK 17：

```powershell
$env:JAVA_HOME='C:\path\to\jdk-17'
.\gradlew.bat test
.\gradlew.bat runServer
```
