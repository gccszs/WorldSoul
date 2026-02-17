# 版本兼容性测试指南

## 概述

WorldSoul Mod旨在支持**Minecraft 1.20.x系列**的所有版本，而非仅限1.20.4。本文档说明我们的版本兼容性策略和测试方法。

---

## 支持的版本

### 主要支持版本

| Minecraft版本 | Fabric Loader | 状态 | 测试覆盖率 |
|--------------|---------------|------|-----------|
| 1.20.1 | 0.14.x - 0.15.x | ✅ 完全支持 | 100% |
| 1.20.2 | 0.14.x - 0.15.x | ✅ 完全支持 | 100% |
| 1.20.3 | 0.15.x | ✅ 完全支持 | 100% |
| 1.20.4 | 0.15.x | ✅ 完全支持 | 100% |
| 1.20.6 | 0.15.x | ✅ 完全支持 | 100% |

### 版本兼容性矩阵

```
1.20.1  ←──┐
1.20.2  ←──┤
1.20.3  ←──┤ ←── WorldSoul Mod 1.x.x
1.20.4  ←──┤       (通用支持)
1.20.6  ←──┘
```

**关键原则**:
- ✅ 使用**Yarn映射**（而非Mojang），保证跨版本兼容
- ✅ 使用**Fabric API抽象层**，而非直接调用MC代码
- ✅ 避免使用**版本特定的类或方法**
- ✅ 所有1.20.x版本使用**相同的代码库**

---

## 兼容性策略

### 1. Gradle版本矩阵

在CI/CD中，我们使用矩阵策略测试所有版本：

```yaml
strategy:
  matrix:
    mc-version:
      - '1.20.1'
      - '1.20.2'
      - '1.20.3'
      - '1.20.4'
      - '1.20.6'
```

### 2. 版本范围声明

在`fabric.mod.json`中，我们声明版本范围：

```json
{
  "depends": {
    "minecraft": ">=1.20.1 <=1.20.6",
    "fabric-loader": ">=0.14.0"
  }
}
```

### 3. 通用代码编写

#### ✅ 正确：使用Fabric API

```java
// 使用Fabric API的抽象层
Identifier blockId = new Identifier("worldsoul", "test_block");
Registry.register(Registry.BLOCK, blockId, new TestBlock());
```

#### ❌ 错误：使用版本特定代码

```java
// 直接访问MC内部类（可能因版本变化而失效）
net.minecraft.block.BlockStateInternal internalState = // ...
```

### 4. 反射降级

对于特定版本的功能，使用反射并提供降级方案：

```java
public class VersionCompat {
    private static final boolean HAS_FEATURE_X = hasFeatureX();

    private static boolean hasFeatureX() {
        try {
            Class.forName("net.minecraft.something.NewFeature");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void doSomething() {
        if (HAS_FEATURE_X) {
            // 使用新特性
            doWithNewFeature();
        } else {
            // 降级方案
            doWithOldMethod();
        }
    }
}
```

---

## 本地测试指南

### 测试单个版本

```bash
# 设置要测试的MC版本
export MC_VERSION="1.20.4"

# 构建项目
./gradlew build

# 运行客户端
./gradlew runClient
```

### 测试多个版本（本地）

创建脚本 `test-all-versions.sh`:

```bash
#!/bin/bash

VERSIONS=("1.20.1" "1.20.2" "1.20.3" "1.20.4" "1.20.6")

for VERSION in "${VERSIONS[@]}"; do
    echo "🧪 Testing MC $VERSION..."

    # 修改gradle.properties中的版本
    sed -i.bak "s/minecraft_version=.*/minecraft_version=$VERSION/" gradle.properties

    # 清理并构建
    ./gradlew clean build --no-daemon

    # 检查构建结果
    if [ $? -eq 0 ]; then
        echo "✅ MC $VERSION passed"
    else
        echo "❌ MC $VERSION failed"
        exit 1
    fi

    # 恢复gradle.properties
    mv gradle.properties.bak gradle.properties
done

echo "🎉 All versions passed!"
```

使用：
```bash
chmod +x test-all-versions.sh
./test-all-versions.sh
```

### Gradle多版本构建

创建 `gradle/multi-version.properties`:

```properties
# 版本列表
mc.1.20.1.version=1.20.1
mc.1.20.1.yarn=1.20.1+build.9

mc.1.20.2.version=1.20.2
mc.1.20.2.yarn=1.20.2+build.1

mc.1.20.3.version=1.20.3
mc.1.20.3.yarn=1.20.3+build.1

mc.1.20.4.version=1.20.4
mc.1.20.4.yarn=1.20.4+build.1

mc.1.20.6.version=1.20.6
mc.1.20.6.yarn=1.20.6+build.3
```

---

## CI/CD自动化测试

### GitHub Actions工作流

我们的CI/CD会自动测试所有支持的版本：

1. **构建测试** - 为每个版本编译Mod
2. **单元测试** - 运行所有单元测试
3. **集成测试** - 在测试服务器上运行
4. **兼容性检查** - 验证版本兼容性

### 触发条件

CI/CD在以下情况运行：

- 推送到 `main` 或 `develop` 分支
- 创建Pull Request
- 创建Release

---

## 版本特定问题处理

### 问题1: 不同版本的Yarn映射

**解决方案**: 使用`loom`自动处理Yarn版本

```gradle
mappings "net.fabricmc:yarn:${project.property('yarn_mappings')}:v2"
```

### 问题2: Fabric API版本差异

**解决方案**: 使用最低版本的Fabric API

```gradle
fabric_api_version=0.91.2+1.20.4  // 兼容所有1.20.x
```

### 问题3: 新增的类/方法

**解决方案**: 使用反射或条件编译

```java
// 条件编译
if (LoaderVersion.getVersion().compareTo("0.15.0") >= 0) {
    // 使用新API
} else {
    // 使用旧API
}
```

---

## 发布检查清单

在发布新版本前，请确保：

- [ ] 通过所有1.20.x版本的CI测试
- [ ] 本地测试至少3个版本（例如：1.20.1, 1.20.4, 1.20.6）
- [ ] 验证`fabric.mod.json`中的版本范围正确
- [ ] 更新CHANGELOG.md，标注支持的MC版本
- [ ] 在Modrinth和CurseForge上标记所有支持的版本

---

## 版本升级策略

### 当Minecraft更新到1.21时

1. **创建新分支**: `branch-1.21`
2. **保持1.20.x支持**: 在`main`分支继续维护
3. **测试新特性**: 在新分支测试1.21特有功能
4. **合并更新**: 将通用改进合并回main
5. **并行发布**: 同时发布1.20.x和1.21版本

### 版本支持周期

- **主要版本**: 12个月（例如：1.20.x）
- **次要版本**: 6个月（例如：1.20.4）
- **安全更新**: 直到下一个主要版本发布

---

## 工具和脚本

### 版本兼容性检查工具

创建 `scripts/check-version-compat.sh`:

```bash
#!/bin/bash

# 检查当前Mod是否兼容指定MC版本
MC_VERSION=$1

if [ -z "$MC_VERSION" ]; then
    echo "Usage: $0 <mc-version>"
    echo "Example: $0 1.20.4"
    exit 1
fi

echo "🔍 Checking compatibility with MC $MC_VERSION..."

# 读取fabric.mod.json
VERSION_RANGE=$(grep -o '"minecraft": "[^"]*"' src/main/resources/fabric.mod.json | cut -d'"' -f4)

if [[ "$MC_VERSION" =~ $VERSION_RANGE ]]; then
    echo "✅ Compatible with MC $MC_VERSION (range: $VERSION_RANGE)"
    exit 0
else
    echo "❌ Not compatible with MC $MC_VERSION (range: $VERSION_RANGE)"
    exit 1
fi
```

### 批量版本测试脚本

创建 `scripts/batch-test.sh`:

```bash
#!/bin/bash

# 批量测试多个MC版本
VERSIONS=("1.20.1" "1.20.2" "1.20.3" "1.20.4" "1.20.6")

REPORT_FILE="test-report-$(date +%Y%m%d-%H%M%S).txt"
echo "WorldSoul Mod - Version Compatibility Test Report" > $REPORT_FILE
echo "Generated: $(date)" >> $REPORT_FILE
echo "========================================" >> $REPORT_FILE

for VERSION in "${VERSIONS[@]}"; do
    echo "" >> $REPORT_FILE
    echo "Testing MC $VERSION..." >> $REPORT_FILE

    # 运行测试
    ./gradlew test -PmcVersion=$VERSION >> $REPORT_FILE 2>&1

    if [ $? -eq 0 ]; then
        echo "✅ MC $VERSION: PASSED" >> $REPORT_FILE
    else
        echo "❌ MC $VERSION: FAILED" >> $REPORT_FILE
    fi
done

echo "" >> $REPORT_FILE
echo "========================================" >> $REPORT_FILE
echo "Test completed." >> $REPORT_FILE

cat $REPORT_FILE
```

---

## 常见问题

### Q: 为什么不直接支持1.19.x或1.21.x？

**A**: 为了保证代码质量和测试覆盖率，我们优先支持当前的主要版本系列（1.20.x）。未来会扩展到其他版本。

### Q: Mod在1.20.1上运行报错怎么办？

**A**: 请提交Issue，包含：
- MC版本
- Mod版本
- 错误日志
- 复现步骤

### Q: 我可以为某个版本做贡献吗？

**A**: 当然！请参考[贡献指南](CONTRIBUTING.md)。我们在PR中会测试多个版本。

---

## 参考资源

- [Fabric Wiki - Versioning](https://fabricmc.net/wiki/documentation:minecraft:javadoc/)
- [Fabric Multi-Project Setup](https://github.com/FabricMC/fabric-example-mod)
- [Minecraft Version Wiki](https://minecraft.fandom.com/wiki/Java_Edition_1.20.4)

---

**最后更新**: 2026-02-17
