# V2compose Agent 指南

本文档提供了 V2compose 项目的高层概览，旨在帮助 AI 代理和开发者快速理解项目架构、技术栈及开发模式。

## 交互协议
- **语言**: 除非特别要求，否则使用中文回复。
- **代码提交**: 每当完成重要的代码修改或功能实现后，请主动执行 `git commit` 保存进度。
- **任务验证**: 每次执行任务完毕，都要进行验证，不仅仅是验证受影响最大的 module，而是整个项目是否可以正确构建。

## 项目概览
V2compose 是一个现代化的 V2EX Android 客户端，完全采用 **Jetpack Compose** 和 **Material You** (Material 3) 设计原则构建。项目正在迁移至 **Compose Multiplatform**，以支持 iOS 平台。

## 技术栈

### 核心语言与框架
- **语言**: Kotlin
- **UI 框架**: Jetpack Compose, Material 3
- **多平台**: Android (已完成), iOS (迁移中)

### 依赖管理
- **依赖注入**: Koin (已支持 KMP)
- **网络层**:
  - shared 模块: Ktor Client
  - app 模块: OkHttp + Retrofit (遗留)
- **数据解析**: Moshi, Gson (部分遗留), Jsoup (HTML 解析)
- **图片加载**: Coil (支持 GIF 和 SVG)
- **HTML 解析**: 自定义 `htmlText` 模块及 `Fruit` 库 (基于 ksoup/Jsoup)
- **导航**: Jetpack Navigation Compose
- **本地存储**: DataStore Preferences (已支持 KMP)
- **构建工具**: Gradle Kotlin DSL, Version Catalogs (`libs.versions.toml`)

## 项目模块结构

### `:app` - 主应用模块
- 包含所有 UI 界面、业务逻辑 (`ViewModel`)、依赖注入配置 (`Koin`) 和导航图
- 也是 Android 应用的主入口

### `:shared` - Kotlin Multiplatform 共享模块
- 数据模型 (`bean/`)
- 网络层 (`network/`) - Ktor Client
- 工具类 (`utils/`)
- 核心功能 (`core/`)
- 已在 build.gradle.kts 中配置 iOS 目标

### `:htmlText` - Android 专用库模块
- 用于将 HTML 内容高效渲染为原生 Compose 文本 (`AnnotatedString`)
- 处理富文本显示、链接点击等

### `:fruit-kt` (复合构建)
- **`fruit`**: 核心解析库，用于将 HTML 结构映射为 Kotlin 对象
- **`fruit-converter-retrofit`**: Retrofit 转换器

### `:app` 模块包结构
- `io.github.v2compose.ui`: 存放 Compose 屏幕 (Screens)、组件 (Components) 和主题 (Theme)
- `io.github.v2compose.usecase`: 业务逻辑层 (Use Cases)
- `io.github.v2compose.repository`: 数据仓库层 (Repositories)
- `io.github.v2compose.datasource`: 数据源层 (Data Sources)
- `io.github.v2compose.bean`: 数据实体模型 (Data Models)
- `io.github.v2compose.core`: 核心工具类、扩展函数和基类

## 架构模式

### MVVM / MVI
- `ViewModel` 管理 UI 状态，通常暴露 `StateFlow` 供 UI 观察
- 采用 **单向数据流 (Unidirectional Data Flow)**: UI 观察状态变化并渲染，用户操作触发事件发送给 ViewModel 处理

### 依赖注入
- 使用 Koin 进行依赖注入
- `KoinModules.kt` 提供核心组件配置

### 状态管理
- `V2AppState` 负责管理应用级 UI 状态（如 `ScaffoldState`, `SnackbarHostState`, `NavController`）

## KMP 迁移状态

### 已完成
- 数据模型迁移到 `shared/src/commonMain/.../bean/`
- 网络层迁移到 `shared/src/commonMain/.../network/` (Ktor)
- Platform expect/actual 实现 (Android + iOS)
- iOS 目标配置 (`iosArm64`, `iosSimulatorArm64`)

### 待迁移
- Repository 接口
- ViewModel
- UI 组件
- 导航逻辑
- DataStore 封装
- 依赖注入模块

## 核心组件与功能

### `HtmlText`
- 关键组件，用于渲染 V2EX 的主题内容和回复
- 必须确保能正确处理 HTML 标签、链接点击（特别是 V2EX 的用户提及 `@user` 和楼层跳转 `#floor`）以及图片显示

### `V2AppNavGraph`
- 集中定义了应用的所有导航路由和参数

### `V2composeTheme`
- 实现了 Material You 的动态取色 (Dynamic Color) 和深色模式适配

## 开发指南

1. **Compose 优先**: 所有新的 UI 必须使用 Jetpack Compose 构建
2. **状态提升 (State Hoisting)**: 尽量编写无状态 (Stateless) 的 Composable 函数
3. **Material 3 规范**: 严格遵循 Material 3 的设计规范
4. **KMP 兼容**: 新代码应考虑跨平台兼容性，避免直接依赖 Android 特定 API
5. **图片加载**: 使用 Coil 的 `AsyncImage` 或 `SubcomposeAsyncImage`
6. **错误处理**: 网络请求和解析错误应在 Repository 层捕获

## 常见任务指引

### 添加新功能
1. 定义数据模型 (`bean`)
2. 在 `shared` 模块中添加请求方法 (Ktor)
3. 实现 `Repository` 和 `UseCase`
4. 创建 `ViewModel` 和 `UI` 界面

### 修改 UI
检查 `V2composeTheme` 和 `ColorScheme` 确保样式一致性

### 调试网络
检查 `network` 包下的拦截器配置和 `OkHttpClient` 设置 (app 模块)

## iOS 开发注意事项

iOS 平台目前尚不支持的功能：
- `WebViewScreen` - 依赖 Android WebView
- `GalleryScreen` - 依赖 Android 特定 API
- WorkManager - 需使用 iOS Background Tasks 替代
- Markwon - Android 专用，需替换为 Compose Markdown
- 部分第三方 UI 库