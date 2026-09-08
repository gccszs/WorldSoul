---
description: "与版本无关的 WorldSoul Minecraft HTTP 网关参考，涵盖 Agent 生命周期、命令校验反馈、语音和 token 配额。"
kind: "package-reference"
---

# `@deepseek-ai/dsh-worldsoul-gateway`

[English](README.md) | 中文

## 概述

此包从 DeepSeek Harness profile 暴露一套与 Minecraft 版本无关的 HTTP 协议。每个存档拥有一个全局 Agent。每个在线玩家在该全局 Agent 下拥有一个相互隔离的子 Agent，并在玩家下线时被 dispose（资源释放）。网关还负责语音转写路由、token 配额和回环监控仪表盘。

## 目录

- [协议与权限主体](#protocol-and-authority)
- [配置](#configuration)
- [模型体验](#model-experience)
- [已知限制与延期工作](#known-limitations-and-deferred-work)
- [开发备注](#dev-note)

-----

<a id="protocol-and-authority"></a>
## 协议与权限主体

Minecraft 保持命令权限主体地位。`minecraft_command` 工具只记录提案；模组校验并执行提案，然后报告接受或拒绝。拒绝详情进入 Agent 的私有下一轮，绝不会包含在面向 Minecraft 的 `speech` 字段中。

网关每次创建或恢复存档 Agent 与玩家 Agent 时都会读取 `agentDefaultModel`，并把该供应商与模型固定到 Agent 选项中。因此，如果共享默认模型服务缺失，profile 会直接激活失败，而不会接受无法到达模型的聊天轮次。供应商认证失败时会返回 HTTP 503 和安全的配置诊断，而不是返回空白的成功响应。

配置的 `pathPrefix` 下提供运维控制台，展示服务指标、存档导航、全局与玩家 Agent 状态、token 用量、阈值操作和停用状态。控制台通过 `GET <pathPrefix>/v1/status` 刷新，不增加对 Minecraft 版本的依赖。

控制台还提供由 `GET` 和 `PUT <pathPrefix>/v1/agent-config` 支持的 Agent 配置面板。面板通过 `agentDefaultModel` 持久化默认 DeepSeek 模型与推理等级，并可通过凭据提供方轮换 `DEEPSEEK_API_KEY`。读取只暴露凭据是否存在、来源与可写性，密钥值绝不会离开凭据提供方。凭据变更对下一次请求生效，模型变更对更新后新建或重新连接的 Agent 生效。

玩家人设是可热更新的选择，而不是创建时常量。`PUT <pathPrefix>/v1/worlds/{world}/players/{player}/mode` 可在不清空会话的情况下切换向导、神明或沙雕模式。上线与聊天请求体接受版本无关的 `playerState`；单独的 `/state` 端点在二进制语音上传前刷新同一快照。维度、坐标、偏航角、俯仰角、朝向、生命、饥饿值、游戏模式、着地状态、当前快捷栏槽位、主背包 36 个槽位和六个装备槽位会渲染到下一轮提示词中，但不会进入公开对话投影。物品信息只包含注册表 ID、数量与耐久；玩家可编辑的自定义名称不会越过提示词边界。

`GET` 和 `PUT <pathPrefix>/v1/speech-config` 管理 OpenAI 兼容的转写接口、模型、超时、凭据名称和可选的凭据轮换。随附 profile 默认使用不需要 API 凭据的本机 FireRedASR-AED-L 服务。设置通过共享设置服务持久化，并对下一次语音请求生效。凭据读取仍不包含值，格式错误的 WAV 会在调用提供方前被拒绝。`POST <player>/voice/transcribe` 只返回转写文本而不启动 Agent 轮次，使 Mod 能先回显文本、显示思考提示，再把转写文本提交至 `<player>/chat`；组合式 `<player>/voice` 接口继续供旧客户端使用。

每个玩家行都提供实时对话室入口。对话室轮询 `GET <pathPrefix>/v1/worlds/{world}/players/{player}/conversation?after={revision}`，并追加返回游标之后的纯文本消息。投影读取玩家的持久化会话，只暴露玩家直接消息和 Agent 回复，并排除插件反馈、工具流量与命令校验详情。

<a id="configuration"></a>
## 配置

`pathPrefix` 选择路由前缀。`maxBodyBytes` 和 `maxAudioBytes` 限制请求大小。`defaultTokenLimit` 是每个 Agent 的初始硬上限。`warningRatio` 选择接近上限的状态。`maxCommandChars` 限制 Minecraft 校验前单个命令提案的长度。`speechEndpoint`、`speechModel`、`speechCredentialEnv` 和 `speechTimeoutMs` 配置 OpenAI 兼容的转写提供方。默认接口是 `http://127.0.0.1:9001/v1/audio/transcriptions`，默认模型字段是 `FireRedASR-AED-L`，默认凭据名称为空。

<a id="model-experience"></a>
## 模型体验

### 玩家人设与命令工具

#### 模型所见

玩家选择的纯文本模式人设和 `minecraft_command` 工具。该工具只能创建等待游戏侧校验的提案。

#### Token 影响

完整人设会替换玩家 Agent 的其他提示词部分。工具调用和私有校验反馈只保留在该玩家的会话中。

#### KV Cache 影响

每个玩家的提示词前缀在其在线 Agent 的生命周期内保持稳定。更改模式目前需要重新连接该玩家 Agent。

## 已知限制与延期工作

<a id="known-limitations-and-deferred-work"></a>

- **模式变更会重建上下文** — 上线端点会保留已有 Agent，因此不同模式会在该玩家下线并重新上线后生效。
- **首次语音安装会下载大型文件** — 本地 FireRedASR 部署使用隔离的 Python 环境、CUDA PyTorch 和约 4.7 GB 的模型权重。模型缺失或停止时文字聊天仍可使用。

不发布运行时 invariant companion，因为运行时在同一个 map 中拥有每个存档与玩家条目，并同步应用生命周期和配额状态转换；其行为测试覆盖这些关系。

<a id="dev-note"></a>
### 开发备注

<details>
<summary>维护者工作上下文——点击展开</summary>

无。

</details>
