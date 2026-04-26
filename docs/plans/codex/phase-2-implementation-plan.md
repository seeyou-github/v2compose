# Phase 2 补剩余缺口

## Summary

- 现有代码已经具备阶段 2 的大部分基线：共享根入口、共享导航、iOS 宿主、第一波/第二波页面基本都在
  `shared`，且我已确认 `./gradlew :shared:compileKotlinIosSimulatorArm64` 和
  `./gradlew :app:compileFossDebugKotlin` 当前可通过。
- 这次不重做阶段 2，只补未闭环的部分：平台能力边界、iOS 明确降级、核心浏览验收链路。
- iOS 策略固定为：隐藏 Android 语义的“自动签到”入口，不做伪等价；保留现有前台浏览链路。

## Key Changes

- 在 `shared/commonMain` 新增统一的 `PlatformCapabilities` 类型，并由 Android/iOS 的 `platformModule`
  提供。
- `PlatformCapabilities` 固定包含两个能力位：
    - `supportsAutoCheckIn`
    - `supportsEmbeddedYouTube`
- Android 取值固定为 `true/true`；iOS 取值固定为 `false/false`。
-
扩展 [AppPlatformHandlers.kt](/Users/cooaer/Developer/myself/V2compose/shared/src/commonMain/kotlin/io/github/v2compose/AppPlatformHandlers.kt)：
    - 新增 `capabilities: PlatformCapabilities`
    - Android/iOS 两侧 `remember...AppPlatformHandlers()` 都要传入各自能力值
-
修改 [SettingsScreen.kt](/Users/cooaer/Developer/myself/V2compose/shared/src/commonMain/kotlin/io/github/v2compose/ui/settings/SettingsScreen.kt)：
    - 当 `supportsAutoCheckIn == false` 时，整个“自动签到”设置项不渲染
    - 不保留 iOS 上的通知权限/通知渠道伪语义
-
修改 [MainViewModel.kt](/Users/cooaer/Developer/myself/V2compose/shared/src/commonMain/kotlin/io/github/v2compose/ui/main/MainViewModel.kt)：
    - 注入 `PlatformCapabilities`
    - `listenCanCheckIn()` 与 `listenAutoCheckIn()` 在 `supportsAutoCheckIn == false` 时直接不启动
    - iOS 上禁止共享层在启动时触发自动签到网络行为，也不调用平台同步逻辑
- 保留 Mine 页里的前台手动签到入口，不在本轮移除；它不属于“自动签到/后台能力”范畴。
-
修改 [PlatformActuals.kt](/Users/cooaer/Developer/myself/V2compose/htmlText/src/iosMain/kotlin/io/github/cooaer/htmltext/PlatformActuals.kt)：
    - 去掉 `YouTube Player (iOS placeholder)` 纯占位文案
    - 改为明确降级 UI：说明 iOS v1 不支持内嵌播放，并提供“在浏览器打开”行为
- 不改现有路由形状，不重整 `SharedApp`/`SharedAppNavGraph`/iOS 宿主结构；现有 shared 页面集合作为阶段
  2 既定基线继续沿用。

## Public APIs / Interfaces / Types

- 新增 `PlatformCapabilities` 公共类型。
- `AppPlatformHandlers` 新增 `capabilities` 字段。
- `MainViewModel` 构造参数新增 `PlatformCapabilities`。
- Android/iOS `platformModule` 都要注册 `PlatformCapabilities`。
- 现有导航 API、页面参数、`AppNavigation` 规则保持兼容，不改 route shape。

## Test Plan

- 新增 `shared/src/commonTest`：
    - 覆盖 `resolveOpenUri` 与 `resolveRedirectLocation`
    - 用例至少包含 `/t`、`/go`、`/member`、外部域名、`mailto/sms/tel`
- 为 `MainViewModel` 增加 common unit test：
    - `supportsAutoCheckIn=false` 时，不触发 `checkIn()`，也不调用平台同步
    - `supportsAutoCheckIn=true` 时，维持现有 Android 行为
- 构建验证：
    - `./gradlew :shared:compileKotlinIosSimulatorArm64`
    - `./gradlew :app:compileFossDebugKotlin`
- 手工冒烟：
    - Android/iOS：Home -> Topic，Node -> Topic，User -> Topic/Node，Search -> Topic，Login -> Main
    - iOS：设置页不再出现自动签到；Topic HTML 中 YouTube 内容走显式外跳降级
    - Android：自动签到入口和原有行为保持不变

## Assumptions

- 当前 `SharedApp`、iOS 宿主、共享 Koin 图和已迁页面视为可接受基线，不在本轮重做。
- 本轮不处理 phase 3 范围：WorkManager 等价、代理等价、Firebase/Crashlytics、广义平台服务重构。
- iOS v1 对不支持能力的默认策略是“隐藏入口或显式降级”，不是保留占位伪支持。
