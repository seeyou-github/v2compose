# V2compose Agent 指南

本文档提供了 V2compose 项目的高层概览，旨在帮助 AI 代理和开发者快速理解项目架构、技术栈及开发模式。

## 交互协议

- **语言**: 除非特别要求，否则使用中文回复。
- **代码提交**: 每当完成重要的代码修改或功能实现后，请主动执行 `git commit` 保存进度。
- **任务验证**: 每次执行任务完毕，都要进行验证。不仅仅是验证受影响最大的模块，而是整个项目是否可以正确构建（Android/iOS）。

## 项目概览

V2compose 是一个现代化的 V2EX 客户端，采用 **Compose Multiplatform** 构建，旨在同时支持 Android 和 iOS 平台。
项目遵循 **Material 3 (Material You)** 设计原则，提供流畅、美观的用户体验。

## 技术栈

### 核心语言与框架

- **语言**: Kotlin (Kotlin Multiplatform)
- **UI 框架**: Compose Multiplatform (Jetpack Compose), Material 3
- **多平台支持**: Android (主要开发平台), iOS (适配中)

### 依赖管理

- **依赖注入**: Koin (Compose Multiplatform 版本)
- **网络层**: Ktor Client (跨平台实现)
- **图片加载**: Coil 3 (支持 KMP, GIF 和 SVG)
- **HTML 解析**: 自定义 `htmlText` 模块及 `Fruit` 库 (基于 ksoup/Jsoup, 复合构建)
- **Markdown**: `mikepenz/multiplatform-markdown-renderer` (KMP)
- **导航**: `androidx.navigation.compose` (JetBrains KMP 版本)
- **本地存储**: DataStore Preferences (KMP)
- **构建工具**: Gradle Kotlin DSL, Version Catalogs (`libs.versions.toml`)

## 项目模块结构

### `:shared` - 核心逻辑与 UI 模块 (KMP)

这是项目的核心，包含：
- **`ui/`**: 绝大部分 Compose 屏幕 (Screens)、组件 (Components) 和主题 (Theme)。
- **`usecase/`**: 业务逻辑层。
- **`repository/`**: 数据仓库层。
- **`datasource/`**: 数据源（Ktor API 和 DataStore）。
- **`bean/`**: 共享数据模型。
- **`di/`**: Koin 依赖注入配置。
- **`core/`**: 核心工具类、扩展函数和跨平台抽象 (`expect/actual`)。

### `:app` - Android 壳模块

- Android 应用的主入口 (`App.kt`, `MainActivity.kt`)。
- 包含 Android 特有的导航逻辑 (`V2AppNavGraph.kt`) 和一些尚未迁移的平台依赖屏幕（如 `WebViewScreen`, `GalleryScreen`）。
- 负责 Android 端的特定初始化（如通知权限处理、`WorkManager`）。

### `:htmlText` - HTML 渲染库 (KMP)

- 跨平台库，用于将 HTML 高效渲染为原生 Compose 文本。
- 避免使用 WebView 以提升性能。

### `:fruit-kt` (复合构建)

- **`fruit`**: 核心解析库，用于通过注解将 HTML 映射为 Kotlin 对象（风格类似 Retrofit）。

## 架构模式

### MVVM / MVI

- `ViewModel` 继承自 `androidx.lifecycle.ViewModel` (KMP 版)，管理 UI 状态并暴露 `StateFlow`。
- **单向数据流 (UDF)**: UI 观察状态并发送事件给 ViewModel。

### 状态管理与导航

- `V2AppState`: 管理应用级 UI 状态（如 `SnackbarHostState`, `NavController`）。
- `AppNavigation.kt`: 提供路由解析和导航 Action。

## KMP 迁移现状

### 已迁移至 `:shared` (commonMain)
- 数据模型 (`bean`)、网络层 (`network`)、本地存储 (`datasource`)。
- Repository 和 UseCase。
- 大部分 UI 屏幕 (Topic, User, Node, Search, Settings, Login, Write, Supplement) 及其 ViewModel。

### 待处理 / 平台相关
- `WebViewScreen` & `GalleryScreen`: 仍保留在 `:app` 模块，依赖 Android 原生组件。
- 导航图完全迁移：`V2AppNavGraph` 仍部分位于 `:app`。

## 开发指南

1. **KMP 优先**: 除非是平台特有功能，否则新代码必须编写在 `:shared` 模块的 `commonMain` 中。
2. **Material 3 规范**: 严格遵循 M3 设计规范，使用 `Theme.of(context)` 访问色值。
3. **平台抽象**: Android 特有 API (如 `Intent`, `WorkManager`) 需通过 `AppPlatformHandlers` 或 `expect/actual` 进行抽象。
4. **资源处理**: 使用 Compose Multiplatform Resources (`Res`) 管理字符串和图片，避免直接使用 `R.string`。
5. **代码风格**: 遵循 `import` 规范，避免在代码中写全路径调用。

## 常见任务指引

### 修改 UI
- UI 主要位于 `shared/src/commonMain/kotlin/io/github/v2compose/ui/`。
- 修改后需验证 Android 编译。

### 调试网络
- 查看 `shared/src/commonMain/kotlin/io/github/v2compose/network/` 下的 Ktor 配置。
- 使用 `KLogger` 进行跨平台日志记录。
