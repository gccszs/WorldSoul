# PCL / 正式版 Minecraft 安装说明

版本矩阵与工程约定见 [VERSIONING.md](VERSIONING.md)。

## 为什么打进 PCL 后没效果？

常见原因：

1. **MC 版本装错 jar**（最常见）  
   `worldsoul-1.20.4.jar` **不能**装进 1.20.1；反之亦然。  
   `fabric.mod.json` 使用精确谓词（`~1.20.1` / `~1.20.4`）。

2. **Mod jar 无效**（少见）  
   旧 Loom 在 Java 22+ 上可能打出空 jar。本项目 Loom **1.10.5+**。  
   复制前确认 jar **> 50KB**。

3. **缺少 Fabric API**  
   必须安装与 **同一 MC 版本** 匹配的 Fabric API。

4. **Harness 未启动**（Mod 能加载，但 `@AI` 无响应）  
   ```powershell
   .\scripts\start-worldsoul-harness.ps1
   ```

## 推荐：开发调试

```powershell
.\scripts\start-worldsoul-harness.ps1
.\gradlew.bat :mod-1.20.1:runClient   # 或 :mod-1.20.4:runClient
```

## PCL 安装清单

| 步骤 | 1.20.1 | 1.20.4 |
|------|--------|--------|
| 1 | PCL 创建 **1.20.1 Fabric** | PCL 创建 **1.20.4 Fabric** |
| 2 | 安装 Fabric API **1.20.1** | 安装 Fabric API **1.20.4** |
| 3 | `./gradlew :mod-1.20.1:build` | `./gradlew :mod-1.20.4:build` |
| 4 | 复制 `mod-1.20.1/build/release/worldsoul-1.20.1.jar` | 复制 `mod-1.20.4/build/release/worldsoul-1.20.4.jar` |
| 5 | 确认 jar **> 50KB** | 同左 |
| 6 | `.\scripts\start-worldsoul-harness.ps1` | 同左 |
| 7 | 进档看欢迎语 / `@AI` | 同左 |

## 构建失败：空 remapJar

```powershell
.\gradlew.bat --stop
.\gradlew.bat buildMods
```

确认对应 `mod-*/build/release/worldsoul-*.jar` **> 50KB**。

## 环境变量（可选）

- `WORLDSOUL_HARNESS_URL`：默认 `http://127.0.0.1:8787/worldsoul`
- `WORLDSOUL_HARNESS_ENABLED=false`：关闭与 harness 的连接

## 进游戏后如何确认 Mod 已加载

- 日志：`WorldSoul mod ready (harness=...)`
- 进存档：`欢迎使用 WorldSoul！`
- Harness 正常时：`[WorldSoul] 伴生灵已就绪，使用 @AI 开始对话。`
