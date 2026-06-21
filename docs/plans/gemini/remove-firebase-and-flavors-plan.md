# 彻底移除 Firebase 与 Android 渠道设计（FOSS / Google）实施计划

## 背景与目的
用户要求彻底从项目中删除 **Firebase Analytics** 和 **Firebase Crashlytics**，并移除 Android 构建中的**分渠道设计（FOSS vs. Google）**，统一为单渠道构建。

## 实施方案

### 1. 修改依赖管理 `gradle/libs.versions.toml`
- 移除 Firebase/Crashlytics 相关的 dependencies 和 plugins 声明。

### 2. 修改 `androidApp/build.gradle.kts`
- 移除 `isGoogleTask` 检测逻辑。
- 移除在 `isGoogleTask` 条件下动态应用谷歌服务的逻辑（不再应用 `google-services` 和 `crashlytics` 插件）。
- 移除 `flavorDimensions` 和 `productFlavors`（`foss` 与 `google`），使 Android 应用变为纯粹的单渠道应用。
- 移除 `dependencies` 中对 Firebase 的引用（`"googleImplementation"...`）。
- 确保 `buildTypes` 的 `debug` 和 `release` 依然使用配置好的 `Test.jks` 硬编码签名。

### 3. 清理 Android 源代码与资源
- 移除 `androidApp/src/google` 目录（包含其中的 `VendorAnalytics.kt`）。
- 移除 `androidApp/src/foss` 目录（包含其中的 `VendorAnalytics.kt`）。
- 将 `VendorAnalytics` 逻辑在 `androidApp/src/main/kotlin/io/github/v2compose/core/analytics/VendorAnalytics.kt`（或类似主路径）中进行重构，或者因为不需要 Analytics，直接使 `IAnalytics` 实现为一个空实现，或者彻底废弃。为了保证极简，我们直接在 `main` 目录下实现一个空的 `VendorAnalytics`，或者由于没有 Firebase 追踪，直接把追踪接口实现为空，避免大范围重构主代码中的 `analytics` 注入。
- 移除 `androidApp/src/googleRelease` 目录（若有，它存放了 `google-services.json` 占位符）。

### 4. 清理 iOS 原生端 Firebase
- 修改 `iosApp/project.yml`：
  - 移除 Firebase Package 依赖（`https://github.com/firebase/firebase-ios-sdk.git`）。
  - 移除 target 的 Framework 依赖：`FirebaseCore`、`FirebaseAnalytics`、`FirebaseCrashlytics`。
  - 移除 Build Phase 中的 Firebase 复制和 `Validate Firebase config`、`Upload Crashlytics dSYM` 运行脚本。
- 重新用 XcodeGen 生成 `iosApp.xcodeproj`（如果本地有该命令，或直接手动/通过命令处理）。
- 修改 `iosApp/iosApp/iOSApp.swift`：
  - 移除 `FirebaseBootstrap.configureIfNeeded()`。
- 删除 `iosApp/iosApp/FirebaseBootstrap.swift`。

### 5. 修改 `.github/workflows/release.yml`
- 移除 `GOOGLE_SERVICES_JSON_RELEASE_BASE64` 的解密和拷贝步骤。
- 更新 Gradle 编译命令：由 `:androidApp:assembleFossRelease :androidApp:assembleGoogleRelease` 改为统一编译单渠道 release 版本：`./gradlew --no-configuration-cache :androidApp:assembleRelease`。
- 修改资产拷贝逻辑：只需拷贝生成的单个 APK 即可。

## 验证计划
- 本地编译 Android 检查是否有编译错误。
- 检查 iOS 编译配置是否正常。
