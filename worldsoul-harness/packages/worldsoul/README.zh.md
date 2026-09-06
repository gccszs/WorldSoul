---
description: "WorldSoul Minecraft 集成的包映射：与版本无关的 HTTP 网关、Agent 生命周期、命令提案、语音输入和 token 上限。"
kind: "package-group"
---

# worldsoul/：Minecraft 集成

[English](README.md) | 中文

## 概述

`worldsoul/` 组通过与版本无关的 HTTP 协议把 Minecraft 模组连接到 DeepSeek Harness。网关负责存档全局 Agent、相互隔离的玩家 Agent、文字与语音轮次、模型命令提案、私有校验反馈和逐 Agent token 上限。特定 Minecraft 版本的模块继续负责命令解析、授权、执行和游戏侧呈现。

## 目录

- [包](#packages)
- [相关文档](#related-documentation)
- [开发备注](#dev-note)

-----

<a id="packages"></a>
## 包

| 包 | 职责 |
|---|---|
| [`gateway/`](gateway/README.zh.md) | 提供 WorldSoul 协议、管理 Agent 生命周期与配额，并暴露监控仪表盘 |

-----

<a id="related-documentation"></a>
## 相关文档

- [WorldSoul 网关决策](../../.agents/notes/implemented/feature/2026-09-05-worldsoul-minecraft-gateway.zh.md)——协议权限主体、隔离、语音、校验反馈和配额的设计理由。

<a id="dev-note"></a>
## 开发备注

<details>
<summary>维护者工作上下文——点击展开</summary>

无。

</details>
