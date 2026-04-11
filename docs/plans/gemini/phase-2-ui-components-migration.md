# 阶段 2 UI 组件迁移计划

## 目标
将剩余的 UI 屏幕、通用组件和导航逻辑从 Android `app` 模块迁移到 Kotlin Multiplatform (`shared`) 模块。

## 关键文件与上下文
- **源 (app)**: `app/src/main/java/io/github/v2compose/ui/`
- **目标 (shared)**: `shared/src/commonMain/kotlin/io/github/v2compose/ui/`
- **平台处理器**: `shared/src/commonMain/kotlin/io/github/v2compose/AppPlatformHandlers.kt`
- **导航**: `shared/src/commonMain/kotlin/io/github/v2compose/AppNavigation.kt`

## 实施步骤

### 1. 基础组件与工具类
1.  **迁移 `BaseScreenState.kt`**:
    *   从 `app/.../ui/` 移动到 `shared/.../ui/`。
    *   更新 import 并移除 Android 特有的依赖。
2.  **抽象 `AutoFillModifier`**:
    *   在 `shared/commonMain/.../common/AutoFillModifier.kt` 中创建 `expect fun Modifier.autofill(...)`。
    *   在 `shared/androidMain` 中使用现有的 Android 逻辑实现 `actual` 版本。
    *   在 `shared/iosMain` 中实现空的 `actual` 版本。
3.  **更新 `AppPlatformHandlers`**:
    *   新增以下属性：
        ```kotlin
        val openAppSettings: () -> Unit,
        val copyToClipboard: (String) -> Unit,
        val checkNotificationPermission: () -> Boolean,
        val requestNotificationPermission: () -> Unit,
        ```
    *   在 `app`（或 `shared/androidMain`）中提供 Android 端实现。

### 2. 通用 UI 组件迁移
1.  **移动 `ui/common/` 下的组件**:
    *   `CloseButton.kt`, `SegmentControl.kt`, `StateList.kt`, `TextEditor.kt`, `ListDialog.kt`, `LoginComposables.kt`。
    *   将 `android.util.Log` 替换为 `io.github.v2compose.util.KLogger`。
    *   移除 `android.annotation.SuppressLint`。

### 3. 功能屏幕迁移
1.  **登录相关 (`login/`)**:
    *   移动 `LoginScreen.kt`, `LoginScreenState.kt` 以及子包 (`google/`, `twostep/`)。
    *   **替换 `SSJetPackComposeProgressButton`**: 实现一个标准的 `Button`。在加载状态时，中心显示 `CircularProgressIndicator`。
2.  **设置相关 (`settings/`)**:
    *   移动 `SettingsScreen.kt`, `SettingsScreenState.kt` 和 `composables/`。
    *   使用 `LocalAppPlatformHandlers.current.openAppSettings` 替换电池优化和应用设置的 `Intent` 调用。
3.  **个人中心子页面 (`main/mine/`)**:
    *   移动 `MyFollowingScreen.kt`, `MyNodesScreen.kt`, `MyTopicsScreen.kt` 及其导航文件。
4.  **网页与图库 (`webview/`, `gallery/`)**:
    *   移动剩余的屏幕文件。
    *   调整 `WebViewScreen.kt`，使其支持多平台兼容的 User-Agent 处理。
5.  **发布话题与补充内容 (`write/`, `supplement/`)**:
    *   移动 `WriteTopicScreen.kt`, `AddSupplementScreen.kt` 及其状态/导航文件。

### 4. 导航集成
1.  **合并导航逻辑**:
    *   将 `NavGraphBuilder` 的扩展函数（如 `loginScreen`, `settingsScreen`）从 `app` 移动到 `shared` 对应的 `Navigation.kt` 文件中。
    *   更新 `shared` 中的 `AppNavigation.kt`，包含所有路由和解析逻辑。
2.  **更新 `V2AppNavGraph.kt`**:
    *   确保所有屏幕现在都调用 `shared` 模块的导航扩展。

## 验证与测试
1.  **构建检查**: 运行 `./gradlew :app:assembleDebug` 确保 Android 端无回归。
2.  **手动 UI 测试 (Android)**:
    *   验证登录按钮的加载状态。
    *   验证设置页面的“打开系统设置”功能正常。
    *   验证 WebView 的 User-Agent 配置。
3.  **iOS 编译**: 运行 `./gradlew :shared:iosSimArm64Main`（或等效命令）验证 `commonMain` 的 UI 代码在 iOS 目标下编译通过。
