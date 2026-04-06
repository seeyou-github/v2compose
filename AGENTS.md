# V2compose Agent 指南

本文档提供了 V2compose 项目的高层概览，旨在帮助 AI 代理和开发者快速理解项目架构、技术栈及开发模式。

## 交互协议

- **语言**: 除非特别要求，否则使用中文回复。
- **代码提交**: 每当完成重要的代码修改或功能实现后，请主动执行 `git commit` 保存进度。
- **任务验证**: 每次执行任务完毕，对整个项目进行构建验证。

## 项目概览

V2compose 是一个现代化的 V2EX Android 客户端，完全采用 **Jetpack Compose** 和 **Material You** (
Material 3) 设计原则构建。项目旨在提供流畅、美观的用户体验，并作为 Compose 的实践范例。

## 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose, Material 3
- **依赖注入**: Hilt (Dagger)
- **网络层**: OkHttp, Retrofit
- **数据解析**: Moshi, Gson (部分遗留), Jsoup (HTML 解析)
- **图片加载**: Coil (支持 GIF 和 SVG)
- **HTML 解析**: 自定义 `htmlText` 模块及 `Fruit` 库 (基于 ksoup/Jsoup)
- **导航**: Jetpack Navigation Compose
- **构建工具**: Gradle Kotlin DSL, Version Catalogs (`libs.versions.toml`)

## 项目结构

项目包含以下核心模块：

- **`:app`**: 主应用模块。
    - 包含所有 UI 界面、业务逻辑 (`ViewModel`)、依赖注入配置 (`Hilt`) 和导航图。
    - 也是应用的主入口。
- **`:htmlText`**: 专门的 Android 库模块。
    - 用于将 HTML 内容高效渲染为原生 Compose 文本 (`AnnotatedString`)，避免使用 WebView 的性能开销。
    - 处理富文本显示、点击事件等。
- **`:v2exApi`**: V2EX API 客户端库。
    - 封装了对 V2EX 网站的请求逻辑，包括 API 调用和网页 HTML 解析。
- **`:fruit-kt` (复合构建)**: 外部依赖项目，作为源码引入。
    - **`fruit`**: 核心解析库，用于将 HTML 结构映射为 Kotlin 对象 (类似 Retrofit 的方式，但针对 HTML)。
    - **`fruit-converter-retrofit`**: Retrofit 转换器，使 HTML 解析能无缝集成到 Retrofit 流程中。

### `:app` 模块包结构

- `io.github.v2compose.ui`: 存放 Compose 屏幕 (Screens)、组件 (Components) 和主题 (Theme)。
- `io.github.v2compose.usecase`: 业务逻辑层 (Use Cases)。
- `io.github.v2compose.repository`: 数据仓库层 (Repositories)，协调数据源。
- `io.github.v2compose.datasource`: 数据源层 (Data Sources)，包括远程 API 和本地存储。
- `io.github.v2compose.bean`: 数据实体模型 (Data Models)。
- `io.github.v2compose.core`: 核心工具类、扩展函数和基类。

## 架构模式

- **MVVM / MVI**:
    - `ViewModel` 管理 UI 状态，通常暴露 `StateFlow` 供 UI 观察。
    - 采用 **单向数据流 (Unidirectional Data Flow)**：UI 观察状态变化并渲染，用户操作触发事件发送给
      ViewModel 处理。
- **依赖注入**:
    - 使用 Hilt 进行依赖注入。
    - `AppModule.kt` 提供了核心组件（如 `OkHttpClient`, `Moshi`, `ImageLoader` 等）的单例实例。
- **状态管理**:
    - `V2AppState` 负责管理应用级 UI 状态（如 `ScaffoldState`, `SnackbarHostState`, `NavController`）。

## 核心组件与功能

- **`HtmlText`**:
    - 关键组件，用于渲染 V2EX 的主题内容和回复。
    - 必须确保能正确处理 HTML 标签、链接点击（特别是 V2EX 的用户提及 `@user` 和楼层跳转 `#floor`
      ）以及图片显示。
- **`V2AppNavGraph`**:
    - 集中定义了应用的所有导航路由和参数。
- **`V2composeTheme`**:
    - 实现了 Material You 的动态取色 (Dynamic Color) 和深色模式适配。

## 开发指南

1. **Compose 优先**: 所有新的 UI 必须使用 Jetpack Compose 构建。避免引入传统的 View 体系代码。
2. **状态提升 (State Hoisting)**: 尽量编写无状态 (Stateless) 的 Composable 函数，将状态提升到调用者或
   ViewModel 中管理。
3. **Material 3 规范**: 严格遵循 Material 3 的设计规范使用组件（如 `Scaffold`, `TopAppBar`,
   `NavigationBar` 等）。
4. **HTML 数据处理**:
    - V2EX 的很多数据通过 HTML 抓取获得。
    - 使用 `Fruit` 库定义接口来解析 HTML，保持与 Retrofit 风格一致的定义方式。
    - 实体类定义在 `bean` 包中。
5. **图片加载**:
    - 使用项目提供的 `ImageLoader`，它已配置好缓存策略和对特殊格式（GIF/SVG）的支持。
    - 在 Compose 中使用 `AsyncImage` 或 `SubcomposeAsyncImage`。
6. **错误处理**:
    - 网络请求和解析错误应在 Repository 层捕获，并转换为 UI 友好的错误状态传递给 ViewModel。

## 常见任务指引

- **添加新功能**:
    1. 定义数据模型 (`bean`)。
    2. 在 `Fruit` 接口或 API 接口中添加请求方法。
    3. 实现 `Repository` 和 `UseCase`。
    4. 创建 `ViewModel` 和 `UI` 界面。
- **修改 UI**: 检查 `V2composeTheme` 和 `ColorScheme` 确保样式一致性。
- **调试网络**: 检查 `network` 包下的拦截器配置和 `OkHttpClient` 设置。
