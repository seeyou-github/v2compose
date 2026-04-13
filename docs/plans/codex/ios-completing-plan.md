# iOS 平台特性补齐研究方案

## Summary

- 当前 iOS 侧的缺口是有意留下的 v1 降级，不是零散
  bug。现状集中在 [IosAppHandlers.kt](/Users/cooaer/Developer/myself/V2compose/shared/src/iosMain/kotlin/io/github/v2compose/IosAppHandlers.kt)、[PlatformModule.kt](/Users/cooaer/Developer/myself/V2compose/shared/src/iosMain/kotlin/io/github/v2compose/di/PlatformModule.kt)、[iOSApp.swift](/Users/cooaer/Developer/myself/V2compose/iosApp/iosApp/iOSApp.swift)
  这三处。
- 推荐按 4 个能力分层处理，而不是一口气追求“安卓一比一”：
    1. 低风险可补齐：通知授权请求、保存图片到系统相册、HTTP 缓存管理。
    2. 中风险可实现：自动签到后台调度。
    3. 高成本但可做：Ktor API 请求代理。
    4. 明确不做等价：`WKWebView` 代理。
- 结论上，iOS 可以做“系统原生等价”而不是“Android 机制照抄”：
    - 自动签到应走 `BackgroundTasks`，不是 Android 式通知前台工作链路。
    - 网络代理可作用于 `URLSession`/Ktor，但不能承诺覆盖 `WKWebView`。
    - 图片保存应走 Photos 写入，而不是继续弹分享面板假装保存。

## Key Changes

### 1. 修正共享层抽象边界

- 保留 `AppPlatformHandlers`、`AutoCheckInScheduler`、`WebViewProxyController` 这组边界，不回退到“把
  iOS API 直接塞进 commonMain”。
- 但要拆掉一个现有语义错误：共享设置页把“自动签到可启用”错误地绑定到了“通知权限是否已授权”。
- 推荐新增一个专门的自动签到前置能力接口，例如 `AutoCheckInPrerequisite` 或等价结果类型，替代当前
  `NotificationAccess` 在自动签到开关中的职责。
- Android 实现继续检查通知权限与 channel；iOS 实现直接允许开启自动签到，不把通知权限当成前提条件。
- `NotificationAccess` 保留给真正的通知能力查询，不再承担“自动签到能不能开”的跨平台判定。

### 2. 图片保存改为真实相册写入

- iOS `ImageSaver` 改为真实保存到 Photos：
    - 先申请 `PHPhotoLibrary.requestAuthorization(for: .addOnly)`。
    - 成功后通过 `PHAssetChangeRequest.creationRequestForAsset(...)` 或文件 URL 方式写入。
- `Info.plist` 增加 `NSPhotoLibraryAddUsageDescription`。
- 成功/失败仍通过现有 `SnackbarHostState` 回传，不改共享 UI 调用方式。
- 若要尽量保留图片元数据，优先走文件 URL 写入；直接 `UIImage` 写入会丢失部分原始 metadata，这是官方文档明确提醒的。

### 3. 通知权限实现改为真实请求，而不是 iOS 假装已授权

- 当前 iOS 的 `checkAndRequestNotificationPermission` 是空实现，应改为调用
  `UNUserNotificationCenter.requestAuthorization(...)`。
- `IosAppHandlers.hasNotificationPermission()` 继续读取真实系统状态，这部分当前方向是对的。
- 但该授权只服务于“是否允许本地通知/结果提醒”，不再绑定自动签到主开关。
- 如果后续要给自动签到补本地通知结果，再在 iOS 端单独调度 `UNUserNotificationCenter.add(...)`
  ；没有通知权限时只是不提醒，不应阻止后台签到本身。

### 4. 自动签到后台调度采用 `BackgroundTasks`

- iOS 宿主层增加 `AppDelegate`，通过 `@UIApplicationDelegateAdaptor` 挂到 SwiftUI `App`
  上；原因很简单：当前项目目标是 iOS 14，走 `AppDelegate + BGTaskScheduler` 最稳，不赌更新的 SwiftUI
  scene API。
- `Info.plist` 增加：
    - `BGTaskSchedulerPermittedIdentifiers`
    - `UIBackgroundModes` 中的 `fetch`
- 新增一个共享导出的 iOS 背景入口，职责固定为：
    - `initIosRuntime()` 的可重入初始化
    - 解析自动签到任务
    - 调用 `CheckInUseCase`
    - 完成后重新提交下一次 `BGAppRefreshTaskRequest`
- `IosAutoCheckInScheduler.syncAutoCheckIn(enabled)` 负责：
    - `enabled = true` 时提交或覆盖已有 `BGAppRefreshTaskRequest`
    - `enabled = false` 时取消对应 task request
- `PlatformCapabilities.Ios.supportsAutoCheckIn` 只有在这一链路闭合后再切到 `true`；不要先把开关放出来，再让用户点进
  no-op。
- 这是“最佳努力”能力，不承诺 Android WorkManager 那种频率和准点性；UI 文案应明确 iOS 由系统决定调度时机。

### 5. HTTP 缓存应补齐，成本低且收益直接

- iOS `HttpCacheManager` 不应继续是 no-op。
- Darwin/Ktor 会落到 `URLSession`，直接在 session 配置里挂 `URLCache` 即可：
    - `NSURLSessionConfiguration.urlCache`
    - 默认缓存策略保持 `useProtocolCachePolicy`
- `HttpCacheManager.size` 读取 `URLCache.currentDiskUsage`。
- `clear()` 调用 `removeAllCachedResponses()`。
- 这项变更不影响共享 API，只补齐设置页“缓存大小/清理缓存”在 iOS 上的真实行为。

### 6. 代理能力要拆成两条线处理

- Ktor API 请求代理：可做。
    - 官方和 Ktor 源码都表明 Darwin `URLSession` 支持 `connectionProxyDictionary`，Ktor Darwin
      引擎也会在创建 session 时写入代理配置。
    - 但当前仓库把 `HttpClient` 作为 Koin 单例注入给 `V2exApi/GithubApi`，而
      `URLSessionConfiguration` 在 session 创建后就不可变，所以运行时改代理不能像 Android
      `ProxySelector` 一样“热生效”。
    - 推荐方案是引入一个 iOS 专用的 `NetworkClientRegistry`/`HttpClientProvider`，让
      `ProxyManager.updateProxy()` 触发客户端重建，而不是尝试热改现有 singleton。
- `WKWebView` 代理：不要承诺。
    - 这是基于官方能力边界和当前 `compose-webview-multiplatform` iOS 实现的推断：现有链路只创建
      `WKWebViewConfiguration`，没有稳定的 per-webview 代理注入面。
    - 因此 `IosWebViewProxyController` 可以继续 no-op，但要把限制显式写清楚。
    - 如果启用 iOS 代理设置，文案必须写成“仅作用于 API 请求，不作用于内嵌
      WebView/登录页”；否则用户会以为代理失效，实际上是产品文案在骗人。

## Test Plan

- 共享层回归：
    - `MainViewModelTest` 增加 iOS 自动签到开启路径，不再依赖通知权限前置。
    - `AppPlatformHandlersTest` 增加 iOS 图片保存/通知请求结果分支的 fake 覆盖。
- Android 回归：
    - `./gradlew :app:assembleFossDebug`
    - `./gradlew :shared:test`
- iOS/KMP 编译：
    - `./gradlew :shared:compileKotlinIosSimulatorArm64`
    -
    `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator -configuration Debug build`
- iOS 手工验收：
    - 图片查看页点击“保存图片”，首次弹 Photos 写入授权，授权后真实入相册。
    - 设置页清理缓存后，缓存大小归零。
    - 自动签到开关开启后，任务被注册并可通过开发态触发 `BGTaskScheduler` 模拟执行。
    - 代理设置修改后，Ktor API 请求走新代理；WebView/登录页继续直连，并有明确文案说明。

## Assumptions

- 本轮只做研究方案，不直接落代码。
- iOS 部署目标保持 `14.0`，因此后台任务方案基于 `AppDelegate + BackgroundTasks`，不引入更激进的宿主改造。
- iOS 自动签到默认不依赖通知权限；如果未来要做“签到结果通知”，那是附加能力，不是主开关前置条件。
- `WKWebView` 代理不可作为承诺能力；这里是基于 Apple/库实现边界的推断，不是仓库里已经验证过的事实。
- 如果业务上“代理必须覆盖登录 WebView”是硬要求，那 iOS 代理功能应整体继续隐藏，而不是上线半套实现。

## 参考依据

- Apple `BGTaskScheduler` 与 `Using background tasks to update your app`：支持注册、提交、取消后台任务，请求类型应使用
  `BGAppRefreshTaskRequest`。
    - <https://developer.apple.com/documentation/backgroundtasks/bgtaskscheduler>
    - <https://developer.apple.com/documentation/uikit/using-background-tasks-to-update-your-app>
- Apple `BGAppRefreshTask`：说明 app refresh 适用于短任务，且需要 `fetch` 背景模式。
    - <https://developer.apple.com/documentation/backgroundtasks/bgapprefreshtask>
- Apple `UNUserNotificationCenter.requestAuthorization(...)`：通知权限应显式请求，且后续请求不会再次弹窗。
    - <https://developer.apple.com/documentation/usernotifications/unusernotificationcenter/requestauthorization(options:completionhandler:)>
- Apple Photos：`PHAccessLevel.addOnly` 适合“只保存到相册”，
  `PHAssetChangeRequest.creationRequestForAsset(from:)` 用于创建新图片资源。
    - <https://developer.apple.com/documentation/photos/phaccesslevel>
    - <https://developer.apple.com/documentation/photos/phassetchangerequest/creationrequestforasset(from:)>
- Apple `URLSessionConfiguration` / `URLCache`：iOS 网络层支持 `urlCache` 与
  `connectionProxyDictionary`，且 session 初始化后配置按副本生效，因此代理变更需要重建 session/client。
    - <https://developer.apple.com/documentation/foundation/urlsessionconfiguration>
    - <https://developer.apple.com/documentation/foundation/nsurlsessionconfiguration/1411499-connectionproxydictionary>
    - <https://developer.apple.com/documentation/foundation/urlsession/configuration>
    - <https://developer.apple.com/documentation/foundation/urlcache/removeallcachedresponses()>
