# WorldSoul Harness

WorldSoul Harness 基于仓库内的 DeepSeek Harness 源码运行，与 Minecraft 游戏版本解耦。Fabric 模组只依赖这里定义的 HTTP JSON 协议，因此 1.20.1、1.20.4 或后续适配版本可连接同一个服务。

## 本地部署

需要 Node.js 24、pnpm 和 Java 17。首次启动：

```powershell
.\scripts\start-worldsoul-harness.ps1 -Install -Build
```

后续启动：

```powershell
.\scripts\start-worldsoul-harness.ps1
```

服务只监听 `127.0.0.1`，默认入口与监控页是 `http://127.0.0.1:8787/worldsoul`。可用 `-Port` 改端口，模组侧通过 `config/worldsoul-harness.properties` 的 `url` 指向相同地址。

游戏对话还需要有效的模型凭据。默认 DeepSeek 模型从 `%USERPROFILE%\.dsh\.credentials.yaml` 的 `refs.DEEPSEEK_API_KEY` 读取密钥；修改并保存该文件后 Harness 会自动重新加载，无需重启。若密钥缺失或失效，对话接口会返回安全的服务错误，游戏内只显示通用失败提示，具体认证信息不会进入聊天栏。

语音默认使用本地 FireRedASR-AED-L。首次执行 `./scripts/setup-fireredasr.ps1` 会在 `worldsoul-harness/.runtime/fireredasr` 创建隔离的 Python 3.10 环境、安装 CUDA PyTorch、检出固定版本的官方源码并下载模型权重。普通的 Harness 启动脚本会自动检查并拉起 `http://127.0.0.1:9001` 上的语音服务；用 `-SkipSpeechService` 可跳过，用 `-SpeechPort` 可改端口。Harness 与语音服务之间仍使用 OpenAI 兼容接口，因此也可在页面热更新为其他提供方。游戏内按住 V 键录音，松开后上传；录音至少持续 0.25 秒。

## Agent 生命周期

每个存档 ID 对应一个全局 Agent。每个在线玩家对应一个以全局 Agent 为父级的隔离子 Agent。玩家离线时关闭个人 Agent，服务器停止时先关闭所有个人 Agent，再关闭存档 Agent。稳定 Session ID 支持 Harness 重启后恢复持久会话，玩家之间不共享提示词、消息、指令提案或 token 用量。

## HTTP 协议

所有路径均位于 `/worldsoul/v1`：

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| GET | `/health` | 健康检查与协议版本 |
| GET | `/status` | 存档及全部 Agent 的 token 状态 |
| GET/PUT | `/agent-config` | 读取或保存默认模型、推理等级与 DeepSeek 凭据 |
| GET/PUT | `/speech-config` | 读取或保存语音转写接口、模型与凭据 |
| PUT/DELETE | `/worlds/{world}/players/{player}/online` | 启用或停用玩家 Agent |
| PUT | `/worlds/{world}/players/{player}/mode` | 热切换向导、神明或沙雕模式 |
| PUT | `/worlds/{world}/players/{player}/state` | 更新玩家坐标、朝向、生存和游戏模式状态 |
| DELETE | `/worlds/{world}` | 停用整个存档 |
| POST | `/worlds/{world}/players/{player}/chat` | 文字对话 |
| GET | `/worlds/{world}/players/{player}/conversation?after={revision}` | 增量读取玩家与 Agent 的公开纯文本对话 |
| POST | `/worlds/{world}/players/{player}/voice/transcribe` | 上传音频并立即返回转写文本，不启动 Agent 轮次 |
| POST | `/worlds/{world}/players/{player}/voice` | 上传 WAV/WebM/Ogg 音频并返回转写与对话结果 |
| PUT | `/worlds/{world}/limit` | 设置全局 Agent token 阈值 |
| PUT | `/worlds/{world}/players/{player}/limit` | 设置个人 Agent token 阈值 |
| POST | `/worlds/{world}/players/{player}/commands/{proposal}/result` | 私下回传模组校验结果 |

响应中的 `speech` 是唯一允许显示在游戏聊天栏中的 Agent 文本。`commands` 只是待验证提案，不能由 Harness 直接执行。模组依次执行模式策略白名单、单行/长度检查、Brigadier 完整解析，再以权限等级 2 的静默玩家命令源执行。失败详情只写服务端日志并回传 Harness，Agent 最多重新生成两次；校验详情不会显示给玩家。

监控页每三秒读取状态，以正常、预警、耗尽三种颜色显示用量，并可直接设置存档或玩家阈值。玩家 Agent 行可进入对话室；对话室每秒按游标拉取新消息，只显示玩家直接输入和 Agent 纯文本回复，不显示命令校验错误、插件反馈或工具流量。到达预警比例时 Harness 页面和游戏提示告警；达到阈值后对应 Agent 拒绝新对话，调高阈值后可恢复。

页面顶部的 Agent 配置面板可选择本地部署已加载的 DeepSeek 模型、推理等级并更新 API Key。密钥输入使用密码字段，服务端读取配置时只返回是否已配置、来源和可写状态，不回传密钥内容。API Key 保存后下一次请求立即使用；模型或推理等级变更后，玩家重新进入存档即可让新 Agent 使用新配置。

玩家可使用 `/worldsoul mode guide`、`/worldsoul mode deity` 或 `/worldsoul mode troll` 热切换个人 Agent 模式，Harness 的玩家列表也提供相同操作。切换不会清空对话。Mod 在上线、文字对话和语音对话前同步维度、坐标、偏航角、俯仰角、水平朝向、生命、饥饿值、游戏模式、是否着地、当前快捷栏槽位、主背包 36 个槽位及六个装备槽位，Harness 将最新状态动态渲染到下一轮系统提示词中。物品只包含注册表 ID、数量和耐久数据，不同步玩家可编辑的自定义名称。语音请求分两阶段执行：转写完成后游戏立即显示“你说：…”和“思考中…”，随后才提交 Agent 对话并显示最终回复。

## 验证

```powershell
.\gradlew.bat test

cd .\worldsoul-harness
pnpm exec vitest run packages/worldsoul/gateway/tests
pnpm run build:lib

cd ..
worldsoul-harness\.runtime\fireredasr\env\python.exe -m pytest worldsoul-harness\services\fireredasr\test_app.py
```

主验证版本是 Minecraft 1.20.1；仓库级 Gradle 测试也会编译并测试 1.20.4。真实启动后可访问 `/health`，再调用玩家上线、状态、阈值、离线和存档关闭接口完成不依赖 LLM 凭据的部署冒烟测试。
