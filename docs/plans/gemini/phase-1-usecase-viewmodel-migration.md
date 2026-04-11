# 阶段 1: UseCase 与 ViewModel 迁移计划

## 目标
将所有剩余的 UseCase 和 ViewModel 从 `app` 模块迁移到 `shared` KMP 模块。确保它们完全兼容 KMP，并且不依赖于 Android 特定的依赖项。

## 关键文件与上下文
- `docs/phase-1-usecase-viewmodel-migration.md` (此文档)
- `app/src/main/java/io/github/v2compose/usecase/FixHtmlUseCase.kt` -> `shared/src/commonMain/kotlin/io/github/v2compose/usecase/FixHtmlUseCase.kt`
- `app/src/main/java/io/github/v2compose/ui/*/*ViewModel.kt` (共 9 个文件) -> `shared/src/commonMain/kotlin/io/github/v2compose/ui/*/*ViewModel.kt`

## 实施步骤

### 0. 计划归档
- 将本计划文档保存到项目代码库的 `docs/phase-1-usecase-viewmodel-migration.md` 路径下。

### 1. 迁移剩余的 UseCases
- 将 `FixHtmlUseCase.kt` 从 `app` 模块重新安置到 `shared` 模块。
- **KMP 适配**: 重构 `FixHtmlUseCase` 以消除 Android 特定的依赖项（例如 `android.content.Context`, `android.util.Log`）。对于 Coil 图片加载，将 Android 的 `Context` 依赖替换为 Coil 的 KMP `PlatformContext`，或者调整 `HtmlImageLoader` 接口以避免显式要求平台上下文。

### 2. ViewModel KMP 兼容性
- 确保 `shared` 模块的 `build.gradle.kts` 中包含了 JetBrains KMP Lifecycle 库（`org.jetbrains.androidx.lifecycle:lifecycle-viewmodel`）。
- 验证所有 ViewModels 是否使用 KMP 兼容的 `androidx.lifecycle.ViewModel` 和 `viewModelScope`（包名保持一致，确保无缝迁移）。

### 3. 将 ViewModels 移动到 `shared`
- 将所有 9 个 ViewModel 类（例如 `LoginViewModel.kt`, `SettingsViewModel.kt`, `WriteTopicViewModel.kt` 等）从 `app/.../ui/*/` 重新安置到 `shared/.../ui/*/` 中的相应目录下。
- 根据需要更新包声明和导入。

### 4. 修复 ViewModel 依赖项
- 分析每个已迁移的 ViewModel，查找残留的 Android 特定依赖项（例如 `Context`, `Application`, `Toast` 等）。
- 将任何 Android 特定的依赖项替换为适当的 KMP 抽象（例如，直接传递所需数据或使用 `expect/actual` 机制）。
- 确保 ViewModels 的结构支持在共享模块中通过 Koin 进行依赖注入（例如，在 KMP 中使用 `viewModelOf`）。

## 验证与测试
- **Android 构建**: 执行 `./gradlew assembleDebug` 以确保 Android 应用程序继续正确编译和运行。
- **iOS 构建**: 执行 `./gradlew linkDebugFrameworkIosSimulatorArm64` 以验证 iOS 框架的 KMP 兼容性。