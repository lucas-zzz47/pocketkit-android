# PocketKit · Android 原生工具箱

一个面向日常场景的 Android 原生工具箱 Demo。使用 Kotlin 和 Android SDK 开发，界面由原生 View 构建，无 WebView、无需账户、无需网络权限。

## 功能

- 文本清理：合并多余空格、整理空行、复制结果。
- 二维码生成：在本地生成文字或链接二维码，并保存 PNG。
- 图片压缩：通过系统文件选择器选择图片、调整 JPEG 质量，另存压缩结果；大图会缩放以控制内存使用，透明部分以白色填充。
- 链接解析：展示协议、域名、路径和参数数量，可复制去除常见追踪参数后的链接。
- 最近使用：使用 SharedPreferences 在本机记录最近操作，不保存工具输入内容。

## 运行

1. 安装 Android Studio、JDK 17 和 Android SDK Platform 35。
2. 用 Android Studio 打开本目录，等待 Gradle 同步。
3. 选择 Android 8.0（API 26）或更新版本的模拟器或设备，运行 `app`。

命令行构建：`gradlew.bat assembleDebug`；单元测试：`gradlew.bat testDebugUnitTest`。首次运行需要联网下载 Gradle 和项目依赖。二维码由 ZXing Core 生成。

图片和二维码均由系统文件选择器保存到用户指定的位置，应用不需要存储权限。系统文件选择器的外观可能因手机厂商而异。
