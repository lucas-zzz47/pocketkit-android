# PocketKit · Android 原生工具箱

PocketKit 是一个轻量的 Android 效率工具 Demo，提供文本清理、二维码生成、图片压缩和链接解析。项目使用 Kotlin 和 Android SDK 原生 View 实现，运行时无需网络或账号。

## 功能

| 工具 | 功能 |
| --- | --- |
| 文本清理 | 合并连续空格、移除行首行尾空格、整理多余空行，并复制结果 |
| 二维码生成 | 将文字或链接生成二维码，通过系统文件选择器保存为 PNG |
| 图片压缩 | 选择图片、调整 JPEG 输出质量并另存；大图按采样比例缩小，透明部分填充白色 |
| 链接解析 | 查看 HTTP/HTTPS 链接的协议、域名、路径和参数数量，复制去除常见追踪参数的链接 |
| 最近使用 | 在设备本地显示最近 5 条操作记录和累计完成次数 |

输入内容和图片由设备本地处理；应用不申请网络或存储权限。图片和二维码使用 Android 系统文件选择器读取与保存，具体界面随设备而异。

## 技术与环境

- Kotlin 2.0.21，Android SDK 原生 View，Gradle 8.9，Android Gradle Plugin 8.7.3
- JDK 17；编译 SDK 35，最低支持 Android 8.0（API 26）
- 使用 [ZXing Core](https://github.com/zxing/zxing) 3.5.3 生成二维码，依赖许可见 `THIRD_PARTY_NOTICE.txt`
- 使用 SharedPreferences 保存操作记录，不保存处理的文本、链接或图片

## 构建与运行

1. 安装 Android Studio、JDK 17、Android SDK Platform 35 和 Build Tools 35.0.0。
2. 用 Android Studio 打开项目根目录，等待 Gradle 同步完成。
3. 选择 Android 8.0 或更新版本的模拟器或真机，运行 `app`。

也可以在项目根目录使用 Gradle Wrapper：

```bash
# Windows
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest

# macOS / Linux
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

首次构建需要联网下载 Gradle 和依赖。调试 APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。

## 项目结构

```text
app/src/main/java/com/pocketkit/app/
├── MainActivity.kt  # 原生界面、工具交互与文件选择
└── TextTools.kt     # 文本清理逻辑
app/src/test/        # 文本清理单元测试
app/src/main/res/    # 主题和应用图标
```

## 验证与边界

项目已完成 Debug 构建和文本清理单元测试，并在 Android 真机上安装启动。其他功能仍应在目标设备上逐项验证，例如扫描生成的二维码、打开压缩后的图片以及检查清理后的链接。当前为单端 Android Demo，不包含 iOS 版本。

二维码生成依赖 ZXing，图片输出为 JPEG，压缩结果大小会受到原始图片内容和所选质量影响。链接清理仅移除 `utm_` 前缀、`fbclid` 和 `gclid` 等常见参数，并非识别所有追踪参数。
