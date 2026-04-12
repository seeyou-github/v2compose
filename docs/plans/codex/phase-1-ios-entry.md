# Phase 1: 新增 iOS 宿主工程

## Summary

基于当前仓库现状，这一段按 `Direct Xcode + SwiftUI thin shell + iOS 14` 落地，不引入 CocoaPods/SwiftPM
额外链路。  
共享层已具备 `SharedApp`、共享导航和共享 ViewModel/Koin 骨架，所以本次实现目标不是重做 UI，而是把 iOS
宿主、iOS 入口导出和 iOS 平台依赖一次接通，做到“可启动、可浏览、不因平台注入缺口崩溃”。

参考依据：
[Kotlin Multiplatform Create your Compose Multiplatform app](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-create-first-app.html)  
[Kotlin Multiplatform Integration with the SwiftUI framework](https://kotlinlang.org/docs/multiplatform/compose-swiftui-integration.html)

## Key Changes

### 1. 新增标准 `iosApp` 宿主工程

- 在仓库根目录新增 `iosApp/`，采用官方 KMP iOS 宿主结构。
- 宿主使用 SwiftUI `@main` 作为最薄壳，根页面只负责承载 Kotlin 导出的 `MainViewController()`。
- 工程采用 direct integration，本地链接 `shared` framework，不引入 CocoaPods。
- 宿主侧保留配置文件入口，例如 `Configuration/Config.xcconfig`，把 Team ID、Bundle ID、显示名留在 Xcode
  配置层，不写死在 Kotlin。
- 默认 Bundle ID 采用 `io.github.v2compose.iosApp`，后续如需上架再改。

### 2. 在 `shared/src/iosMain` 导出 iOS 入口

- 新增 `MainViewController.kt`，暴露稳定 API：
    - `fun MainViewController(): UIViewController`
- `MainViewController()` 内部统一完成一次性启动逻辑：
    - 初始化 Koin；若已启动则直接复用，避免 SwiftUI 重建导致重复 `startKoin()`
    - 初始化 `SingletonImageLoader`
    - 创建 iOS 版 `AppPlatformHandlers`
    - 调用现有 `SharedApp(...)`
- 不改 `SharedApp` 的外部职责；Android/iOS 都继续把它当共享根入口。

### 3. 补齐 iOS `platformModule`

- 将 `shared/src/iosMain/kotlin/io/github/v2compose/di/PlatformModule.kt` 从 TODO 补成可运行模块，确保
  shared Koin 图在 iOS 上完整闭合。
- iOS 平台模块必须提供以下依赖：
    - `PlatformContext` 的 iOS 实例，用于 DataStore 构造
    - `MainPlatformDelegate`：iOS v1 使用 no-op 实现，`syncAutoCheckIn()` 和 `updateWebViewProxy()`
      不做后台/代理副作用
    - `Fruit`
    - named `HttpClient`：`V2HttpClient`、`ImageHttpClient`、`GithubHttpClient`
    - `CookieManager`：基于 `NSHTTPCookieStorage` 的真实实现，`logout()` 时必须清 cookie，不能 no-op
    - `ProxyManager`：iOS v1 no-op 实现，仅持有设置值，不尝试改系统代理
    - `HttpCacheManager`：iOS v1 可用 no-op 或轻量实现，但接口必须可注入、可调用
    - `DiskCache`、`ImageLoader`
    - `FixHtmlUseCase` / `HtmlImageLoader`
- `ImageLoader` 与 `DiskCache` 使用 iOS cache 目录，避免把图片缓存落到 Documents。
- `FixHtmlUseCase` 依赖的 `coil3.PlatformContext` 必须在 iOS 明确注入，不能假设 Koin 自动推断。

### 4. 提供 iOS 平台交互降级策略

- 新增 iOS 版平台 handlers，满足 `SharedApp` 当前依赖面：
    - `openExternalUri`：调用 `UIApplication.openURL`
    - `shareContent`：桥接 `UIActivityViewController`
    - `copyToClipboard`：桥接 `UIPasteboard`
    - `saveImage`：iOS v1 先实现为系统分享/保存到相册的受控路径，保证按钮可用且不崩
    - `openAppSettings`：跳系统设置页
    - `openNotificationSettings` / `requestNotificationPermission`：统一跳系统设置或请求权限
    - `checkNotificationPermission`：真实读取通知授权状态
    - `isAutoCheckInChannelEnabled`：iOS 固定返回 `true`，避免 Android 通知 channel 语义泄漏
- 共享 UI 不新增平台分支；iOS 通过 handler 语义降级兜底。

### 5. 边界与已知限制

- iOS 宿主本轮只负责把共享壳跑起来，不处理 Android-only 能力等价实现：
    - WorkManager 自动签到
    - WebView 代理配置
    - Android 通知 channel
    - Firebase/Crashlytics
- `shared` 现有 `webViewScreen`、设置页、HTML 图片加载必须在 iOS 至少做到“可进入、不因依赖缺失崩溃”。
- 若个别能力无法在本轮达到完整可用，优先保证：
    - 页面可打开
    - 操作有明确降级
    - 不隐藏崩溃点

## Public APIs / Interfaces / Types

- 新增 iOS 导出入口：`MainViewController(): UIViewController`
- 如需避免多处散落初始化，新增共享 iOS 启动封装，例如 `initKoinIos()` 或等价内部 bootstrap API，但职责固定为：
    - 一次性启动 Koin
    - 注册 iOS 平台模块
    - 安装 Coil singleton
- 新增 iOS 平台实现类型：
    - `IosMainPlatformDelegate`
    - `IosCookieManager`
    - `IosAppPlatformHandlers` 或 `rememberIosAppPlatformHandlers()`
    - `NoOpProxyManager`
    - `NoOpHttpCacheManager`（若本轮不做真实缓存管理）
- 不改动 `SharedApp` 的主签名，除非实现时发现 iOS 生命周期桥接存在硬阻塞；默认视为保持兼容。

## Test Plan

- 构建验证
    - `:shared:linkDebugFrameworkIosSimulatorArm64` 通过
    - Android 现有构建仍通过，至少 `:app:assembleFossDebug` 通过
    - `iosApp` 可在 iOS 14+ 模拟器启动到共享首页
- iOS 冒烟场景
    - 首页进入成功
    - 主题详情可打开，HTML 内容和图片加载不崩
    - 用户页、搜索页、设置页可进入
    - 外链打开、分享、复制可调用系统能力
    - 登出后 cookie 被清空
- 回归重点
    - Koin 只初始化一次，不因 SwiftUI 重建重复启动
    - `FixHtmlUseCase` 在 iOS 能拿到 `coil3.PlatformContext`
    - 设置页中的通知/自动签到相关操作在 iOS 走降级逻辑，不触发 Android 语义崩溃

## Assumptions

- 宿主形态固定为 `Direct Xcode + SwiftUI thin shell + iOS 14`。
- iOS v1 目标是“共享壳可运行 + 核心浏览可用”，不是功能对齐 Android。
- 通知、后台任务、代理等 Android-only 能力本轮不做等价实现，统一以 no-op 或系统级替代方案降级。
- 若实现中发现 `iosApp` 工程文件由当前 IDE/模板生成结果与计划命名略有差异，以“官方模板产物优先、行为不变”为准。
