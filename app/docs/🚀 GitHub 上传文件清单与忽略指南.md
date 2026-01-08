# 🚀 GitHub 上传文件清单与忽略指南

为了确保项目的可移植性和安全性，上传至 GitHub 时应严格遵守以下规范。

## 1. 核心上传清单 (Must Upload)

这些文件是项目的“灵魂”，缺少任何一个都会导致他人无法编译你的项目。

### 📁 项目根目录

- `app/`：整个应用的核心源码目录（详见下方）。
- `build.gradle` (Project level)：项目级构建脚本。
- `settings.gradle`：模块配置文件。
- `gradle.properties`：Gradle 编译参数。
- `gradlew` & `gradlew.bat`：Gradle 包装器脚本（确保环境一致）。
- `gradle/wrapper/`：Gradle 包装器相关的 jar 和配置文件。
- `.gitignore`：**极其重要**，决定了哪些文件不被上传。
- `README.md`：项目的门面说明。
- `LICENSE`：开源协议文件。

### 📁 app 模块目录 (`/app`)

- `src/`：所有的 Kotlin 代码、布局文件、图片资源、艺术字体等。
- `build.gradle` (App level)：应用插件、依赖库配置。
- `proguard-rules.pro`：代码混淆规则。
- `docs/`：我们之前创建的技术文档目录。

## 2. 严禁上传清单 (Do NOT Upload)

这些文件通常由本地环境生成，或者包含敏感信息，必须通过 `.gitignore` 排除。

- **`local.properties`**：包含你的 Android SDK 路径，每个人电脑都不一样。
- **`.gradle/` & `.idea/`**：本地编译缓存和 IDE 个人偏好设置。
- **`build/` & `app/build/`**：编译生成的中间文件和最终的 APK/Bundle。
- **`\*.jks` 或 `\*.keystore`**：应用的签名文件（**泄露会导致应用被冒名顶替**）。
- **`captures/`**：性能分析的截图或记录。

## 3. 标准 .gitignore 配置模板

如果你的项目根目录还没有 `.gitignore`，请创建一个并粘贴以下内容（这是 Android 开发的标准模板）：

```
# 编译生成文件
/build
/*/build

# IDE 配置文件
.idea/
.gradle/
*.iml
.DS_Store

# 个人环境配置
local.properties

# 签名文件
*.jks
*.keystore

# 临时文件
.temp/
.log
```

## 4. 上传建议流程

1. **检查 Git 状态**： 在 Android Studio Terminal 输入 `git status`，确认没有 `.idea` 或 `build` 文件夹出现在“待提交”列表中。
2. **编写 Commit Message**： 使用清晰的描述，例如：`feat: 完善 Jetpack Glance 跨进程同步方案并添加说明文档`。
3. **核对 README**： 确保 README 中的图片链接和项目描述是最新的。

**💡 提示**：如果你不小心把 `local.properties` 传上去了，记得立即从 GitHub 删除它并提交一次新的忽略纪录，否则别人克隆后会因为 SDK 路径不对而报错。