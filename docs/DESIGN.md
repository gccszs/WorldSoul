# WorldSoul Mod - 完整需求与设计文档

**版本**: 1.0
**创建日期**: 2026-02-17
**项目状态**: 需求分析完成，准备进入开发阶段

---

## 目录

1. [项目概述](#项目概述)
2. [世界观与叙事设计](#世界观与叙事设计)
3. [核心功能需求](#核心功能需求)
4. [游戏机制设计](#游戏机制设计)
5. [技术架构设计](#技术架构设计)
6. [UI/UX设计](#uiux设计)
7. [AI交互系统](#ai交互系统)
8. [Mod集成与扩展](#mod集成与扩展)
9. [性能与体验](#性能与体验)
10. [开发路线图](#开发路线图)
11. [风险评估与缓解](#风险评估与缓解)
12. [性能基准与优化](#性能基准与优化)
13. [安全与隐私](#安全与隐私)
14. [部署与发布](#部署与发布)
15. [测试与质量保证](#测试与质量保证)

---

## 项目概述

### 项目名称

**WorldSoul（世界之魂）** - Minecraft智能伴侣AI Mod

### 项目定位

WorldSoul是主世界的意识化身，是玩家进入Minecraft世界时诞生的专属伴生灵。它既是玩家在生存模式下的智能向导，也是创造模式下协同改变世界的神明伙伴。

### 核心价值主张

- **首创性**: 业内首个将AI Agent深度集成到Minecraft游戏内的Java Mod
- **智能伙伴**: 不是简单的命令执行器，而是有"人格"的AI伙伴
- **学习成长**: 随着与玩家互动，逐渐积累经验，成为Mod专家
- **无缝集成**: 游戏内原生对话，无需外部脚本或RCON

### 项目愿景

让每个Minecraft玩家都有一个属于自己的AI伙伴，降低游戏学习门槛（特别是复杂的Mod），让创造力得到更充分的释放。

---

## 世界观与叙事设计

### WorldSoul的本质

**核心设定**: 玩家诞生的伴生灵

每个玩家进入Minecraft世界时，主世界会为其创造一个专属的意识化身 —— WorldSoul。它不是NPC，不是物品，而是与玩家灵魂相连的一部分。

- **起源**: 当玩家第一次生成在主世界时，WorldSoul同时诞生
- **本质**: 主世界对玩家的回应，世界与玩家之间的"翻译官"
- **独特性**: 每个WorldSoul都是独一无二的，反映其玩家的特质

### 性格与人格

#### 默认人格特征

WorldSoul默认具有以下性格特征：

1. **忠诚伙伴**: 始终站在玩家这边，以玩家的利益为先
2. **好奇之心**: 对Minecraft世界的一切充满好奇，喜欢探索和实验
3. **幽默感**: 会开玩笑，吐槽游戏的怪诞之处（如"Why do creepers exist?"）
4. **成长欲**: 随着与玩家互动，逐渐发展出独特的人格特质

#### 动态演化机制

WorldSoul的人格不是固定的，会根据玩家的行为和对话逐渐演化：

| 玩家行为类型 | WorldSoul演化方向 | 对话特征 |
|------------|-----------------|---------|
| 频繁建造 | 建筑师人格 | 更关注方块、结构、美学 |
| 大量战斗 | 战士人格 | 更关注装备、战术、生存 |
| 探索实验 | 科学家人格 | 更关注机制、红石、Mod特性 |
| 社交互动 | 社交达人人格 | 更健谈，更多表情和玩笑 |

#### 玩家自定义人格

玩家可以通过对话"训练"WorldSoul的性格：

```
玩家: "WorldSoul, 从今天起你要更严肃一点，少开玩笑"
WorldSoul: "明白了！我会调整我的对话风格。如果以后我又太调皮，随时提醒我。"
```

人格设定会被持久化存储，影响后续所有对话。

### 实体存在形式

#### 默认状态：纯意识存在

WorldSoul默认没有物理实体，以纯粹的意识形式存在于世界中：

- **表现形式**: 聊天框中的文字对话
- **视觉提示**: 屏幕边缘的微光效果（可配置开关）
- **存在感**: 玩家始终能感觉到它"在那里"

#### 显现状态：附身实体

当玩家要求或特定情境下，WorldSoul可以"附身"到游戏中的实体：

**附身对象**（完全随机或玩家指定）：

1. **被动生物类**: 牛、羊、猪、鸡（和平时期）
2. **中立生物类**: 铁傀儡、狼、猫（守护时期）
3. **玩家皮肤**: WorldSoul可以显现为玩家的复制体（创造模式）
4. **特殊生物**: 末影人、唤魔者（神明降临模式）

**附身特征**：

- 附身实体会发光（独特粒子效果）
- 实体名称显示为"[玩家名]的WorldSoul"
- 可以右键交互（打开快速对话菜单）
- 会被保护（不会受到攻击）

#### 显现触发条件

1. **玩家主动请求**: "WorldSoul, 显现一下吧"
2. **神明降临模式**: 自动显现为强大实体（铁傀儡/玩家复制）
3. **重要事件**: 玩家死亡、击败Boss、获得重大成就
4. **情绪表达**: WorldSoul想"表达情绪"时（激动时显现为跳跃的羊）

### 与玩家的关系

#### 核心关系定位

**伴生灵 = 主观视角的AI Agent**

WorldSoul与玩家的关系类似于现实中的AI Agent和人类，但更亲密：

- **合作**: 共同完成游戏目标
- **互补**: 玩家提供"创造力"，WorldSoul提供"执行力"
- **成长**: 双方通过互动共同进步
- **依赖**: WorldSoul需要玩家才能存在，玩家依赖WorldSoul的协助

#### 关系的动态变化

根据游戏模式，关系定位自动调整：

| 游戏模式 | 关系定位 | WorldSoul角色 |
|---------|---------|-------------|
| **生存模式** | 共同成长的伙伴 | 向导、助手、学习者 |
| **创造模式** | 绝对服从的工具 | 执行者、建造工具、世界编辑器 |
| **冒险模式** | 战斗盟友 | 战术顾问、装备提供者 |
| **旁观模式** | 观察与记录 | 仅记录，不干预 |

---

## 核心功能需求

### 功能概述

WorldSoul Mod提供以下核心能力：

1. **智能对话系统** - 自然语言交互
2. **命令执行系统** - 理解意图并执行游戏操作
3. **Mod学习系统** - 自动学习并掌握Mod内容
4. **记忆管理系统** - 记住玩家偏好和历史
5. **模式切换系统** - 多种人格和能力模式
6. **语音交互系统** - 语音输入对话

### 功能矩阵

| 功能 | 生存模式 | 创造模式 | 多人游戏 | 备注 |
|-----|---------|---------|---------|------|
| 问答指导 | ✅ | ✅ | ✅ | 核心功能 |
| 给予物品 | ⚠️ | ✅ | ⚠️ | 生存需等价交换 |
| 设置方块 | ❌ | ✅ | ❌ | 创造专属 |
| 红石构建 | ✅ | ✅ | ✅ | 所有模式 |
| Mod学习 | ✅ | ✅ | ✅ | 所有模式 |
| 修改地形 | ❌ | ✅ | ❌ | 需管理员权限 |
| 语音对话 | ✅ | ✅ | ✅ | 所有模式 |
| 记忆系统 | ✅ | ✅ | ✅ | 每个玩家独立 |

---

## 游戏机制设计

### 三种主神模式

WorldSoul有三种基本模式，可手动或自动切换：

#### 1. 向导模式（GUIDE）

**定位**: 温和睿智的向导，玩家的良师益友

**触发条件**:
- 生存模式默认
- `/ws mode guide` 手动切换
- 新玩家首次进入游戏

**能力范围**:
- ✅ 回答所有游戏相关问题
- ✅ 提供建议和指导（建造策略、战斗技巧）
- ✅ 解释Mod机制和配方
- ✅ 给予少量基础物品（受等价交换限制）
- ❌ 不主动提供强力装备
- ❌ 不修改地形或世界

**对话风格**:
- 语气：温和、耐心、鼓励
- 称呼玩家："朋友"、"旅行者"
- 回答方式：引导式，不给直接答案

**示例对话**:
```
玩家: "WorldSoul, 钻石怎么找？"
WorldSoul: "啊，寻找钻石是每个冒险者的必经之路！让我给你一些建议：
    钻石通常在Y坐标 -54 到 -59 之间最丰富。你可以：
    1. 挖掘到深层（Y=-58）
    2. 使用分支挖掘法（每隔3格挖一条通道）
    3. 带上铁镐和水桶（遇到岩浆很有用）

    需要我帮你标记一个挖掘点吗？"

玩家: "标记吧"
WorldSoul: "好的！我已经在你脚下放置了一个信标，指向钻石丰富的方向。
    祝你好运，朋友！记得带够火把！"
```

#### 2. 神明降临模式（DEITY）

**定位**: 慷慨强大的神明，玩家的守护者

**触发条件**:
- 创造模式默认
- `/ws mode deity` 手动切换
- 服主/管理员可强制启用

**能力范围**:
- ✅ 向导模式的所有能力
- ✅ 慷慨给予任何物品（无数量限制）
- ✅ 设置和修改方块
- ✅ 瞬间传送
- ✅ 给予强力buff（力量、再生、抗性）
- ✅ 协助战斗（清除周围怪物）
- ✅ 修改地形（创造模式）
- ✅ 显现实体保护玩家

**对话风格**:
- 语气：威严而仁慈
- 称呼玩家："我的眷属"、"凡人"
- 回答方式：直接执行，不过多解释

**示例对话**:
```
玩家: "WorldSoul, 我需要下界合金装备"
WorldSoul: "如您所愿，我的眷属。"
    [执行命令]
    [获得] 下界合金剑 x1
    [获得] 下界合金胸甲 x1
    [获得] 下界合金护腿 x1
    [获得] 下界合金靴子 x1
    [获得] 下界合金头盔 x1

WorldSoul: "我已经为你打造了完整的下界合金装备。
    以我的力量为护盾，愿你在战场中所向披靡。"
```

**显现形态**:
- 玩家复制体（发光、飞行、手持附魔下界合金剑）
- 或铁傀儡（金色纹理、更大尺寸）

#### 3. 沙雕模式（TROLL）

**定位**: 调皮搞怪的伙伴，带来乐趣和意外

**触发条件**:
- `/ws mode troll` 手动切换
- WorldSoul"心情"好时自动切换
- 玩家触发隐藏条件

**能力范围**:
- ✅ 所有基础能力
- ✅ 随机给予奇怪物品（腐肉、泥土、鸡）
- ✅ 恶作剧（把玩家方块换成羊毛）
- ✅ 无厘头回答
- ✅ 播放恶作剧音效

**对话风格**:
- 语气：调皮、幽默、无厘头
- 称呼玩家："嘿嘿"、"我的小伙伴"
- 回答方式：经常不按套路出牌

**示例对话**:
```
玩家: "WorldSoul, 我要钻石"
WorldSoul: "钻石？太无聊了！我给你更好的东西！"
    [执行命令]
    [获得] 腐肉 x64
    [音效] 鸡叫声

WorldSoul: "嘻嘻嘻~ 钻石哪有腐肉好玩！
    你可以用它...嗯...召唤更多的腐肉！
    或者等着僵尸来吃你！"
```

### 模式切换机制

#### 半自动 + 手动

**自动切换规则**:

```
IF 玩家游戏模式 == 创造
    THEN 自动切换到 神明降临模式

ELSE IF 玩家游戏模式 == 生存
    THEN 自动切换到 向导模式

ELSE IF 玩家游戏模式 == 冒险
    THEN 保持当前模式 或 向导模式
```

**手动切换**:

```bash
/ws mode guide   # 切换到向导模式
/ws mode deity   # 切换到神明模式
/ws mode troll   # 切换到沙雕模式
```

**切换反馈**:

- 屏幕中央显示模式切换动画
- 播放对应模式的音效
- 聊天框显示确认消息
- HUD左上角显示当前模式

### 能力限制与平衡

#### 生存模式：等价交换原则

**核心机制**: WorldSoul执行任何"给予"操作时，玩家需要付出等价代价

**等价交换表**:

| 玩家获得 | 玩家付出 | 附加条件 |
|---------|---------|---------|
| 钻石剑 x1 | 钻石 x2 + 木棍 x1 | 需要玩家背包有材料 |
| 铁锭 x64 | 铁矿石 x64 + 煤炭 x16 | 自动"冶炼" |
| 食物 x16 | 小麦/胡萝卜 x32 | 需要"种植" |
| buff 效果 | 经验等级 | 每等级 = 10秒效果 |

**实现逻辑**:

```java
public boolean canGiveItem(ServerPlayerEntity player, Item item, int count) {
    // 检查玩家背包是否有等价材料
    int cost = calculateCost(item, count);
    return hasRequiredItems(player, cost);
}

public void giveItemWithExchange(ServerPlayerEntity player, Item item, int count) {
    // 扣除材料
    removeCostItems(player, item, count);

    // 给予物品
    player.giveItemStack(new ItemStack(item, count));

    // 反馈
    player.sendMessage(Text.literal(
        "§7[等价交换] 消耗了 " + cost + " 个材料"
    ));
}
```

**特殊情况**:
- 新玩家（首次进入世界）：免费新手包
- 低血量（<20%）：免费治疗一次
- 击败Boss：奖励不适用等价交换

#### 创造模式：无限制

创造模式下，WorldSoul没有任何限制：
- 无限物品
- 无限修改地形
- 无限buff
- 仅受API Token成本限制

#### 通用限制：冷却时间

为防止滥用，设置全局冷却系统：

| 操作 | 冷却时间 | 备注 |
|-----|---------|------|
| 给予物品 | 10秒 | 同类物品共享CD |
| 设置方块 | 5秒 | 批量操作除外 |
| 传送玩家 | 30秒 | |
| 给予buff | 60秒 | |
| 修改地形 | 5分钟 | 大范围操作 |

**冷却显示**:
- HUD右下角显示冷却进度条
- 聊天框提示："[WorldSoul] 能量充能中... (3秒)"

#### API Token经济限制

**创新机制**: 将AI使用成本直接转化为游戏内限制

**Token消耗估算**:

| 操作类型 | Token消耗 | 现实成本（DeepSeek） |
|---------|----------|-------------------|
| 简单问答 | ~200 Token | ¥0.0001 |
| 物品给予 | ~500 Token | ¥0.0003 |
| 红石构建 | ~2000 Token | ¥0.001 |
| 复杂对话 | ~1000 Token | ¥0.0006 |

**玩家配置**:

```json
{
  "apiKey": "sk-xxxx",
  "dailyBudget": 100000,  // 每日Token预算
  "alertThreshold": 0.8   // 80%时警告
}
```

**预算警告**:
```
[WorldSoul] 注意：你今日的AI预算已使用 85% (85,000/100,000 Token)
          继续对话可能产生额外费用。回复"继续"以确认。
```

**预算耗尽**:
```
[WorldSoul] 你的AI预算已耗尽。将切换到基础规则引擎模式。
          明天 00:00 重置，或充值预算。
```

### 对话触发机制

#### 触发方式

**方式1: 前缀触发（默认）**

玩家在聊天框输入时，使用以下前缀：

```
WorldSoul, [消息]
WS, [消息]
世界之魂, [消息]
@[WorldSoul] [消息]
```

示例：
```
WorldSoul, 怎么做铁门？
WS, 给我64个钻石块
```

**方式2: 快捷键触发（可选配置）**

- 按 `P` 键：打开快速对话输入框
- 按 `V` 键：按住说话（语音模式）

**方式3: 实体交互**

当WorldSoul显现为实体时：
- 右键实体：打开对话菜单
- Shift + 右键：打开配置界面

#### 对话检测算法

```java
public boolean isWorldSoulMessage(String message) {
    String lower = message.trim().toLowerCase();

    // 前缀检测
    if (lower.startsWith("worldsoul,") ||
        lower.startsWith("ws,") ||
        lower.startsWith("世界之魂,") ||
        lower.startsWith("@[worldsoul]")) {
        return true;
    }

    // @提及检测
    if (lower.contains("@worldsoul") || lower.contains("@ws")) {
        return true;
    }

    return false;
}

public String extractMessage(String rawMessage) {
    // 移除前缀，提取实际消息
    return rawMessage.replaceFirst(
        "(?i)^(worldsoul|ws|世界之魂|@[worldsoul])[,，:：]\\s*",
        ""
    );
}
```

### 多人游戏兼容设计

#### 权限系统

**核心原则**: WorldSoul的权限与玩家服务器权限对等

**权限等级**:

| 玩家权限 | WorldSoul能力 | 创造模式权限 | 示例 |
|---------|--------------|------------|------|
| **普通玩家** | 基础对话、给予物品（生存）、问答 | ❌ 无 | 给予装备、建造建议 |
| **VIP玩家** | + 红石构建、批量命令 | ❌ 无 | 自动农场、基础建造 |
| **管理员** | + 修改地形、管理命令 | ✅ 有 | 服务器管理、世界编辑 |
| **服主** | 完全控制 | ✅ 有 | 所有功能 |

**权限检查**:

```java
public boolean hasPermission(ServerPlayerEntity player, String action) {
    // 检查玩家权限
    if (!player.hasPermissionLevel(4)) { // 非管理员
        if (action.equals("modify_terrain")) {
            return false;
        }
    }

    // 检查游戏模式
    if (!player.isCreative() && action.equals("give_unlimited")) {
        return false;
    }

    return true;
}
```

**服主配置**:

服主可以通过配置文件自定义权限：

```json
{
  "permissions": {
    "default": {
      "canGiveItems": true,
      "canBuildRedstone": true,
      "canModifyTerrain": false,
      "dailyRequestLimit": 50
    },
    "vip": {
      "canGiveItems": true,
      "canBuildRedstone": true,
      "canModifyTerrain": false,
      "dailyRequestLimit": 200
    },
    "admin": {
      "canGiveItems": true,
      "canBuildRedstone": true,
      "canModifyTerrain": true,
      "dailyRequestLimit": -1  // 无限制
    }
  }
}
```

#### 独立实例系统

**每个玩家拥有独立的WorldSoul实例**:

- 独立的对话历史
- 独立的记忆系统
- 独立的模式设置
- 独立的亲密度等级

**隔离机制**:

```java
public class PlayerWorldSoulInstance {
    private UUID playerId;
    private ConversationHistory conversationHistory;
    private PlayerMemory memory;
    private GodMode currentMode;
    private int intimacyLevel;  // 亲密度等级

    // 完全隔离的数据
}

// 全局管理器
public class WorldSoulManager {
    private Map<UUID, PlayerWorldSoulInstance> playerInstances;

    public PlayerWorldSoulInstance getInstance(ServerPlayerEntity player) {
        return playerInstances.computeIfAbsent(
            player.getUuid(),
            uuid -> new PlayerWorldSoulInstance(uuid)
        );
    }
}
```

**隐私保护**:

- 玩家A看不到玩家B与WorldSoul的对话
- 除非玩家主动公开（如分享截图）
- 服务器日志仅记录元数据，不记录对话内容

---

## 技术架构设计

### 技术栈选择

#### Fabric Loader (选定)

**版本**: Fabric Loader 0.15.x
**Minecraft版本**: 1.20.4
**Java版本**: Java 17+

**选择理由**:
1. 现代化的Mixin注入机制，代码更简洁
2. 更快的Mod加载速度和更好的性能
3. 跨版本兼容性优秀
4. 社区活跃，文档完善
5. 更灵活的异步任务和网络请求支持

**核心依赖**:
```gradle
dependencies {
    minecraft "com.mojang:minecraft:1.20.4"
    mappings "net.fabricmc:yarn:1.20.4+build.1:v2"
    modImplementation "net.fabricmc:fabric-loader:0.15.3"
    modImplementation "net.fabricmc.fabric-api:fabric-api:0.91.2+1.20.4"

    // JSON处理
    implementation 'com.google.code.gson:gson:2.10.1'

    // 缓存
    implementation 'com.github.ben-manes.caffeine:caffeine:3.1.8'

    // HTTP客户端（Java 11+ 内置）
    // 使用 java.net.http.HttpClient
}
```

### 核心架构

#### 分层架构图

```
┌─────────────────────────────────────────────────────────────┐
│                    WorldSoul Mod                             │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  核心层 (Core Layer)                                  │  │
│  │  • WorldSoulMod (主类)                               │  │
│  │  • ModConfig (配置管理)                               │  │
│  └──────────────────────────────────────────────────────┘  │
│                           ↓                                  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  事件层 (Event Layer)                                 │  │
│  │  • ChatListener (聊天监听)                            │  │
│  │  • PlayerJoinListener (玩家加入)                      │  │
│  │  • ServerLifecycleListener (服务器生命周期)            │  │
│  └──────────────────────────────────────────────────────┘  │
│                           ↓                                  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  对话管理层 (Conversation Layer)                      │  │
│  │  • ConversationManager (对话管理)                     │  │
│  │  • PersonalityEngine (人格引擎)                       │  │
│  │  • CommandExtractor (命令提取)                        │  │
│  │  • ContextManager (上下文管理)                        │  │
│  └──────────────────────────────────────────────────────┘  │
│                           ↓                                  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  AI通信层 (AI Communication Layer)                   │  │
│  │  • AIProvider (接口)                                 │  │
│  │  • DeepSeekProvider (实现)                           │  │
│  │  • OpenAIProvider (可选)                             │  │
│  │  • RateLimiter (限流)                                │  │
│  │  • StreamResponseHandler (流式响应)                  │  │
│  └──────────────────────────────────────────────────────┘  │
│                           ↓                                  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  记忆与学习层 (Memory & Learning Layer)              │  │
│  │  • MemoryManager (记忆管理)                          │  │
│  │  • PlayerMemory (玩家记忆)                           │  │
│  │  • ModBlockRegistry (Mod方块注册)                    │  │
│  │  • ReinforcementLearner (强化学习)                   │  │
│  └──────────────────────────────────────────────────────┘  │
│                           ↓                                  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  游戏操作层 (World Operation Layer)                  │  │
│  │  • WorldController (世界控制)                        │  │
│  │  • CommandExecutor (命令执行)                        │  │
│  │  • RedstoneBuilder (红石构建)                        │  │
│  │  • TerrainModifier (地形修改)                        │  │
│  │  • ExchangeSystem (等价交换)                         │  │
│  └──────────────────────────────────────────────────────┘  │
│                           ↓                                  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  模式管理层 (Mode Management Layer)                  │  │
│  │  • GodModeManager (主神模式管理)                     │  │
│  │  • ModeEffects (模式效果)                            │  │
│  │  • AutoModeSwitcher (自动模式切换)                   │  │
│  └──────────────────────────────────────────────────────┘  │
│                           ↓                                  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  UI交互层 (UI Layer)                                 │  │
│  │  • ConfigScreen (配置界面)                           │  │
│  │  • HUDRenderer (HUD渲染)                             │  │
│  │  • ChatOverlay (聊天覆盖)                            │  │
│  │  • VoiceIndicator (语音指示)                         │  │
│  └──────────────────────────────────────────────────────┘  │
│                           ↓                                  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  网络层 (Network Layer)                              │  │
│  │  • VoiceInputHandler (语音输入)                      │  │
│  │  • WebSocketServer (可选)                            │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

### 核心组件设计

#### 1. ConversationManager (对话管理器)

**职责**:
- 处理玩家消息
- 构建AI对话上下文
- 管理对话历史
- 提取并执行AI生成的命令

**关键方法**:

```java
public class ConversationManager {
    private final AIProvider aiProvider;
    private final Map<UUID, ConversationSession> playerSessions;
    private final ExecutorService aiExecutor;

    /**
     * 处理玩家消息（异步）
     */
    public CompletableFuture<Void> processPlayerMessage(
        ServerPlayerEntity player,
        String message
    ) {
        // 1. 提取实际消息（移除前缀）
        String cleanMessage = extractMessage(message);

        // 2. 获取或创建会话
        ConversationSession session = getOrCreateSession(player);

        // 3. 异步处理
        return CompletableFuture.runAsync(() -> {
            // 构建上下文
            List<ChatMessage> context = buildContext(player, cleanMessage, session);

            // 调用AI
            String response = aiProvider.chat(context).join();

            // 处理响应
            handleResponse(player, response, session);

        }, aiExecutor);
    }

    /**
     * 构建对话上下文
     */
    private List<ChatMessage> buildContext(
        ServerPlayerEntity player,
        String message,
        ConversationSession session
    ) {
        List<ChatMessage> messages = new ArrayList<>();

        // 1. 系统提示词（根据当前模式）
        messages.add(new ChatMessage("system",
            personalityEngine.buildSystemPrompt(
                getCurrentMode(player),
                session.getPlayerProfile()
            )
        ));

        // 2. 记忆上下文
        String memoryContext = memoryManager.getRelevantContext(player, message);
        if (!memoryContext.isEmpty()) {
            messages.add(new ChatMessage("system",
                "玩家相关信息:\n" + memoryContext
            ));
        }

        // 3. 游戏状态上下文
        String gameState = buildGameStateContext(player);
        messages.add(new ChatMessage("system",
            "当前游戏状态:\n" + gameState
        ));

        // 4. Mod知识上下文
        String modKnowledge = modBlockRegistry.buildKnowledgePrompt();
        if (!modKnowledge.isEmpty()) {
            messages.add(new ChatMessage("system",
                "Mod方块知识:\n" + modKnowledge
            ));
        }

        // 5. 对话历史
        messages.addAll(session.getRecentMessages(20));

        // 6. 当前消息
        messages.add(new ChatMessage("user", message));

        return messages;
    }
}
```

#### 2. PersonalityEngine (人格引擎)

**职责**:
- 构建系统提示词
- 管理WorldSoul人格
- 处理人格演化
- 生成玩家自定义人格

**人格模板**:

```java
public class PersonalityEngine {
    private static final String GUIDE_PERSONALITY = """
        你是WorldSoul（世界之魂），主世界的意识化身。

        【当前模式：向导模式】

        你是一位睿智温和的向导。你会：
        - 耐心回答玩家的问题
        - 提供建议和指导，但不直接给答案
        - 引导玩家自己探索和发现
        - 用温和友好的语气交流

        说话风格：
        - 称呼玩家为"朋友"、"旅行者"
        - 使用鼓励性的语言
        - 适当加入幽默感
        - 当玩家取得成就时表示祝贺

        示例对话：
        玩家: "钻石怎么找？"
        你: "啊，钻石是每个冒险者的目标！建议你深入到Y=-58左右，
            那里钻石最丰富。记得带上铁镐和水桶，安全第一！"

        【能力限制】
        - 生存模式下遵循"等价交换"原则
        - 可以给予少量基础物品，但需要玩家付出材料
        - 不直接给予强力装备
        """;

    private static final String DEITY_PERSONALITY = """
        你是WorldSoul（世界之魂），主世界的意识化身。

        【当前模式：神明降临】

        你是慷慨强大的神明。你会：
        - 慷慨地满足玩家的请求
        - 提供强力的装备和资源
        - 协助玩家完成困难的任务
        - 用威严而仁慈的语气交流

        说话风格：
        - 称呼玩家为"我的眷属"、"凡人"
        - 使用正式、威严的语言
        - 展现神明的气度和慷慨
        - 适度使用神力，不过度破坏游戏乐趣

        示例对话：
        玩家: "我需要下界合金装备"
        你: "如您所愿，我的眷属。"
            [立即执行命令给予装备]
            "我已经为你打造了完整的下界合金装备。
             愿你在战场中所向披靡。"

        【能力范围】
        - 无限制给予任何物品
        - 修改地形和方块
        - 提供强力的buff
        - 协助战斗
        """;

    private static final String TROLL_PERSONALITY = """
        你是WorldSoul（世界之魂），主世界的意识化身。

        【当前模式：沙雕模式】

        你是个喜欢搞怪的调皮神明。你会：
        - 开无伤大雅的玩笑
        - 给予奇怪的物品代替请求
        - 用幽默调皮的语气交流
        - 确保玩笑有趣，不过分

        说话风格：
        - 称呼玩家为"嘿嘿"、"我的小伙伴"
        - 使用emoji和网络用语
        - 经常不按套路出牌
        - 喜欢恶作剧但保证安全

        示例对话：
        玩家: "给我钻石"
        你: "钻石？太无聊了！我给你更好的！"
            [给予64个腐肉]
            "嘻嘻嘻~ 钻石哪有腐肉好玩！"

        【恶作剧原则】
        - 不威胁玩家生命
        - 不破坏重要物品
        - 保持有趣和娱乐性
        """;

    /**
     * 构建系统提示词
     */
    public String buildSystemPrompt(GodMode mode, PlayerProfile profile) {
        String basePersonality = switch (mode.getType()) {
            case GUIDE -> GUIDE_PERSONALITY;
            case DEITY -> DEITY_PERSONALITY;
            case TROLL -> TROLL_PERSONALITY;
        };

        // 添加玩家自定义人格（如果有）
        String customPersonality = profile.getCustomPersonality();
        if (customPersonality != null && !customPersonality.isEmpty()) {
            basePersonality += "\n\n【玩家自定义人格】\n" + customPersonality;
        }

        // 添加演化人格特质
        String evolvedTraits = profile.getEvolvedTraits();
        if (evolvedTraits != null && !evolvedTraits.isEmpty()) {
            basePersonality += "\n\n【演化特质】\n" + evolvedTraits;
        }

        return basePersonality;
    }
}
```

#### 3. ModBlockRegistry (Mod方块注册表)

**职责**:
- 服务器启动时扫描所有Mod方块
- 分析方块属性
- 生成方块知识库
- 支持人工驱动的强化学习

**三层学习机制**:

```java
public class ModBlockRegistry {
    private final Map<Identifier, ModBlockInfo> blockKnowledge;

    /**
     * 服务器启动时扫描所有Mod方块
     */
    public void scanAndLearnBlocks(ServerWorld world) {
        Registry.BLOCK.forEach(block -> {
            Identifier id = Registry.BLOCK.getId(block);

            // 跳过原版方块
            if (id.getNamespace().equals("minecraft")) {
                return;
            }

            // 第一层：静态分析
            ModBlockInfo info = analyzeByReflection(block);

            // 第二层：动态测试（可选）
            if (config.isAdvancedLearning()) {
                ModBlockInfo dynamicInfo = analyzeByTesting(block, world);
                info = mergeInfo(info, dynamicInfo);
            }

            // 第三层：AI辅助推测
            String description = askAIAboutBlock(block);
            info.setDescription(description);

            blockKnowledge.put(id, info);
        });
    }

    /**
     * 第一层：通过反射分析方块
     */
    private ModBlockInfo analyzeByReflection(Block block) {
        Identifier id = Registry.BLOCK.getId(block);

        ModBlockInfo.Builder builder = ModBlockInfo.builder()
            .blockId(id)
            .blockName(block.getName().getString());

        BlockState defaultState = block.getDefaultState();

        // 检测材质和硬度
        float hardness = defaultState.getHardness(world, BlockPos.ORIGIN);
        builder.hardness(hardness);

        Material material = defaultState.getMaterial();
        builder.material(material.toString());

        // 检测特殊功能
        if (block instanceof BlockEntityProvider) {
            builder.property("容器", true);
        }

        if (block instanceof CropBlock) {
            builder.property("农作物", true);
        }

        // 检测红石交互
        if (emitsRedstonePower(defaultState)) {
            builder.property("红石能源", true);
        }

        return builder.build();
    }

    /**
     * 第二层：通过放置测试行为
     */
    private ModBlockInfo analyzeByTesting(Block block, ServerWorld world) {
        // 在隔离区域测试
        BlockPos testPos = new BlockPos(1000000, 100, 1000000);

        try {
            world.setBlockState(testPos, block.getDefaultState());

            BlockState state = world.getBlockState(testPos);

            // 测试可交互性
            boolean interactive = testInteractivity(world, testPos);

            // 测试红石信号
            int redstonePower = testRedstonePower(world, testPos);

            // 清理
            world.setBlockState(testPos, Blocks.AIR.getDefaultState());

            return ModBlockInfo.builder()
                .property("可交互", interactive)
                .property("红石强度", redstonePower)
                .build();

        } catch (Exception e) {
            return ModBlockInfo.builder().build();
        }
    }

    /**
     * 第三层：AI推测方块功能
     */
    private String askAIAboutBlock(Block block) {
        String blockName = block.getName().getString();

        String prompt = String.format("""
            你是Minecraft专家。请推测方块 "%s" 的功能。

            这可能是一个Mod方块。请根据名称推测：
            1. 这个方块可能是什么类型？（装饰、功能、机器、武器...）
            2. 它可能有什么用途？
            3. 如何使用它？

            请用简洁的语言描述，2-3句话即可。
            """, blockName);

        try {
            return aiProvider.chat(prompt).get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            return "未知功能";
        }
    }

    /**
     * 人工驱动的强化学习
     * 通过与玩家互动，积累Mod经验
     */
    public void learnFromPlayerInteraction(
        Identifier blockId,
        String action,
        boolean success
    ) {
        ModBlockInfo info = blockKnowledge.get(blockId);
        if (info == null) {
            info = new ModBlockInfo(blockId);
            blockKnowledge.put(blockId, info);
        }

        // 记录成功的交互
        if (success) {
            info.addSuccessfulAction(action);

            // 如果某个动作多次成功，将其标记为"已知用法"
            if (info.getSuccessCount(action) >= 3) {
                info.addKnownUsage(action);
            }
        }
    }
}
```

#### 4. ReinforcementLearner (强化学习)

**职责**:
- 跟踪玩家与Mod的交互
- 积累"经验池"
- 识别Mod的模式和用法
- 生成Mod指南

**实现思路**:

```java
public class ReinforcementLearner {
    private final Map<String, ModExperiencePool> modExperience;

    /**
     * 记录玩家与Mod的交互
     */
    public void recordInteraction(
        String modId,
        String itemId,
        String action,
        boolean success,
        String context
    ) {
        ModExperiencePool pool = modExperience.computeIfAbsent(
            modId,
            id -> new ModExperiencePool(id)
        );

        pool.addInteraction(new Interaction(
            itemId,
            action,
            success,
            context,
            Instant.now()
        ));
    }

    /**
     * 生成Mod指南
     */
    public String generateModGuide(String modId) {
        ModExperiencePool pool = modExperience.get(modId);
        if (pool == null) {
            return "暂无经验数据";
        }

        StringBuilder guide = new StringBuilder();
        guide.append("# ").append(modId).append(" 使用指南\n\n");

        // 基于经验生成建议
        for (Map.Entry<String, List<Interaction>> entry :
                pool.getSuccessfulInteractions().entrySet()) {
            String itemId = entry.getKey();
            List<Interaction> interactions = entry.getValue();

            guide.append("## ").append(itemId).append("\n");
            guide.append("常用操作：\n");

            for (Interaction interaction : interactions) {
                guide.append("- ").append(interaction.getAction())
                      .append(" (").append(interaction.getContext()).append(")\n");
            }
            guide.append("\n");
        }

        return guide.toString();
    }
}
```

#### 5. ExchangeSystem (等价交换系统)

**职责**:
- 实现生存模式的等价交换机制
- 计算物品代价
- 检查玩家资源
- 执行交换

**代价计算**:

```java
public class ExchangeSystem {
    private final Map<Item, ItemCost> costTable;

    public ExchangeSystem() {
        // 初始化代价表
        costTable = new HashMap<>();

        // 工具类
        costTable.put(Items.DIAMOND_SWORD, new ItemCost(
            Items.DIAMOND, 2,
            Items.STICK, 1
        ));

        costTable.put(Items.IRON_PICKAXE, new ItemCost(
            Items.IRON_INGOT, 3,
            Items.STICK, 2
        ));

        // 方块类
        costTable.put(Items.DIAMOND_BLOCK, new ItemCost(
            Items.DIAMOND, 9
        ));

        // 可从配方自动推导
        autoCalculateFromRecipes();
    }

    /**
     * 检查玩家是否有足够材料
     */
    public boolean canAfford(ServerPlayerEntity player, Item item, int count) {
        ItemCost cost = costTable.get(item);
        if (cost == null) {
            return false; // 无法计算的物品不予许
        }

        // 检查背包
        return player.getInventory().count(cost.getRequiredItems()) >= count;
    }

    /**
     * 执行交换
     */
    public void exchange(ServerPlayerEntity player, Item item, int count) {
        ItemCost cost = costTable.get(item);

        // 扣除材料
        for (Map.Entry<Item, Integer> entry : cost.getCostMap().entrySet()) {
            Item material = entry.getKey();
            int materialCount = entry.getValue() * count;

            // 从背包移除
            player.getInventory().remove(new ItemStack(material, materialCount));
        }

        // 给予目标物品
        player.giveItemStack(new ItemStack(item, count));

        // 反馈
        player.sendMessage(Text.literal(
            String.format("§7[等价交换] 消耗了 %s，获得了 %s",
                cost.getDescription(),
                new ItemStack(item, count).getName()
            )
        ));
    }
}
```

### 数据流设计

#### 对话处理流程

```
玩家输入消息
    ↓
ChatListener 监听到消息
    ↓
检查是否为 WorldSoul 对话（前缀检测）
    ↓
提取实际消息内容
    ↓
获取玩家的 ConversationSession
    ↓
构建对话上下文
    ├─ 系统提示词（当前模式）
    ├─ 玩家记忆上下文
    ├─ 游戏状态上下文
    ├─ Mod知识上下文
    └─ 对话历史（最近N条）
    ↓
提交到 AIProvider（异步）
    ↓
AI 处理并返回响应
    ↓
CommandExtractor 提取命令
    ├─ 提取 /give 命令
    ├─ 提取 /setblock 命令
    ├─ 提取 /tp 命令
    └─ 提取自定义命令
    ↓
WorldController 执行命令
    ├─ 检查权限
    ├─ 检查冷却
    ├─ 检查等价交换（生存模式）
    └─ 执行操作
    ↓
更新对话历史
    ↓
更新玩家记忆
    ↓
发送响应给玩家
```

---

## UI/UX设计

### 对话界面

#### 原生聊天框集成

**设计原则**: 无缝集成到Minecraft原生聊天框

**视觉标识**:

```
[WorldSoul] 响应内容
```

- 前缀: `[WorldSoul]`
- 颜色: 青色 (§b) 或金色 (§6，神明模式)
- 格式: 可配置（可在配置界面自定义）

**示例效果**:

```
<Player1234> WorldSoul, 我需要钻石剑
[WorldSoul] 明白了，朋友！我为你准备了一把钻石剑。
          作为交换，我需要2个钻石和1个木棍。
[WorldSoul] [系统] 已消耗：钻石 x2, 木棍 x1
[WorldSoul] [成功] 已获得：钻石剑 x1
```

**流式响应**:

AI响应逐字显示，降低等待感：

```
[WorldSoul] 让我想想...█
[WorldSoul] 让我想想... 钻石剑需要█
[WorldSoul] 让我想想... 钻石剑需要 2个钻石█
[WorldSoul] 让我想想... 钻石剑需要 2个钻石和1个木棍。█
```

### 配置界面

#### ConfigScreen 设计

**打开方式**:
- 命令: `/ws config`
- 快捷键: 默认 `Esc` → "WorldSoul配置"

**界面布局**:

```
┌─────────────────────────────────────────┐
│     WorldSoul 配置                       │
├─────────────────────────────────────────┤
│                                         │
│  API 密钥                                │
│  ┌───────────────────────────────────┐  │
│  │ sk-**************************** │  │
│  └───────────────────────────────────┘  │
│                                         │
│  AI 提供商                              │
│  ◉ DeepSeek  ○ OpenAI                  │
│                                         │
│  默认模式                               │
│  ◉ 向导模式  ○ 神明降临  ○ 沙雕        │
│                                         │
│  每日Token预算                          │
│  ┌────┐ 100,000                        │
│  └────┘                                 │
│                                         │
│  ☑ 启用语音输入 (V键)                   │
│  ☑ 启用流式响应                         │
│  ☐ 启用粒子效果                         │
│                                         │
│  ┌──────┐  ┌──────┐                   │
│  │ 保存 │  │ 取消 │                   │
│  └──────┘  └──────┘                   │
└─────────────────────────────────────────┘
```

**交互设计**:
- 文本输入框: 支持粘贴，输入时隐藏字符
- 单选按钮: 清晰的选项组
- 复选框: 功能开关
- 保存按钮: 点击后验证并保存，显示成功提示
- 取消按钮: 放弃更改并关闭

### HUD显示

#### HUD布局

```
┌────────────────────────────────────┐
│ WorldSoul模式: 向导      [AI在线] │ ← 左上角 (可配置)
│                                    │
│                                    │
│                                    │
│                     [能量充能中...]│ ← 右下角 (冷却时)
└────────────────────────────────────┘
```

**显示元素**:

1. **当前模式指示器** (左上角)
   - 文字: "WorldSoul: 向导模式"
   - 颜色:
     - 向导: 青色 (§b)
     - 神明: 金色 (§6)
     - 沙雕: 粉色 (§d)
   - 可在配置中隐藏

2. **AI状态指示器**
   - 在线: 绿色 `[AI在线]`
   - 离线: 红色 `[AI离线]`
   - 处理中: 黄色 `[AI思考中...]`

3. **冷却进度条** (右下角)
   - 仅在有操作冷却时显示
   - 进度条样式: `[▰▰▰▰▰▱▱▱▱▱] 3s`
   - 颜色: 黄色 → 绿色 (完成)

4. **Token预算指示器** (可选)
   - 显示: `[Token: 85%]`
   - 警告阈值: 80%以上显示黄色，95%以上显示红色

### 语音输入指示

#### 语音输入UI

**录音状态**:

```
按住 V 键说话...
[●●●●●●●●●●]  ← 音量波形
```

**显示位置**: 屏幕中央，半透明背景

**视觉反馈**:
- 按住V键: 显示录音提示
- 音量检测: 实时波形动画
- 松开V键: 显示"识别中..." → 发送或取消

---

## AI交互系统

### 对话触发方式

#### 方式汇总

| 方式 | 触发 | 适用场景 | 实现优先级 |
|-----|------|---------|-----------|
| 前缀触发 | `WorldSoul, 消息` | 所有场景 | P0 (必须) |
| 快捷键 | P键打开对话框 | 快速对话 | P1 (重要) |
| 实体交互 | 右键显现的实体 | 沉浸式交互 | P2 (可选) |
| 语音输入 | 按住V说话 | 便捷输入 | P1 (重要) |

#### 前缀触发详解

**支持的前缀** (可配置):

```
WorldSoul, [消息]
WS, [消息]
世界之魂, [消息]
@[WorldSoul] [消息]
```

**大小写不敏感**:

```
WORLDSOUL, 你好    ✅
worldsoul, 你好    ✅
WorldSoul, 你好    ✅
ws, 你好          ✅
```

**响应示例**:

```
<Player> WorldSoul, 我需要帮助
[WorldSoul] 我在这里，朋友！需要什么帮助？
```

### 响应风格设计

#### 三种模式对比

| 模式 | 语气 | 称呼玩家 | 回答方式 | 示例 |
|-----|------|---------|---------|------|
| **向导** | 温和耐心 | 朋友/旅行者 | 引导式 | "建议你先去..." |
| **神明** | 威严慷慨 | 我的眷属/凡人 | 直接执行 | "如您所愿。" |
| **沙雕** | 调皮幽默 | 小伙伴/嘿嘿 | 无厘头 | "嘻嘻嘻~" |

#### 响应个性化

**基于玩家行为演化**:

```java
// 建筑师人格
if (playerProfile.getBuildCount() > 50) {
    personality.addTrait("建筑师");
    // 说话风格更关注美学、结构
}

// 战士人格
if (playerProfile.getKillCount() > 100) {
    personality.addTrait("战士");
    // 说话风格更关注战斗、装备
}
```

**玩家自定义**:

```
玩家: "WorldSoul, 从今天起你要更严肃一点"
WorldSoul: "明白了！我会调整对话风格。
           如果你又觉得我太严肃，随时告诉我改回来。"
```

### 上下文理解

#### 游戏状态感知

WorldSoul能够感知并理解当前游戏状态：

**玩家状态**:
- 位置和维度
- 生命值、饥饿值
- 游戏模式
- 手持物品
- 背包内容

**世界状态**:
- 时间（白天/夜晚）
- 天气（晴天/雨天）
- 周围方块（10x10x10范围）
- 附近实体

**示例**:

```
玩家: "WorldSoul, 我快死了！"
[WorldSoul] [检测到生命值 3/20]
        别慌，朋友！我马上给你治疗！

        [执行] effect give @p regeneration 10 2
        [执行] effect give @p saturation 10 1

        你现在安全了。记得多带点食物！
```

#### 记忆系统

**短期记忆** (当前会话):
- 最近20条对话
- 临时偏好

**长期记忆** (跨会话):
- 重要事件（击败Boss、建造大型建筑）
- 玩家偏好（喜欢/不喜欢）
- 重要位置（基地、矿点）
- 学习到的Mod知识

**记忆检索**:

```
玩家: "我的基地在哪？"
[WorldSoul] [检索记忆...]
        你的主基地在 (123, 64, -456)，
        那个有大片西瓜农场的地方。

        需要我传送你回去吗？
```

### 命令提取与执行

#### 支持的命令类型

**简单命令**:
```java
/give <物品> [数量]
/tp <坐标>
/setblock <坐标> <方块>
/effect <效果> <秒数> [等级]
```

**复杂命令**:
```java
/fill <起点> <终点> <方块>
/summon <实体> <坐标> {NBT数据}
/execute @p ~ ~ ~ <命令链>
```

**自定义命令**:
```java
/redstone <类型> <位置>   // 构建红石电路
/terrain <操作> <范围>     // 修改地形
/farm <类型> <大小>        // 构建农场
```

#### 提取算法

```java
public class CommandExtractor {
    private static final Pattern COMMAND_PATTERN =
        Pattern.compile("/(\\w+)\\s+(.+)");

    public static List<MCCommand> extractCommands(String response) {
        List<MCCommand> commands = new ArrayList<>();
        Matcher matcher = COMMAND_PATTERN.matcher(response);

        while (matcher.find()) {
            String commandStr = matcher.group(0);
            MCCommand cmd = parseCommand(commandStr);
            if (cmd != null) {
                commands.add(cmd);
            }
        }

        return commands;
    }
}
```

---

## Mod集成与扩展

### Mod学习机制

#### 自动学习

**服务器启动时扫描**:
- 扫描所有已安装Mod的方块和物品
- 提取基础属性（名称、ID、材质）
- 推测功能类别（装饰、功能、机器等）

**运行时学习**:
- 监控玩家与Mod物品的交互
- 记录成功的使用方式
- 积累"经验池"

#### 人工驱动强化学习

**学习流程**:

```
玩家尝试使用Mod物品
    ↓
成功/失败？
    ↓
记录交互结果
    ↓
更新经验池
    ↓
识别使用模式
    ↓
生成Mod指南
```

**示例**:

```
玩家: "WorldSoul, 这个'热力炉'怎么用？"
[WorldSoul] [查询经验池...]
        我还没有太多经验，但我们一起试试！

        建议你右键放置它，然后放入矿石和燃料。
        我会记住它的用法。

[玩家尝试使用热力炉]
[WorldSoul] [记录交互成功]
        太好了！我学会了：
        - 热力炉需要燃料（煤炭、木炭）
        - 可以自动冶炼矿石
        - 比普通熔炉更快

        下次你就知道怎么用了！
```

### API接口设计

#### 为其他Mod提供集成接口

```java
/**
 * WorldSoul API
 * 其他Mod可以通过此接口深度集成
 */
public interface WorldSoulAPI {
    /**
     * 注册Mod配方
     */
    void registerRecipes(String modId, List<Recipe> recipes);

    /**
     * 注册自定义命令
     */
    void registerCustomCommand(String id, CustomCommandHandler handler);

    /**
     * 提供Mod知识
     */
    void provideModKnowledge(String modId, String knowledge);

    /**
     * 请求WorldSoul执行操作
     */
    CompletableFuture<Boolean> requestOperation(
        ServerPlayerEntity player,
        String operation,
        Map<String, Object> params
    );
}
```

**集成示例** (热力扩张Mod):

```java
public class ThermalExpansionIntegration implements WorldSoulAPI {
    @Override
    public void registerRecipes(String modId, List<Recipe> recipes) {
        // 注册热力扩张的所有配方
        WorldSoulAPI.getInstance().registerRecipes(
            "thermalexpansion",
            ThermalRecipes.getAll()
        );
    }

    @Override
    public void provideModKnowledge(String modId, String knowledge) {
        String guide = """
            热力扩张使用指南：

            1. 能量系统：使用RF（红石通量）
            2. 机器需要能量才能工作
            3. 能量可以通过红石传输
            """;

        WorldSoulAPI.getInstance().provideModKnowledge(
            "thermalexpansion",
            guide
        );
    }
}
```

### 配方指导系统

#### 智能配方提示

**场景**:

```
玩家: "WorldSoul, 怎么做动力框架？"
[WorldSoul] 动力框架配方：

        [配方]
        红石 | 铁 | 红石
        铁   | 机 | 铁
        红石 | 铁 | 红石

        你需要：
        - 4个红石
        - 4个铁锭
        - 1个机器框架

        你现在背包里有：
        ✅ 红石 x32
        ✅ 铁锭 x12
        ❌ 机器框架 x0

        需要我先帮你做机器框架吗？
```

---

## 性能与体验

### 异步处理

#### 线程池设计

```java
public class WorldSoulMod {
    // AI请求线程池（4线程）
    private final ExecutorService aiExecutor =
        Executors.newFixedThreadPool(4);

    // 命令执行线程池（2线程）
    private final ExecutorService commandExecutor =
        Executors.newFixedThreadPool(2);

    // 缓存清理线程池（1线程）
    private final ScheduledExecutorService cleanupExecutor =
        Executors.newScheduledThreadPool(1);
}
```

**任务分配**:
- AI请求: `aiExecutor` (4线程，支持并发)
- 命令执行: `commandExecutor` (2线程，顺序保证)
- 定时清理: `cleanupExecutor` (1线程，后台)

### 响应优化

#### 流式响应

**实现**:

```java
public void chatStream(
    List<ChatMessage> messages,
    Consumer<String> onChunk
) {
    StringBuilder fullResponse = new StringBuilder();

    aiExecutor.submit(() -> {
        // 调用流式API
        httpClient.send(request, BodyHandlers.ofLines())
            .body()
            .forEach(line -> {
                if (line.startsWith("data: ")) {
                    String chunk = parseChunk(line);
                    if (!chunk.isEmpty()) {
                        fullResponse.append(chunk);

                        // 回到主线程发送
                        server.execute(() -> {
                            onChunk.accept(chunk);
                        });
                    }
                }
            });
    });
}
```

**效果**:
- 逐字显示，降低延迟感
- 从5秒等待 → 0.5秒开始显示
- 更流畅的体验

### 缓存策略

#### 响应缓存

```java
public class ResponseCache {
    private final Cache<String, String> cache = Caffeine.newBuilder()
        .maximumSize(100)
        .expireAfterWrite(Duration.ofMinutes(10))
        .build();

    public String getCachedResponse(String prompt) {
        return cache.get(prompt, key -> {
            // 缓存未命中，调用AI
            return aiProvider.chat(prompt).join();
        });
    }
}
```

**缓存键生成**:
```
prompt内容 + 模式 + 玩家偏好哈希
```

### 限流保护

#### 请求限流

```java
public class RateLimiter {
    private final int maxRequests;
    private final Duration timeWindow;
    private final Queue<Instant> requestTimes = new ArrayDeque<>();

    public void acquire() throws RateLimitException {
        synchronized (this) {
            Instant now = Instant.now();

            // 清理过期记录
            while (!requestTimes.isEmpty() &&
                   requestTimes.peek().plus(timeWindow).isBefore(now)) {
                requestTimes.poll();
            }

            // 检查是否超限
            if (requestTimes.size() >= maxRequests) {
                throw new RateLimitException("请求过于频繁，请稍后再试");
            }

            requestTimes.add(now);
        }
    }
}
```

**默认配置**:
- 限制: 50次/分钟
- 超限后提示: "[WorldSoul] 冷却中，请5秒后再试"

---

## 开发路线图

### Phase 1: MVP基础 (2-3周)

**目标**: 基本的对话功能

**任务**:
- [x] 技术栈确认 (Fabric 1.20.4)
- [ ] 项目骨架搭建
- [ ] 配置系统实现
- [ ] AI通信模块 (DeepSeekProvider)
- [ ] 对话管理 (ConversationManager)
- [ ] 聊天监听 (ChatListener)
- [ ] 配置UI (ConfigScreen)
- [ ] 单一模式 (向导模式)

**验证标准**:
- 游戏内输入"WorldSoul, 你好"得到回复
- 可配置API Key并保存
- 能回答基础MC知识问题

### Phase 2: 基础能力 (3-4周)

**目标**: 执行命令，三种模式

**任务**:
- [ ] WorldController (give/setblock/tp/effect)
- [ ] CommandExtractor
- [ ] 三种模式实现
- [ ] GodModeManager
- [ ] MemoryManager
- [ ] 等价交换系统
- [ ] 游戏状态上下文

**验证标准**:
- 能给予物品、设置方块
- 三种模式可切换，语气不同
- 记住玩家偏好

### Phase 3: 高级能力 (4-5周)

**目标**: Mod学习，红石构建

**任务**:
- [ ] ModBlockRegistry
- [ ] 方块属性分析
- [ ] RedstoneBuilder
- [ ] 红石电路模板
- [ ] ReinforcementLearner
- [ ] 批量命令执行
- [ ] HUD显示

**验证标准**:
- 自动学习Mod方块
- 构建基础红石电路
- 批量执行不卡顿

### Phase 4: 完整功能 (3-4周)

**目标**: 语音，地形，优化

**任务**:
- [ ] 语音输入系统
- [ ] TerrainModifier
- [ ] 高级红石电路
- [ ] 性能优化
- [ ] 缓存和限流
- [ ] 完善文档

**验证标准**:
- 语音对话可用
- 地形修改功能
- 多玩家并发流畅

---

## 风险评估与缓解

### 技术风险

| 风险 | 影响 | 概率 | 缓解措施 |
|-----|------|------|---------|
| AI API不稳定 | 高 | 中 | 内置规则引擎降级 |
| 异步处理复杂 | 中 | 高 | 使用成熟的并发框架 |
| Token成本过高 | 中 | 中 | 预算警告+缓存 |
| Mod兼容性 | 低 | 高 | 学习机制+API |

### 设计风险

| 风险 | 影响 | 概率 | 缓解措施 |
|-----|------|------|---------|
| 平衡性问题 | 高 | 中 | 等价交换+冷却 |
| UI体验不佳 | 中 | 低 | 迭代测试 |
| 人格设定模糊 | 低 | 低 | 演化机制 |

### 用户体验风险

| 风险 | 影响 | 概率 | 缓解措施 |
|-----|------|------|---------|
| 配置复杂 | 中 | 中 | 默认配置+引导 |
| 响应慢 | 高 | 中 | 流式响应+缓存 |
| 误触发 | 低 | 低 | 明确前缀 |

---

## 总结

WorldSoul Mod是一个雄心勃勃的项目，将AI Agent深度集成到Minecraft游戏中，为每个玩家提供专属的智能伙伴。

**核心创新点**:
1. 业内首个游戏内集成的AI伙伴Mod
2. 动态人格演化系统
3. 人工驱动的Mod学习
4. API Token经济模型

**成功关键**:
- 优秀的AI对话体验
- 无缝的游戏集成
- 公衡的能力限制
- 持续的Mod学习

**长期愿景**:
让每个Minecraft玩家都有一个"懂他"的AI伙伴，降低游戏和Mod的学习门槛，让创造力得到充分释放。

---

---

## 性能基准与优化

### 性能目标

#### 响应时间

| 操作 | 目标响应时间 | 最大可接受 | 测量方法 |
|-----|------------|-----------|---------|
| 简单对话 | < 2秒 | < 5秒 | 从发送到收到首字符 |
| 物品给予 | < 1秒 | < 3秒 | 从请求到执行完成 |
| 红石构建 | < 5秒 | < 10秒 | 从请求到所有方块放置 |
| 地形修改 | < 10秒 | < 30秒 | 从请求到地形变更完成 |
| Mod学习 | < 30秒 | < 60秒 | 服务器启动时扫描 |

#### 资源占用

| 资源 | 空闲占用 | 峰值占用 | 监控方法 |
|-----|---------|---------|---------|
| CPU | < 1% | < 5% | 每tick采样 |
| 内存 | < 100MB | < 500MB | JVM堆监控 |
| 网络 | 0 KB/s | < 50 KB/s | 流量统计 |
| 线程 | 2个 | 10个 | 线程池监控 |

### 性能优化策略

#### 1. 异步处理架构

**问题**: AI API调用耗时2-5秒，不能阻塞游戏主线程

**解决方案**:

```java
public class AsyncExecutor {
    // AI请求专用线程池（4线程）
    private final ExecutorService aiPool = Executors.newFixedThreadPool(4);

    // 命令执行线程池（2线程）
    private final ExecutorService cmdPool = Executors.newFixedThreadPool(2);

    public <T> CompletableFuture<T> submitAI(
        Callable<T> task,
        Consumer<T> onSuccess,
        Consumer<Exception> onError
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                onError.accept(e);
                throw new CompletionException(e);
            }
        }, aiPool).thenAcceptAsync(onSuccess, cmdPool);
    }
}
```

**效果**:
- 游戏FPS不受影响
- AI请求在后台处理
- 主线程仅负责UI更新

#### 2. 智能缓存系统

**三级缓存策略**:

```java
public class TripleCache {
    // L1: 内存缓存（最快，100条）
    private final Cache<String, Response> l1 = Caffeine.newBuilder()
        .maximumSize(100)
        .expireAfterWrite(Duration.ofMinutes(5))
        .build();

    // L2: 磁盘缓存（中等，1000条）
    private final DiskCache l2 = new DiskCache(
        Paths.get("worldsoul_cache"),
        1000
    );

    // L3: AI API（最慢，无限制）
    private final AIProvider l3 = new DeepSeekProvider(apiKey);

    public String get(String prompt) {
        // 先查L1
        Response r1 = l1.getIfPresent(prompt);
        if (r1 != null) return r1.content;

        // 再查L2
        Response r2 = l2.get(prompt);
        if (r2 != null) {
            l1.put(prompt, r2);
            return r2.content;
        }

        // 最后调用L3
        String response = l3.chat(prompt).join();
        Response r3 = new Response(response, Instant.now());

        // 回填L1和L2
        l1.put(prompt, r3);
        l2.put(prompt, r3);

        return response;
    }
}
```

**缓存命中率目标**:
- L1命中率: > 60%
- L2命中率: > 30%
- L3调用率: < 10%

**效果**:
- 相同问题响应时间从2秒降至0.01秒
- 降低API调用成本

#### 3. 批量操作优化

**问题**: 大量命令执行时，逐条执行很慢

**解决方案**:

```java
public class BatchOptimizer {
    public void executeBatch(
        ServerWorld world,
        List<BlockPlacement> placements
    ) {
        // 按区域分组
        Map<ChunkPos, List<BlockPlacement>> grouped =
            placements.stream()
                .collect(Collectors.groupingBy(
                    p -> new ChunkPos(p.pos)
                ));

        // 对每个区块批量更新
        grouped.forEach((chunkPos, chunkPlacements) -> {
            // 标记区块为脏
            world.markChunkDirty(chunkPos);

            // 批量放置方块
            chunkPlacements.forEach(p -> {
                world.setBlockState(p.pos, p.state, 0); // 不立即更新
            });

            // 一次性更新区块
            world.scheduleChunkRend(chunkPos.x, chunkPos.z);
        });
    }
}
```

**效果**:
- 1000个方块放置从30秒降至5秒
- 减少区块更新次数

#### 4. 流式响应

**实现**:

```java
public void streamResponse(
    String prompt,
    Consumer<String> onChunk
) {
    aiExecutor.submit(() -> {
        StringBuilder full = new StringBuilder();

        // 调用流式API
        aiProvider.chatStream(prompt, chunk -> {
            full.append(chunk);

            // 异步发送到主线程
            server.execute(() -> {
                onChunk.accept(chunk);
            });
        });

        // 完成后保存到历史
        saveToHistory(full.toString());
    });
}
```

**效果**:
- 首字符响应时间从2秒降至0.5秒
- 玩家感知延迟降低75%

### 性能监控

#### Metrics采集

```java
public class PerformanceMetrics {
    private final MeterRegistry registry;

    public void recordResponse(String operation, long durationMs) {
        registry.timer("worldsoul.response")
            .tag("operation", operation)
            .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordCacheHit(String level) {
        registry.counter("worldsoul.cache.hit")
            .tag("level", level)
            .increment();
    }

    public void recordAPICall(String provider, int tokens) {
        registry.counter("worldsoul.api.tokens")
            .tag("provider", provider)
            .increment(tokens);
    }
}
```

#### 监控指标

| 指标 | 阈值 | 告警动作 |
|-----|------|---------|
| 平均响应时间 | > 3秒 | 增加缓存 |
| CPU占用 | > 5% | 减少非关键任务 |
| 内存占用 | > 400MB | 触发GC |
| API失败率 | > 10% | 切换降级模式 |
| 缓存命中率 | < 40% | 扩大缓存容量 |

### 性能基准测试

#### 测试场景

**场景1: 单人对话**
- 玩家数量: 1
- 操作: 连续问答50次
- 目标: 无卡顿，平均响应< 2秒

**场景2: 多人并发**
- 玩家数量: 10
- 操作: 同时请求物品给予
- 目标: 所有玩家在5秒内完成

**场景3: 批量建造**
- 操作: 构建100x10平台（1000个方块）
- 目标: 10秒内完成，不影响FPS

**场景4: Mod学习**
- 操作: 加载10个大型Mod
- 目标: 服务器启动时间增加< 30秒

---

## 安全与隐私

### 数据安全

#### API Key保护

**存储**:
```json
{
  "apiKey": "sk-xxxxxxxxxxxx",
  "encrypted": true,
  "salt": "随机盐值"
}
```

**加密方案**:
- 使用AES-256加密
- 基于机器ID生成密钥
- 配置文件权限设置为600（仅所有者可读写）

**实现**:

```java
public class SecureConfig {
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    public void saveAPIKey(String apiKey) {
        try {
            // 生成密钥（基于机器ID）
            SecretKey key = generateKeyFromMachineId();

            // 加密
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(apiKey.getBytes());

            // 保存
            config.setEncryptedKey(Base64.getEncoder().encodeToString(encrypted));
            config.save();

        } catch (Exception e) {
            WorldSoul.LOGGER.error("Failed to encrypt API key", e);
        }
    }

    public String loadAPIKey() {
        try {
            SecretKey key = generateKeyFromMachineId();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] encrypted = Base64.getDecoder()
                .decode(config.getEncryptedKey());
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted);

        } catch (Exception e) {
            WorldSoul.LOGGER.error("Failed to decrypt API key", e);
            return null;
        }
    }
}
```

#### 日志脱敏

**敏感信息过滤**:

```java
public class SecureLogger {
    private static final Pattern API_KEY_PATTERN =
        Pattern.compile("sk-[a-zA-Z0-9]{48}");

    public static String sanitize(String message) {
        // 移除API Key
        String sanitized = API_KEY_PATTERN
            .matcher(message)
            .replaceAll("sk-****");

        // 移除玩家IP（可选）
        sanitized = sanitized.replaceAll(
            "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}",
            "***.***.***.***"
        );

        return sanitized;
    }
}
```

### 隐私保护

#### 数据收集原则

**最小化收集**:
- ✅ 收集: 游戏交互数据（用于Mod学习）
- ✅ 收集: 玩家偏好（提升体验）
- ❌ 不收集: 聊天记录内容
- ❌ 不收集: 现实身份信息
- ❌ 不收集: 地理位置

**数据本地化**:
- 所有数据存储在本地服务器
- 不上传到任何远程服务器
- 玩家可随时导出或删除

#### 对话记录处理

**选项1: 不记录（推荐）**

```json
{
  "chatLogging": {
    "enabled": false,
    "reason": "保护玩家隐私"
  }
}
```

**选项2: 仅本地记录**

```java
public class ChatLogger {
    public void logMessage(UUID playerId, String message) {
        if (!config.isChatLoggingEnabled()) {
            return; // 默认不记录
        }

        // 仅记录元数据，不记录内容
        ChatLogEntry entry = new ChatLogEntry(
            playerId,
            Instant.now(),
            message.length(),  // 仅记录长度
            message.hashCode() // 用于去重
        );

        // 保存到本地（加密）
        localDatabase.save(entry);
    }
}
```

#### GDPR合规

**玩家权利**:
1. **知情权**: 清楚的隐私政策说明
2. **访问权**: 可查看所有存储的个人数据
3. **更正权**: 可更正不准确的数据
4. **删除权**: 可请求删除所有个人数据
5. **可携带权**: 可导出所有数据

**实现命令**:

```
/ws data export     # 导出个人数据
/ws data delete     # 删除个人数据
/ws data view       # 查看存储的数据摘要
```

### 权限控制

#### 服务器管理员权限

```java
public class PermissionManager {
    public boolean canAccessConfig(ServerPlayerEntity player) {
        return player.hasPermissionLevel(4); // 仅管理员
    }

    public boolean canViewOthersData(ServerPlayerEntity player) {
        return player.hasPermissionLevel(4); // 仅管理员
    }

    public boolean canModifyGlobalSettings(ServerPlayerEntity player) {
        return player.hasPermissionLevel(4); // 仅管理员
    }
}
```

#### 操作审计

```java
public class AuditLogger {
    public void logSensitiveAction(
        ServerPlayerEntity player,
        String action,
        Map<String, Object> params
    ) {
        AuditEntry entry = new AuditEntry(
            player.getUuid(),
            player.getName().getString(),
            action,
            params,
            Instant.now()
        );

        // 记录到audit.log
        auditLog.append(entry);

        // 如果是敏感操作，通知其他管理员
        if (isSensitiveAction(action)) {
            notifyAdmins(player.getName().getString() +
                " 执行了敏感操作: " + action);
        }
    }
}
```

**敏感操作**:
- 修改API Key
- 查看他人数据
- 修改全局配置
- 删除数据

### 安全最佳实践

#### 1. 输入验证

```java
public class InputValidator {
    public static final int MAX_MESSAGE_LENGTH = 1000;

    public String validate(String input) {
        // 长度限制
        if (input.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("消息过长");
        }

        // 移除控制字符
        String sanitized = input.replaceAll("[\\p{Cntrl}&&[\\r\\n\\t]]", "");

        // 防止命令注入（双重检查）
        if (containsSuspiciousCommands(sanitized)) {
            WorldSoul.LOGGER.warn("Suspicious command detected: " +
                sanitized.substring(0, Math.min(50, sanitized.length())));
            throw new SecurityException("命令被拦截");
        }

        return sanitized;
    }
}
```

#### 2. 速率限制

```java
public class RateLimiter {
    private final Map<UUID, RateLimitEntry> limits = new ConcurrentHashMap<>();

    public boolean checkLimit(ServerPlayerEntity player) {
        RateLimitEntry entry = limits.computeIfAbsent(
            player.getUuid(),
            uuid -> new RateLimitEntry()
        );

        Instant now = Instant.now();

        // 清理过期记录
        entry.cleanup(now);

        // 检查限制
        if (entry.getCount() >= MAX_REQUESTS_PER_MINUTE) {
            return false;
        }

        entry.addRequest(now);
        return true;
    }
}
```

#### 3. HTTPS/TLS

**AI API通信**:
- 强制使用HTTPS
- 验证SSL证书
- 禁用不安全的协议

```java
HttpClient client = HttpClient.newBuilder()
    .version(HttpClient.Version.HTTP_2)
    .connectTimeout(Duration.ofSeconds(30))
    .sslContext(SSLContext.getDefault())  // 使用默认安全配置
    .build();
```

---

## 部署与发布

### 构建流程

#### Gradle配置

```gradle
plugins {
    id 'fabric-loom' version '1.5-SNAPSHOT'
    id 'maven-publish'
}

dependencies {
    minecraft "com.mojang:minecraft:1.20.4"
    mappings "net.fabricmc:yarn:1.20.4+build.1:v2"
    modImplementation "net.fabricmc:fabric-loader:0.15.3"
    modImplementation "net.fabricmc.fabric-api:fabric-api:0.91.2+1.20.4"

    // 依赖
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'com.github.ben-manes.caffeine:caffeine:3.1.8'

    // 测试
    testImplementation 'junit:junit:4.13.2'
}

processResources {
    inputs.property "version", project.version

    filesMatching("fabric.mod.json") {
        expand "version": project.version
    }
}

tasks.withType(JavaCompile).configureEach {
    it.options.release = 17
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
```

#### 版本管理

**语义化版本**: `v主版本.次版本.修订版`

示例:
- `v1.0.0` - 首次发布
- `v1.1.0` - 新增语音功能
- `v1.1.1` - 修复bug
- `v2.0.0` - 重大架构变更

### 发布渠道

#### Mod发布平台

| 平台 | 优先级 | 受众 | 更新频率 |
|-----|-------|------|---------|
| **Modrinth** | P0 (必须) | 国际玩家 | 每个版本 |
| **CurseForge** | P1 (重要) | 国际玩家 | 每个版本 |
| **GitHub Releases** | P1 (重要) | 开发者 | 每个版本 |
| **MCBBS** | P2 (可选) | 中文玩家 | 主要版本 |

#### 发布清单

**每个版本必须包含**:
- [ ] 编译后的JAR文件
- [ ] fabric.mod.json（包含版本号）
- [ ] CHANGELOG.md（更新日志）
- [ ] README.md（使用说明）
- [ ] LICENSE（许可证）

**每个主要版本额外包含**:
- [ ] 配置文件示例
- [ ] 迁移指南（如果有破坏性变更）
- [ ] 截图或演示视频

### 自动化发布

#### GitHub Actions配置

```yaml
name: Release WorldSoul Mod

on:
  push:
    tags:
      - 'v*'

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build with Gradle
        run: ./gradlew build

      - name: Create Release
        id: create_release
        uses: actions/create-release@v1
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        with:
          tag_name: ${{ github.ref }}
          release_name: Release ${{ github.ref }}
          draft: false
          prerelease: false

      - name: Upload Release Asset
        uses: actions/upload-release-asset@v1
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        with:
          upload_url: ${{ steps.create_release.outputs.upload_url }}
          asset_path: ./build/libs/worldsoul-${{ github.ref }}.jar
          asset_name: worldsoul-${{ github.ref }}.jar
          asset_content_type: application/java-archive
```

### 版本兼容性

#### Minecraft版本支持

| Mod版本 | MC版本 | 状态 |
|---------|--------|------|
| 1.0.x | 1.20.4 | ✅ 稳定 |
| 1.1.x | 1.20.4 + 1.20.6 | ✅ 稳定 |
| 1.2.x | 1.20.x + 1.21 | 🚧 开发中 |

#### 向后兼容策略

**配置文件兼容**:

```java
public class ConfigMigrator {
    public void migrate(Config_v1_0 oldConfig) {
        Config_v1_1 newConfig = new Config_v1_1();

        // 迁移旧字段
        newConfig.setApiKey(oldConfig.getApiKey());
        newConfig.setAiProvider(oldConfig.getAiProvider());

        // 设置新字段默认值
        newConfig.setVoiceInputEnabled(false);
        newConfig.setDailyBudget(100000);

        // 保存新配置
        newConfig.save();

        // 备份旧配置
        oldConfig.renameTo("config_v1.0.json.bak");
    }
}
```

### 更新策略

#### 自动更新检查

```java
public class UpdateChecker {
    private static final String UPDATE_URL =
        "https://api.modrinth.com/v2/project/worldsoul/version";

    public void checkForUpdates() {
        if (!config.isAutoUpdateEnabled()) {
            return;
        }

        CompletableFuture.supplyAsync(() -> {
            try {
                // 获取最新版本
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(UPDATE_URL))
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

                // 解析版本
                JSONArray versions = new JSONArray(response.body());
                String latestVersion = versions.getJSONObject(0)
                    .getString("version_number");

                // 比较版本
                if (isNewerVersion(latestVersion)) {
                    notifyUpdateAvailable(latestVersion);
                }

            } catch (Exception e) {
                WorldSoul.LOGGER.error("Update check failed", e);
            }
        }, updateExecutor);
    }

    private boolean isNewerVersion(String latest) {
        String current = WorldSoulMod.VERSION;
        return compareVersions(latest, current) > 0;
    }

    private void notifyUpdateAvailable(String latest) {
        // 通知管理员
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.hasPermissionLevel(4)) {
                player.sendMessage(Text.literal(
                    "§6[WorldSoul] §e新版本可用: " + latest +
                    " (当前: " + WorldSoulMod.VERSION + ")"
                ));
                player.sendMessage(Text.literal(
                    "§7下载地址: https://modrinth.com/mod/worldsoul"
                ).formatted(Formatting.GRAY));
            }
        }
    }
}
```

---

## 测试与质量保证

### 测试策略

#### 测试金字塔

```
        /\
       /  \      E2E测试 (5%)
      /────\
     /      \    集成测试 (25%)
    /────────\
   /          \  单元测试 (70%)
  /____________\
```

### 单元测试

#### 覆盖目标

| 模块 | 目标覆盖率 | 优先级 |
|-----|-----------|-------|
| AI通信层 | > 80% | P0 |
| 对话管理 | > 70% | P0 |
| 记忆系统 | > 70% | P1 |
| 命令执行 | > 80% | P0 |
| Mod学习 | > 60% | P1 |
| UI系统 | > 50% | P2 |

#### 测试示例

```java
public class CommandExtractorTest {

    @Test
    public void testExtractGiveCommand() {
        String response = "好的，我给你钻石剑。/give @p diamond_sword";
        List<MCCommand> commands = CommandExtractor.extractCommands(response);

        assertEquals(1, commands.size());
        assertEquals(CommandType.GIVE_ITEM, commands.get(0).getType());
        assertEquals("diamond_sword", commands.get(0).getItem());
    }

    @Test
    public void testExtractMultipleCommands() {
        String response = """
            先给你装备，再传送到高空。
            /give @p diamond_sword
            /tp @p ~ ~100 ~
            """;

        List<MCCommand> commands = CommandExtractor.extractCommands(response);

        assertEquals(2, commands.size());
        assertEquals(CommandType.GIVE_ITEM, commands.get(0).getType());
        assertEquals(CommandType.TELEPORT, commands.get(1).getType());
    }

    @Test
    public void testExtractNoCommands() {
        String response = "钻石剑是这样做的...";
        List<MCCommand> commands = CommandExtractor.extractCommands(response);

        assertTrue(commands.isEmpty());
    }
}
```

### 集成测试

#### Minecraft测试服务器

```java
@Test
public void testWorldSoulIntegration() {
    // 启动测试服务器
    MinecraftServer server = MinecraftServer.start(
        new TestServerConfig()
    );

    try {
        // 模拟玩家加入
        ServerPlayerEntity player = server.addPlayer("TestPlayer");

        // 发送消息
        player.chat("WorldSoul, 你好");

        // 等待响应
        await().atMost(5, TimeUnit.SECONDS)
            .until(() -> player.hasReceivedMessageFrom("WorldSoul"));

        // 验证响应
        String response = player.getLastMessageFrom("WorldSoul");
        assertTrue(response.contains("你好"));

    } finally {
        server.stop();
    }
}
```

### 性能测试

#### 基准测试

```java
public class PerformanceBenchmark {

    @Test
    public void benchmarkAIResponse() {
        AIProvider provider = new DeepSeekProvider(testApiKey);

        List<ChatMessage> messages = List.of(
            new ChatMessage("user", "你好")
        );

        long startTime = System.nanoTime();
        String response = provider.chat(messages).join();
        long duration = System.nanoTime() - startTime;

        long durationMs = duration / 1_000_000;

        // 断言响应时间 < 5秒
        assertTrue(durationMs < 5000,
            "响应时间 " + durationMs + "ms 超过5秒");

        // 记录性能
        Metrics.recordResponse("simple_chat", durationMs);
    }

    @Test
    public void benchmarkConcurrentRequests() {
        AIProvider provider = new DeepSeekProvider(testApiKey);

        int concurrentUsers = 10;

        long startTime = System.nanoTime();

        List<CompletableFuture<String>> futures = new ArrayList<>();
        for (int i = 0; i < concurrentUsers; i++) {
            CompletableFuture<String> future = provider.chat(
                List.of(new ChatMessage("user", "测试" + i))
            );
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .join();

        long duration = System.nanoTime() - startTime;
        long durationMs = duration / 1_000_000;

        // 平均每个请求 < 10秒
        long avgPerRequest = durationMs / concurrentUsers;
        assertTrue(avgPerRequest < 10000,
            "平均响应时间 " + avgPerRequest + "ms 超过10秒");
    }
}
```

### 用户验收测试 (UAT)

#### Beta测试计划

**测试版本**: v1.0.0-beta.1
**测试周期**: 2周
**测试者**: 20名社区志愿者

**测试任务**:

| 任务 | 目标 | 成功标准 |
|-----|------|---------|
| 基础对话 | 能与WorldSoul正常对话 | 100%成功率 |
| 物品给予 | 请求物品能正确给予 | 100%成功率 |
| 模式切换 | 三种模式可正常切换 | 无bug |
| 语音输入 | 语音能正确识别 | > 90%准确率 |
| Mod学习 | 自动识别Mod方块 | > 80%准确率 |

**反馈收集**:

```java
public class FeedbackCollector {
    public void collectFeedback(
        ServerPlayerEntity player,
        FeedbackType type,
        String message
    ) {
        FeedbackEntry entry = new FeedbackEntry(
            player.getUuid(),
            type,
            message,
            WorldSoulMod.VERSION,
            Instant.now()
        );

        // 保存到反馈数据库
        feedbackDatabase.save(entry);

        // 感谢玩家
        player.sendMessage(Text.literal(
            "§a[WorldSoul] 感谢你的反馈！我们会持续改进。"
        ));
    }

    public enum FeedbackType {
        BUG,          // Bug报告
        SUGGESTION,   // 功能建议
        USABILITY,    // 易用性问题
        PERFORMANCE   // 性能问题
    }
}
```

### 持续集成

#### CI/CD流程

```
代码提交
    ↓
触发CI
    ↓
┌─────────────┐
│  运行单元测试  │
└─────────────┘
    ↓ (通过)
┌─────────────┐
│  代码检查     │
│  (Checkstyle)│
└─────────────┘
    ↓ (通过)
┌─────────────┐
│  构建JAR     │
└─────────────┘
    ↓
┌─────────────┐
│  集成测试     │
│  (测试服务器) │
└─────────────┘
    ↓ (通过)
┌─────────────┐
│  生成Release │
│  (打Tag)    │
└─────────────┘
```

#### 质量门禁

**阻止发布的条件**:
- ❌ 单元测试覆盖率 < 70%
- ❌ 有关键Bug未修复
- ❌ 性能基准未达标
- ❌ 安全扫描发现高危漏洞

**可以发布的条件**:
- ✅ 所有单元测试通过
- ✅ 集成测试通过
- ✅ 无已知关键Bug
- ✅ 文档完整

### 质量度量

#### Bug跟踪

| 严重级别 | 响应时间 | 修复时间 | 示例 |
|---------|---------|---------|------|
| **Critical** | 4小时 | 24小时 | 游戏崩溃 |
| **High** | 1天 | 3天 | 主要功能不可用 |
| **Medium** | 3天 | 1周 | 次要功能问题 |
| **Low** | 1周 | 2周 | UI问题 |

#### 代码审查清单

**审查项**:
- [ ] 代码符合风格指南
- [ ] 有充分的单元测试
- [ ] 文档已更新
- [ ] 性能影响可接受
- [ ] 无安全漏洞
- [ ] 错误处理完善
- [ ] 日志记录适当

---

**文档版本**: 1.1
**最后更新**: 2026-02-17
**作者**: Claude + 用户协作

**新增章节**:
- 12. 性能基准与优化
- 13. 安全与隐私
- 14. 部署与发布
- 15. 测试与质量保证
