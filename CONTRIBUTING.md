# 贡献指南

感谢你有兴趣为WorldSoul Mod做出贡献！🎉

---

## 🤝 如何贡献

我们欢迎所有形式的贡献，无论是代码、文档、Bug报告还是功能建议。

### 报告Bug

如果你发现了Bug，请：

1. **检查是否已报告** - 在[Issues](https://github.com/gccszs/WorldSoul/issues)中搜索
2. **创建新Issue** - 使用Bug报告模板
3. **提供详细信息**:
   - Minecraft版本
   - WorldSoul版本
   - 复现步骤
   - 预期行为 vs 实际行为
   - 日志/截图

**Bug报告模板**:

```markdown
### Bug描述
简要描述Bug是什么

### 复现步骤
1. 进入游戏
2. 输入 '...'
3. ...

### 预期行为
应该发生什么

### 实际行为
实际发生了什么

### 环境
- Minecraft版本: 1.20.4
- WorldSoul版本: v1.0.0
- Java版本: 17
- 其他Mod: ...
```

### 提交功能建议

我们欢迎新功能建议！在提交前请考虑：

- ✅ 功能是否符合WorldSoul的核心定位
- ✅ 是否对大多数玩家有价值
- ✅ 实现复杂度是否合理

**功能建议模板**:

```markdown
### 功能描述
简要描述你希望添加的功能

### 使用场景
在什么情况下会使用这个功能
示例：当玩家...

### 解决的问题
这个功能解决了什么痛点

### 实现思路（可选）
如果你有技术实现的想法，请分享
```

---

## 💻 代码贡献

### 开发环境设置

#### 前置要求

- **JDK 17** 或更高版本
- **Minecraft 1.20.4**
- **Fabric Loader 0.15+**
- **IDE**: IntelliJ IDEA（推荐）或 Eclipse

#### 克隆仓库

```bash
git clone https://github.com/gccszs/WorldSoul.git
cd WorldSoul
```

#### 构建项目

```bash
# Linux/macOS
./gradlew build

# Windows
gradlew.bat build
```

#### 在IDE中打开

**IntelliJ IDEA**:
1. 打开IDEA
2. 选择 `File` → `Open`
3. 选择WorldSoul项目根目录
4. IDEA会自动识别Gradle项目

### 代码风格

我们遵循以下代码风格：

#### Java代码规范

- 使用 **4空格缩进**
- 类名使用 **PascalCase**
- 方法名和变量名使用 **camelCase**
- 常量名使用 **UPPER_SNAKE_CASE**

**示例**:

```java
public class ConversationManager {
    private static final int MAX_HISTORY_SIZE = 20;

    private final AIProvider aiProvider;
    private ConversationSession currentSession;

    public void processMessage(String message) {
        // 实现
    }
}
```

#### 注释规范

- 公共API必须有Javadoc注释
- 复杂逻辑需要行内注释
- TODO标记待办事项

**示例**:

```java
/**
 * 处理玩家的聊天消息
 *
 * @param player 发送消息的玩家
 * @param message 消息内容
 * @return CompletableFuture，完成时表示处理完毕
 */
public CompletableFuture<Void> processPlayerMessage(
    ServerPlayerEntity player,
    String message
) {
    // TODO: 添加消息验证
    // ...
}
```

### 提交规范

#### Commit Message格式

我们使用[约定式提交](https://www.conventionalcommits.org/)格式：

```
<类型>(<范围>): <简短描述>

<详细描述>

<页脚>
```

**类型**:
- `feat`: 新功能
- `fix`: Bug修复
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具相关

**示例**:

```bash
git commit -m "feat(ai): 添加流式响应支持

实现了AI响应的流式处理，玩家可以实时看到
AI回复的逐字输出，降低感知延迟。

Closes #123"
```

#### Pull Request流程

1. **Fork仓库** - 点击GitHub页面右上角的Fork按钮
2. **创建分支** - 从main分支创建你的功能分支
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **进行更改** - 编写代码、测试、更新文档
4. **提交更改** - 使用清晰的commit message
   ```bash
   git add .
   git commit -m "feat: 添加某某功能"
   ```
5. **推送到Fork** - 推送到你的远程仓库
   ```bash
   git push origin feature/your-feature-name
   ```
6. **创建PR** - 在GitHub上创建Pull Request
7. **等待审核** - 维护者会review你的代码并提供反馈

#### PR模板

创建PR时请填写以下信息：

```markdown
## 变更类型
- [ ] Bug修复
- [ ] 新功能
- [ ] 代码重构
- [ ] 文档更新
- [ ] 性能优化

## 描述
简要描述这个PR做了什么

## 相关Issue
关闭 #issue_number

## 测试
描述你如何测试这些更改
- [ ] 单元测试通过
- [ ] 集成测试通过
- [ ] 手动测试

## 截图（如适用）
添加截图或GIF展示新功能

## 检查清单
- [ ] 代码符合项目风格指南
- [ ] 已添加必要的文档
- [ ] 已添加/更新测试
- [ ] 所有测试通过
```

---

## 📝 文档贡献

### 改进文档

文档非常重要！你可以帮助：

- 📖 修正错别字和语法错误
- 📚 添加使用示例
- 🌐 翻译文档到其他语言
- 📸 添加截图和演示视频
- 🎨 改进文档的可读性

### 文档风格

- 使用**清晰简洁**的语言
- 提供**具体示例**
- 添加**代码块**和**截图**
- 保持**更新**与代码同步

---

## 🧪 测试贡献

### 编写测试

我们欢迎测试贡献：

- **单元测试**: 测试单个类/方法
- **集成测试**: 测试多个组件协作
- **E2E测试**: 测试完整用户流程

**测试示例**:

```java
public class CommandExtractorTest {

    @Test
    public void testExtractGiveCommand() {
        String response = "好的，我给你钻石剑。/give @p diamond_sword";
        List<MCCommand> commands = CommandExtractor.extractCommands(response);

        assertEquals(1, commands.size());
        assertEquals(CommandType.GIVE_ITEM, commands.get(0).getType());
    }
}
```

---

## 🌍 国际化

### 翻译

WorldSoul支持多语言，你可以：

1. 查看现有语言文件: `src/main/resources/assets/worldsoul/lang/`
2. 创建新语言文件: `xx_xx.json`（例如：`fr_fr.json`）
3. 翻译所有字符串
4. 提交PR

---

## 🎨 设计贡献

### UI/UX改进

如果你有设计技能，可以帮助：

- 改进配置界面设计
- 优化HUD布局
- 设计图标和纹理
- 创建演示视频

---

## 📜 许可证

通过贡献代码，你同意你的贡献将在与项目相同的[MIT许可证](LICENSE)下发布。

---

## 🙏 致谢

感谢所有贡献者！你的名字会出现在[贡献者列表](CONTRIBUTORS.md)中。

---

## 💬 社区

- 💬 [Discord](https://discord.gg/worldsoul)
- 🐦 [Twitter](https://twitter.com/WorldSoulMod)
- 📧 Email: worldsoul@example.com

---

**有问题？** 请在[Discussions](https://github.com/gccszs/WorldSoul/discussions)中提问，我们会尽快回复！

Happy Coding! 🚀
