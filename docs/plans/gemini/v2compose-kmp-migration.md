# V2compose KMP 迁移计划

## 背景与动机
V2compose 目前正在向 Compose Multiplatform 框架迁移，以同时支持 Android 和 iOS 平台。项目已经成功将基础层迁移到了 `shared` 模块中，包括数据模型（`bean`）、网络层（Ktor）、数据源（DataStore 封装）以及 Repositories 层。接下来的工作重点是将表现层和业务逻辑层（ViewModels、UI 组件、导航和依赖注入）从 Android 的 `app` 模块迁移到 Kotlin Multiplatform 的 `shared` 模块中。

## 范围与影响
本次迁移的范围包括：
- `ViewModel` 类和 `UseCase` 业务逻辑组件。
- Jetpack Compose UI 界面及相关状态类。
- Jetpack Navigation Compose 导航逻辑。
- Koin 依赖注入配置（`KoinModules.kt`）。
- Android 和 iOS 平台的主要 UI 入口点。

本次迁移将影响应用程序的整个表现层。它将把所有业务逻辑和 UI 集中到 `shared` 模块中，使 `app` 模块成为仅用于 Android 特定初始化的轻量级外壳。

## 提议方案
我们将采用**逐层迁移（Layer-by-Layer）**的策略。这可以确保业务逻辑（ViewModels/UseCases）在处理复杂的 UI 组件和导航之前，完全在 KMP 环境中抽象和测试完毕。

## 考虑过的备选方案
- **逐屏迁移（Screen-by-Screen）：** 一次迁移一个屏幕、对应的 ViewModel 以及路由。被拒绝的原因是，这需要临时管理两个导航图或在 Android 和 KMP 之间代理路由，增加了过渡期的样板代码。
- **大爆炸式迁移（Big Bang）：** 一次性移动所有剩余文件。被拒绝的原因是存在引入隐蔽 bug 的高风险，并且代码审查难度大。

## 实施计划

### 阶段 1：UseCase 与 ViewModel 迁移
1. **移动 UseCases：** 将剩余的业务逻辑（如 `FixHtmlUseCase.kt`）从 `app/src/main/java/io/github/v2compose/usecase/` 移动到 `shared/src/commonMain/kotlin/io/github/v2compose/usecase/`。
2. **ViewModel KMP 兼容：** 确保所有的 ViewModels 继承由 JetBrains KMP Lifecycle 库提供的 `androidx.lifecycle.ViewModel`，而不是 Android 特定的实现。
3. **移动 ViewModels：** 将所有的 ViewModels 从 `app/.../ui/*` 目录重新安置到 `shared/.../ui/*/` 中已经创建好的对应目录下。
4. **修复依赖：** 通过显式传递所需数据或使用 KMP 抽象（如 `PlatformContext`），处理 ViewModels 中 Android 特定的依赖（例如 `Context` 或 `Application` 的引用）。

### 阶段 2：UI 组件迁移
1. **重置 UI 屏幕：** 将所有的 Compose 函数、屏幕文件和 UI 状态类从 `app/.../ui/` 移动到 `shared/.../ui/`。
2. **处理 Android 特定 API：** 使用 `expect/actual` 模式或 `shared` 中现有的 `AppPlatformHandlers` 抽象，替换 Android 特定的 API（如 `Intent` 生成、`Toast`、分享功能）。
3. **资源处理：** 确保字符串资源、图标（Drawables）等使用新的 Compose Multiplatform Resource 库，而不是 Android 的 `R.string` 和 `R.drawable`。

### 阶段 3：导航与依赖注入迁移
1. **导航图：** 将 Jetpack Navigation Compose 逻辑从 `app/.../V2AppNavGraph.kt` 迁移到 `shared/.../AppNavigation.kt`。使用 JetBrains Navigation KMP 库定义完整的路由结构。
2. **Koin DI 重构：** 将 `KoinModules.kt` 移动到 `shared`。将 DI 定义拆分为用于共享依赖的 `commonMain` 和用于平台特定实现的 `androidMain`/`iosMain`。在共享模块中初始化 Koin。

### 阶段 4：平台入口点连接与文档生成
1. **导出计划文档：** 实施阶段的第一步是将本计划文件写入到项目根目录下的 `docs/v2compose-kmp-migration.md` 文件中。
2. **Android 入口点：** 更新 `app` 模块中的 `MainActivity.kt` 和 `V2App.kt`，以启动共享的 Compose 应用程序入口点（例如 `V2AppShell`）并初始化共享 DI。
3. **iOS 入口点：** 在 `shared/src/iosMain/...` 中创建 `MainViewController.kt`，以将共享的 Compose UI 暴露给 Swift/iOS 应用程序。
4. **清理：** 从 Android `app` 模块中删除未使用的配置、目录和文件。

## 验证
- 在每个阶段结束后，执行完整的项目构建（`./gradlew assembleDebug`）。
- 手动测试 Android 应用程序，以确保迁移后 UI 和业务逻辑运行正常。
- 为 iOS 编译 `shared` framework（`./gradlew linkDebugFrameworkIosSimulatorArm64`）以确认 KMP 兼容性。

## 迁移与回滚
由于迁移主要包含重新安置和调整现有文件，如果在任何阶段遇到无法克服的 KMP 限制，可以通过 `git revert` 或切换回上一个分支状态来轻松执行回滚。