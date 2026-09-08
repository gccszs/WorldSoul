---
description: "供 Minecraft 文字、语音、命令提案和 Agent 配额监控使用的独立回环 WorldSoul profile 组合包。"
kind: "package-bundle"
---

# `@deepseek-ai/dsh-worldsoul-app`

[English](README.md) | 中文

## 概述

此组合包向 `dsh-base` 添加回环 Web 服务器和 WorldSoul 网关。随附的 `worldsoul` profile 使用 `dsh --profile worldsoul` 启动；`WORLDSOUL_PORT` 选择监听端口，默认值为 `8787`。它是受支持 Minecraft 模组使用的可独立部署本地服务。

## 目录

- [使用本包](#use-this-package)
- [理解实现](#understand-the-implementation)
- [模型体验](#model-experience)
- [已知限制与延期工作](#known-limitations-and-deferred-work)
- [开发备注](#dev-note)

-----

<a id="use-this-package"></a>
## 使用本包

配置 DeepSeek 凭据后运行 `dsh --profile worldsoul`。端口 8787 不可用时设置 `WORLDSOUL_PORT`，并把模组的 Harness URL 指向匹配的 `/worldsoul` 前缀。语音设置是可选项，与文字聊天相互独立。

-----

<a id="understand-the-implementation"></a>
## 理解实现

<details>
<summary>实现细节——点击展开</summary>

此组合包在共享 base 配置树之上插入 Host Web 服务器和 `@deepseek-ai/dsh-worldsoul-gateway`。网关包拥有运行时行为；本包只选择和配置插件项。

### 源码地图

| 文件 | 职责 |
|---|---|
| [`cordis.patch.yml`](cordis.patch.yml) | 回环服务器和使用环境配置的 WorldSoul 网关配置 |
| [`src/index.ts`](src/index.ts) | 组合包入口 |
| — | 不发布运行时 invariant companion，因为此组合包是静态 patch 列表载体，插入的插件各自拥有其运行时关系与检查。 |

</details>

-----

<a id="model-experience"></a>
## 模型体验

### 挂载的 WorldSoul 网关

#### 模型所见

此组合包挂载 `@deepseek-ai/dsh-worldsoul-gateway`，由后者提供模型可见的玩家人设和命令工具。

#### Token 影响

此组合包本身不增加 token；挂载的网关负责提示词、工具和校验反馈的 token。

#### KV Cache 影响

此组合包本身不添加请求前缀；挂载的网关负责每个玩家的稳定提示词前缀。

## 已知限制与延期工作

<a id="known-limitations-and-deferred-work"></a>

- **仅限回环地址** — 远程 Minecraft 服务器需要使用带认证传输的显式 profile 补丁，之后才能绑定全部接口。

<a id="dev-note"></a>
### 开发备注

<details>
<summary>维护者工作上下文——点击展开</summary>

无。

</details>
