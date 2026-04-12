# Phase 4 收尾与收敛实施方案

## Summary

- 本轮按“完整收口 + 保守兼容”执行：不重做 `SharedApp`/`V2App`/`MainViewController`
  的对外形状，只清理确认属于双轨迁移遗留的薄层和重复注入，并补齐阶段 4 缺失文档。
- 代码目标不是继续搬页面；当前仓库里共享入口、iOS 宿主、平台 handler、平台能力边界都已存在。阶段 4
  应聚焦在“减少迁移噪音、固定边界、明确 iOS v1 能力矩阵、形成后续迁移模板”。
- 实施建议拆成 2 个提交：
    1. 代码收尾与依赖图收敛
    2. 阶段 4 文档与总计划回填

## Key Changes

- 收敛 DI 中仍带有“迁移过渡味道”的模块拆分：
    - 将 `PlatformCapabilities` 绑定并入各平台 `platformModule`，删除单独的
      `platformCapabilitiesModule` expect/actual 文件和 `sharedModules()` 里的额外装配位。
    - 保持 `MainViewModel` 继续直接注入 `PlatformCapabilities`，不改 ViewModel 构造形状，只减少一层无意义模块壳。
- 保留宿主薄壳，不做误删：
    - Android 继续保留 `app/src/main/kotlin/io/github/v2compose/V2App.kt`
      作为宿主包装层；它已足够薄，不属于需要下线的重复页面实现。
    - iOS 继续保留 `shared/src/iosMain/kotlin/io/github/v2compose/MainViewController.kt` 作为稳定导出入口。
    - 不移除 `PlatformContext`、`AutoCheckInScheduler`、`WebViewProxyController`；它们当前仍是有效的平台边界，不是双轨残留。
- 文档补齐为单一阶段文档，避免散落：
    - 新增 `docs/plans/codex/phase-4.md`，包含 4 个固定部分：
        - 模块职责图：`app`、`shared`、`htmlText`、`iosApp` 的职责边界
        - 平台能力接口清单：`AppPlatformHandlers`、`PlatformCapabilities`、`AutoCheckInScheduler`、
          `WebViewProxyController`、`PlatformContext` 的职责和使用边界
        - iOS 首版功能矩阵：已支持、降级支持、不支持三类，至少覆盖浏览、搜索、主题、用户、登录、分享、图片保存、通知/自动签到、YouTube
          内嵌
        - 新页面迁移模板：页面、ViewModel、导航、依赖注入、平台差异、验收项的最小清单
    - 回写 `docs/plans/codex/Compose Multiplatform Migration Plan.md`：
        - 为阶段 4 增加落地文档引用
        - 明确当前仓库事实：`app` 已无大块重复 UI，阶段 4 以收口和文档为主
- 不在本轮引入新架构决策：
    - 不替换导航栈
    - 不重命名共享根入口
    - 不扩大 iOS v1 范围
    - 不把 Android-only 能力继续下沉到 commonMain

## Public APIs / Interfaces

- 保持这些外部入口不变：
    - `SharedApp(...)`
    - `V2App()`
    - `MainViewController()`
    - `initKoin(...)`
- 内部接口调整仅限依赖图收口：
    - `PlatformCapabilities` 仍保留类型和注入方式，但其注册来源改为 `platformModule`
    - 删除 `platformCapabilitiesModule` 这一独立模块概念，避免未来继续把“平台能力声明”和“平台实现注册”拆成两层

## Test Plan

- 共享测试：
    - `./gradlew :shared:test`
- Android 构建：
    - `./gradlew :app:assembleFossDebug`
    - `./gradlew :shared:compileDebugKotlinAndroid`
    - `./gradlew :htmlText:compileDebugKotlinAndroid`
- iOS/KMP 编译：
    - `./gradlew :shared:compileKotlinIosSimulatorArm64`
    - `./gradlew :htmlText:compileKotlinIosSimulatorArm64`
- iOS 宿主验证：
    -
    `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator -configuration Debug build`
- 回归关注点：
    - `MainViewModel` 在 Android/iOS 能力差异下的自动签到与代理同步逻辑
    - `SettingsScreen` 的平台能力显隐
    - `TopicScreen` / `GalleryImage` / 分享与外链跳转仍走 `LocalAppPlatformHandlers`
    - iOS 内嵌 YouTube 降级语义与文档矩阵一致

## Assumptions

- 当前 `app` 模块中的 `V2App` 仅是合理宿主薄壳，不视为“旧实现重复页面”，默认不删除。
- 当前最明确的代码收尾项是 DI 模块收口；其余平台边界更多是文档固化，而不是继续删接口。
- 阶段 4 文档采用单文件汇总，避免再拆成多篇造成维护分散。
- 若实现时发现 `platformCapabilitiesModule`
  被外部代码或测试以非预期方式直接依赖，则退回为“保留类型、仅在文档中声明其将被废弃”，但默认先按删除独立模块执行。
