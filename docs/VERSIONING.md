# Minecraft 版本管理范式

WorldSoul 的 Fabric mod **按 Minecraft 小版本分工程构建**，共享无 MC 依赖代码。

## 目录结构

```
WorldSoul/
├── agent-core/          # 无 MC：AI / 会话 / 协议
├── worldsoul-harness/   # 无 MC：DeepSeek Harness Agent 服务
├── mod-common/          # 无 MC：HarnessClient / HarnessSettings
├── mod-1.20.1/          # Fabric 适配层（仅 1.20.1）
├── mod-1.20.4/          # Fabric 适配层（仅 1.20.4）
├── versions/
│   ├── 1.20.1.properties
│   └── 1.20.4.properties
└── gradle/fabric-mod.gradle   # 各版本 mod 的公共 Loom 约定
```

## 原则

1. **一个 MC 版本 = 一个 Gradle 子工程 + 一个 `versions/*.properties`**
2. **禁止**在单个 jar 里用宽泛依赖冒充多版本（例如 `>=1.20.1`）；Loader 声明必须是精确谓词（如 `~1.20.1`）
3. **能共用且属于模组侧的不碰 MC API**：放 `mod-common` / `agent-core`；Agent 服务放 `worldsoul-harness`
4. **碰 MC / Fabric API 的**：复制或分别维护在 `mod-<version>/`（当前 1.20.1 与 1.20.4 源码可暂时相同，有差异时只改对应目录）
5. **产物命名带版本**：`worldsoul-1.20.1.jar` / `worldsoul-1.20.4.jar`，避免 PCL 装错

## 常用命令

```bash
# 构建全部版本
./gradlew buildMods

# 只构建 1.20.1
./gradlew :mod-1.20.1:build
# 产物: mod-1.20.1/build/release/worldsoul-1.20.1.jar
#   或:  mod-1.20.1/build/libs/worldsoul-1.20.1-1.0.0.jar

# 只构建 1.20.4
./gradlew :mod-1.20.4:build

# 开发运行
./gradlew :mod-1.20.1:runClient
./gradlew :mod-1.20.4:runClient
```

## 新增一个 MC 版本（清单）

1. 新建 `versions/1.21.1.properties`（填 yarn / fabric-api / loader / `minecraft_version_range`）
2. 复制最近的 `mod-1.20.4/` → `mod-1.21.1/`，按 mappings 修复编译错误
3. `mod-1.21.1/build.gradle` 指向新 properties，并 `apply from: fabric-mod.gradle`
4. `settings.gradle`：`include 'mod-1.21.1'`
5. `buildMods` / CI / 文档补上新工程
6. 在目标版本上 `:mod-1.21.1:runClient` 冒烟（欢迎语 + `@AI`）

## 改公共逻辑时

| 改动类型 | 改哪里 |
|----------|--------|
| Harness HTTP / 配置 | `mod-common`（一次，所有版本受益） |
| Agent / Provider | `agent-core` |
| 按键、聊天事件、HUD、MC 文本 API | 各 `mod-*`（可能每个版本都要改） |

## PCL 安装

| 游戏版本 | 使用的 jar |
|----------|------------|
| 1.20.1 Fabric | `mod-1.20.1/build/release/worldsoul-1.20.1.jar` |
| 1.20.4 Fabric | `mod-1.20.4/build/release/worldsoul-1.20.4.jar` |

仍需安装对应版本的 **Fabric API**，并先启动 Harness：`.\scripts\start-worldsoul-harness.ps1`。
