# V2compose Agent 指南

本文档提供了 V2compose 项目的高层概览，旨在帮助 AI 代理和开发者快速理解项目架构、技术栈及开发模式。

## 交互协议

- **语言**: 除非特别要求，否则使用中文回复。
- **代码提交**: 每当完成重要的代码修改或功能实现后，请主动执行 `git commit` 保存进度。
- **任务验证**: 每次执行任务完毕，都要进行验证。不仅仅是验证受影响最大的模块，而是整个项目是否可以正确构建（Android/iOS）。
- **归档实施计划 (Plan Documentation)**: 每次制定并实施重要的 Plan（计划）后，必须将该 Plan 的详细文档同步归档至
  `docs/plans/` 目录下的相应分类中（如 `gemini/` 或 `codex/`），以确保开发链路的可追溯性和知识沉淀。建议文件命名遵循
  `<agent_name>-<topic>-plan.md`。

## 项目概览

V2compose 是一个现代化的 V2EX 客户端，采用 **Compose Multiplatform (KMP)** 构建，旨在同时支持 Android
和 iOS 平台。
项目遵循 **Material 3 (Material You)** 设计原则，提供流畅、美观的用户体验。
**注：目前项目的绝大部分 UI 和业务逻辑已完全迁移至 KMP 共享层（即 `:shared` 模块）。**

## 技术栈

### 核心语言与框架

- **语言**: Kotlin (Kotlin Multiplatform 2.3.20+)
- **UI 框架**: Compose Multiplatform (Jetpack Compose 1.10.3+), Material 3
- **多平台支持**: Android (Min SDK 26, Compile SDK 36), iOS (iOSArm64, iosSimulatorArm64)

### 关键库 (Libraries)

- **依赖注入**: Koin (Compose Multiplatform 版本)
- **网络层**: Ktor Client (跨平台实现, OkHttp/Darwin)
- **图片加载**: Coil 3 (支持 KMP, GIF 和 SVG)
- **HTML 解析**: 自定义 `htmlText` 模块及 `Fruit` 库 (基于 ksoup/Jsoup, 复合构建)
- **Markdown**: `mikepenz/multiplatform-markdown-renderer` (KMP)
- **Web 容器**: `compose_webview_multiplatform` (支持 WebView)
- **导航**: `androidx.navigation.compose` (JetBrains KMP 版本)
- **本地存储**: DataStore Preferences (KMP)
- **数据流**: Paging 3 (KMP 版)
- **配置工具**: BuildKonfig (用于管理 API 密钥等构建配置)
- **构建工具**: Gradle Kotlin DSL, Version Catalogs (`libs.versions.toml`)

## 项目模块结构

### `:shared` - 核心逻辑与 UI 模块 (KMP)

这是项目的绝对核心，包含几乎所有的业务逻辑与 UI 界面：

- **`ui/`**: Compose 屏幕 (Screens)、组件 (Components) 和主题 (Theme)。所有页面均已全面迁移至此。
- **`usecase/`**: 业务逻辑层。
- **`repository/`**: 数据仓库层。
- **`datasource/`**: 数据源（Ktor API 和 DataStore）。
- **`bean/`**: 共享数据模型。
- **`di/`**: Koin 依赖注入配置。
- **`core/`**: 核心工具类、扩展函数和跨平台抽象。

### `:app` - Android 壳模块

- Android 应用的主入口 (`MainActivity.kt`, `V2App.kt`)。
- **构建变体 (Flavors)**:
    - 已移除 `foss/google` 双渠道，Android 统一为单渠道构建。

### `:iosApp` - iOS 壳模块

- iOS 应用的主入口 (`iOSApp.swift`, `MainViewController.kt`)。提供 KMP 的稳定导出入口，绑定 iOS
  端的特定平台能力。

### `:htmlText` - HTML 渲染库 (KMP)

- 跨平台库，用于将 HTML 高效渲染为原生 Compose 文本。
- 避免在列表中使用 WebView 以提升滚动性能。

### `:fruit-kt` (复合构建)

- 核心解析库，用于通过注解将 HTML 映射为 Kotlin 对象。通过 `includeBuild` 在 `settings.gradle.kts`
  中引入。

## 架构模式

### MVVM / MVI

- `ViewModel` 继承自 `androidx.lifecycle.ViewModel` (KMP 版)，管理 UI 状态并暴露 `StateFlow`。
- **单向数据流 (UDF)**: UI 观察状态并发送事件给 ViewModel。

### 状态管理与导航

- `V2AppState`: 管理应用级 UI 状态（如 `SnackbarHostState`, `NavController`）。
- **完全共享的导航栈**: 导航逻辑 (`AppNavigation.kt` 和各层级 NavGraph) 已在 `:shared`
  模块中完全实现，支持跨平台路由解析和导航。

## 平台边界与能力矩阵 (Platform Boundaries)

平台特定能力通过明确的接口边界下沉或暴露：

- **`AppPlatformHandlers`**: 处理意图相关操作（如系统分享、外部浏览器跳转、发送邮件、保存图片至相册等）。
- **`PlatformCapabilities`**: 平台能力开关矩阵（例如定义不同平台是否支持某些后台服务或特定设置项）。
- **`AutoCheckInScheduler`**: 自动签到定时任务的平台实现（Android WorkManager / iOS Background
  Tasks）。
- **`WebViewProxyController`**: 提供跨平台的 WebView 代理注入与配置能力。
- **`PlatformContext`**: 提供底层平台上下文访问能力（如 Android 的 `Context`，iOS 的
  `UIViewController`）。

## 开发指南

1. **KMP 绝对优先**: 除非是极个别的平台底层 API 对接，否则所有新功能（含 UI、逻辑、网络、存储）必须编写在
   `:shared` 模块的 `commonMain` 中。
2. **Material 3 规范**: 严格遵循 M3 设计规范，使用 `Theme.of(context)` 访问色值。
3. **平台抽象**: 平台特有 API 需通过上述定义的边界接口或 `expect/actual` 机制进行抽象。
4. **资源处理**: 使用 Compose Multiplatform Resources (`Res`) 管理资源，通过 `Res` 生成的代码访问。
5. **代码生成**:

- 路由与 HTML 解析依赖 `Fruit` 库的 KSP 处理。
- 版本信息由 `BuildKonfig` 在编译时生成。

6. **代码风格**: 遵循 Kotlin 官方代码风格，优先使用 `lateinit` 或 `nullable` 属性而非强制解包。

## 测试 (Testing)

- **共享代码测试**: 位于 `shared/src/commonTest/`，主要针对 `ViewModel` 和 `UseCase` 进行逻辑验证。
- **Android 测试**: 位于 `androidApp/src/androidTest/`，主要针对 Android 端特有的 UI 组件或 Service
  进行验证。
- **测试命令**: 使用 `./gradlew :shared:allTests` 运行所有平台的测试任务。

## 常见任务指引

### 修改 UI 或添加新页面

- 绝大部分 UI 修改请在 `shared/src/commonMain/kotlin/io/github/v2compose/ui/` 下进行。
- 新页面也应当在此处编写，并在同级的 NavGraph 中注册。
- 修改后请验证 Android/iOS 双端构建 (`./gradlew :androidApp:assembleDebug` 和
  `./gradlew :shared:compileKotlinIosSimulatorArm64`)。

### 调试网络

- 查看 `shared/src/commonMain/kotlin/io/github/v2compose/network/` 下的 Ktor 配置。
- 使用 `KLogger` 进行跨平台日志记录。
