# 修复 iOS 外链打开失效

## Summary
- 问题根因已定位：`TopicMenuItem.OpenInBrowser` 在共享层已经正确调用 `LocalAppPlatformHandlers.current.openExternalUri(...)`，Android 端有完整实现，而 iOS 端当前仅调用了过时且过薄的 `UIApplication.sharedApplication.openURL(nsUrl)`，没有做可打开性判断，也没有走现代 `openURL(_:options:completionHandler:)` 风格封装。
- 修复集中在 `shared/src/iosMain/kotlin/io/github/v2compose/IosAppHandlers.kt`，不改 `TopicScreen` 的调用链。

## Key Changes
- 在 iOS 平台处理器中重写 `openUrl(url: String)` 的行为：
  - 继续用 `NSURL.URLWithString(url)` 做 URL 解析，解析失败直接返回。
  - 先用 `UIApplication.sharedApplication.canOpenURL(nsUrl)` 做可打开性判断，避免静默失败。
  - 使用现代打开方式 `openURL(nsUrl, options = emptyMap<Any?, Any>(), completionHandler = ...)`，替代旧的 `openURL(nsUrl)`。
  - 保持 `http/https` 外链与 `UIApplicationOpenSettingsURLString` 共用同一个底层打开方法，避免设置页和浏览器跳转走两套逻辑。
- 保持共享接口不变：
  - `shared/src/commonMain/kotlin/io/github/v2compose/AppPlatformHandlers.kt` 无需改 public API。
  - `shared/src/commonMain/kotlin/io/github/v2compose/ui/topic/TopicScreen.kt` 无需改业务逻辑。
- 增加轻量日志：
  - 当 URL 无法被系统打开或 completion 回调失败时，在 iOS handler 内记录 debug 日志。
  - 不引入新的用户可见提示，保持与 Android 当前行为一致。

## Test Plan
- 单元层：
  - 在 `shared/src/iosTest/kotlin/io/github/v2compose/IosAppHandlersTest.kt` 添加回归测试，覆盖可打开与不可打开两条路径。
- 构建验证：
  - `./gradlew :shared:compileKotlinIosSimulatorArm64`
  - `./gradlew :app:assembleFossDebug`
- 运行验证：
  - 在 iOS Simulator 启动应用，进入任意帖子页。
  - 点击右上菜单中的“Open in Browser”。
  - 预期 Safari 打开 `https://www.v2ex.com/t/<topicId>`。
  - 顺带验证“应用设置”入口仍能跳转，确认共用 `openUrl` 后没有回归。

## Assumptions
- 本次修复目标仅覆盖 iOS 的外链打开失效，不重构 `TopicScreen`、导航层或 `AppPlatformHandlers` 接口。
- iOS 端参考 Android 语义，但不照搬 Custom Tabs / scheme 分流细节；只要求合法 URL 能稳定交给系统打开。
- 完成后执行构建验证并提交一个独立 commit 保存进度。
