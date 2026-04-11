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
- **依赖注入**: Koin (已支持 KMP)
- **网络层**:
    - shared 模块: Ktor Client
    - app 模块: OkHttp + Ktor (KMP 迁移中)
- **数据解析**: kotlinx-serialization (推荐)
- **图片加载**: Coil (支持 GIF 和 SVG)
- **HTML 解析**: 自定义 `htmlText` 模块及 `Fruit` 库 (基于 ksoup/Jsoup)
- **导航**: Jetpack Navigation Compose
- **构建工具**: Gradle Kotlin DSL, Version Catalogs (`libs.versions.toml`)

## 项目结构

项目包含以下核心模块：

- **`:app`**: 主应用模块。
    - 包含所有 UI 界面、业务逻辑 (`ViewModel`)、依赖注入配置 (`Koin`) 和导航图。
    - 也是应用的主入口。
- **`:htmlText`**: 专门的 Android 库模块。
    - 用于将 HTML 内容高效渲染为原生 Compose 文本 (`AnnotatedString`)，避免使用 WebView 的性能开销。
    - 处理富文本显示、点击事件等。
- **`:v2exApi`**: (已迁移至 shared) V2EX API 客户端库。
- **`:shared`**: KMP 共享模块，包含 API 请求、实体类等。
- **`:fruit-kt` (复合构建)**: 外部依赖项目，作为源码引入。
    - **`fruit`**: 核心解析库，用于将 HTML 结构映射为 Kotlin 对象 (类似 Retrofit 的方式，但针对 HTML)。
    - **`fruit-converter-retrofit`**: Retrofit 转换器 (遗留)。

### `:app` 模块包结构

- `io.github.v2compose.ui`: 存放 Compose 屏幕 (Screens)、组件 (Components) 和主题 (Theme)。
- `io.github.v2compose.usecase`: 业务逻辑层 (Use Cases)。
- `io.github.v2compose.repository`: 数据仓库层 (Repositories)，协调数据源。
- `io.github.v2compose.datasource`: 数据源层 (Data Sources)，包括远程 API 和本地存储。
- `io.github.v2compose.bean`: 数据实体模型 (Data Models)。
- `io.github.v2compose.core`: 核心工具类、扩展函数 and 基类。

## 架构模式

- **MVVM / MVI**:
    - `ViewModel` 管理 UI 状态，通常暴露 `StateFlow` 供 UI 观察。
    - 采用 **单向数据流 (Unidirectional Data Flow)**：UI 观察状态变化并渲染，用户操作触发事件发送给
      ViewModel 处理。
- **依赖注入**:
    - 使用 Koin 进行依赖注入。
- **状态管理**:
    - `V2AppState` 负责管理应用级 UI 状态（如 `ScaffoldState`, `SnackbarHostState`, `NavController`）。

## 核心组件与功能

- **`HtmlText`**:
    - 关键组件，用于渲染 V2EX 的主题内容和回复。
    - 必须确保能正确处理 HTML 标签、链接点击（特别是 V2EX 的用户提及 `@user` 和楼层跳转 `#floor`
      ）以及图片显示。
- **`V2AppNavGraph`**:
    - 集中定义了应用的所有导航路由 and 参数。
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
    - 使用项目提供的 `ImageLoader` (Coil)。
6. **错误处理**:
    - 网络请求和解析错误应在 Repository 层捕获，并转换为 UI 友好的错误状态传递给 ViewModel。
    - 推荐使用 `shared` 模块中定义的异常扩展。
7. **KMP 依赖查找**:
    - 优先在 [klibs.io](https://klibs.io/) 上查找 KMP 项目所需的第三方仓库或替代方案。
8. **代码风格 (导入规范)**:
    - 避免在代码中直接使用完整的包名加类名（如 `com.example.MyClass.method()`）来调用属性或方法。
    - 应始终在文件顶部先 `import` 导入该类，然后直接使用类名进行调用，以保持代码的整洁和可读性。
