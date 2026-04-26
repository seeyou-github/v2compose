# Phase 3 平台能力边界收敛方案

## Summary

- 目标是把当前“零散 handler + 独立 `openExternalUri` + `MainPlatformDelegate`
  混合职责”收敛成稳定的平台边界，同时不重做现有共享 `WebView` / Google 登录页面。
- 本轮采用你选的“边界收敛”路线：优先整理接口与职责，不做更大范围的平台重写。
- 实施建议按 3 个提交点推进：UI 平台桥接收敛、ViewModel 平台服务拆分、htmlText/外链策略统一与测试补齐。

## Key Changes

- 收敛共享根入口：
    - 修改 `SharedApp` 和 `rememberV2AppState`，移除单独的 `openExternalUri` 参数，外部跳转统一从平台桥接对象获取。
    - Android/iOS 宿主都只向 `SharedApp` 提供一个平台桥接 provider。
    - Android 侧 `rememberAndroidAppPlatformHandlers()` 及相关浏览器/分享实现迁入
      `shared/src/androidMain`，`app` 模块继续只做宿主启动与 Android 专属初始化。
- 重构 UI 平台桥接：
    - 保留 `LocalAppPlatformHandlers` 作为 Compose 注入入口，但把内部字段拆成明确接口，不再继续堆平铺
      lambda。
    - 固定拆成 5 个能力面：
        - `ExternalNavigator`: `openExternalUri(uri: String)`
        - `ShareLauncher`: `shareContent(title: String, url: String)`
        - `ImageSaver`: `saveImage(url: String)`
        - `SettingsLauncher`: `openAppSettings()` / `openNotificationSettings()`
        - `NotificationAccess`: `hasNotificationPermission()` / `isAutoCheckInChannelEnabled()`
    - `PlatformCapabilities` 继续只承担 UI 门禁与降级开关，不再新增行为型布尔值。
    - 从桥接对象中删除冗余项：
        - `copyToClipboard` 删除，统一继续用 Compose `LocalClipboardManager`
        - `requestNotificationPermission` 删除，权限申请继续走现有
          `checkAndRequestNotificationPermission` expect/actual
- 拆分 ViewModel 侧平台服务：
    - 废弃 `MainPlatformDelegate`，改成两个接口并分别注入 `MainViewModel`：
        - `AutoCheckInScheduler`
        - `WebViewProxyController`
    - Android 提供真实实现；iOS 提供 no-op。
    - `MainViewModel` 只负责编排，不再依赖一个混合职责 delegate。
- 明确保留已成熟的服务边界：
    - `CookieManager` 继续作为 cookie/session 清理接口，不再新增第二套封装。
    - `KLogger` 继续作为跨平台日志入口，不引入新的 logging facade。
    - 图片缓存目录继续由各平台 `platformModule` 内部管理；共享层只依赖 `ImageLoader` / `ImageSaver`
      ，不暴露原始 cache path。
- 统一外链/网页/视频策略：
    - `AppNavigationAction.External` 作为共享层唯一外部跳转出口；外域链接、系统 scheme、显式“在浏览器打开”都走
      `ExternalNavigator`。
    - `TopicScreen` 去掉 `LocalUriHandler` 直开浏览器，改走同一平台桥接。
    - `htmlText` 不依赖 `shared`；把 `YouTubePlayer` 的外跳动作显式参数化，复用 `HtmlText` 现有
      `onLinkClick` 链路，让 iOS YouTube 降级也走同一套外链判定。
    - 共享 `webview` 路由和 `GoogleLoginScreen` 保持现有页面形状；本阶段不切到原生 OAuth/URL scheme
      callback，登录回跳继续以 `RedirectEvent` + `resolveRedirectLocation` 为唯一共享契约。
- Android-only 边界保持不变：
    - WorkManager 自动签到、通知中心、WebView 代理、Firebase/Crashlytics 继续留在 Android 侧。
    - iOS v1 不补后台任务、复杂 WebView 交互、Android 风格代理链路。

## Public APIs / Interfaces / Types

- `SharedApp` 改成只接收平台桥接 provider，不再额外接收 `openExternalUri`。
- `AppPlatformHandlers` 保留名称，但内部改为持有 5 个明确接口和 `PlatformCapabilities`。
- 删除 `AppPlatformHandlers.copyToClipboard` 与 `AppPlatformHandlers.requestNotificationPermission`。
- 删除 `MainPlatformDelegate`，新增 `AutoCheckInScheduler` 与 `WebViewProxyController`。
- `htmlText` 的 `YouTubePlayer` expect/actual 改成接收外跳回调。

## Test Plan

- 单元测试：
    - 更新 `MainViewModelTest`，分别验证 `AutoCheckInScheduler` 和 `WebViewProxyController` 的调用条件。
    - 补共享导航测试，覆盖“显式在浏览器打开”和外域链接都落到 `AppNavigationAction.External`。
    - 为新的平台桥接 fake 增加最小测试，确认分享、保存图片、打开设置、通知状态查询都能被共享层调用。
- 编译验证：
    - `./gradlew :shared:compileKotlinIosSimulatorArm64`
    - `./gradlew :htmlText:compileKotlinIosSimulatorArm64`
    - `./gradlew :app:compileFossDebugKotlin`
- 手工回归：
    - Android/iOS：Topic 菜单“分享 / 在浏览器打开”可用。
    - iOS：Topic HTML 中 YouTube 降级按钮走外部浏览器，不再直接依赖模块内 `LocalUriHandler`。
    - Android：自动签到和 WebView 代理行为不回退；设置页自动签到入口与通知联动保持原状。
    - Android/iOS：Google 登录现有 WebView 流程仍能进入并完成共享回跳。

## Assumptions

- 阶段 3 只做“边界收敛”，不做更激进的原生登录/原生 Web 容器重写。
- `PlatformCapabilities` 只做 UI 能力门禁；行为型平台差异一律走接口。
- `htmlText` 与 `shared` 保持单向依赖，不为了统一外链而引入循环依赖。
