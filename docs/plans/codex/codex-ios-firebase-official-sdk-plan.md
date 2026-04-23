# iOS 原生 Firebase Analytics + Crashlytics 接入计划

## Summary

选型固定为：`iOS 15 + Swift Package Manager + 原生宿主接入`，不把 Firebase SDK 拉进 KMP `shared` 层。  
原因是当前 Firebase Apple 官方文档已把新接入基线收敛到 iOS 15，并推荐 SPM；Crashlytics 的 dSYM 上传链路也以 Xcode/SPM 为中心设计。当前本机 `Xcode 26.4` 满足官方工具链要求。  
本次同时采用的工程形态为：`iOS 仅保留 Firebase 版单构建线`，不新增 `foss/google` 双 target 或双 scheme。

参考：
- [Firebase Apple setup](https://firebase.google.com/docs/ios/setup)
- [Firebase Crashlytics for Apple](https://firebase.google.com/docs/crashlytics/ios/get-started)

## Key Changes

### 1. iOS 工程与依赖管理

- 以 `iosApp/project.yml` 为 iOS 工程真源，新增 Firebase SPM package 与 target 依赖，然后用 XcodeGen 回生成 `iosApp.xcodeproj/project.pbxproj`，避免手改 pbxproj 漂移。
- iOS deployment target 从 `14.0` 提升到 `15.0`，同步更新 `project.yml` 与 `iosApp/Configuration/Config.xcconfig`。
- 为主 target 加入 `FirebaseAnalytics` 与 `FirebaseCrashlytics`，并补上 `OTHER_LDFLAGS += -ObjC`。
- 保持现有 `shared.framework` direct integration，不改 KMP 框架导出方式。

### 2. 原生启动与职责边界

- Firebase 只放在 `iosApp/iosApp/` 原生壳内初始化，不改 `shared` 的 DI 结构，也不新增 KMP 对 Firebase 的编译期依赖。
- 在现有 `AppDelegate` 启动链中加入单一原生 bootstrap，负责：
  - `FirebaseApp.configure()`
  - Debug 构建关闭 Analytics collection，以对齐 Android 现在的 debug 行为
  - 预留 Crashlytics 测试触发 helper，但不把测试崩溃按钮留到正式 UI
- 共享层继续保持 vendor-agnostic；后续若真要把“埋点开关/事件记录”提升到共享层，再单独设计桥接，不在这次顺手塞进去。

### 3. Crashlytics 符号与配置文件链路

- 按官方要求在 iOS target 末尾追加 Crashlytics run script，并确保它是最后一个 Build Phase。
- 同步配置 dSYM 生成与 run script Input Files，至少包含：
  - dSYM 目录与主二进制
  - 打包后的 `GoogleService-Info.plist`
  - 可执行文件路径
- 继续沿用当前工程里的 `ENABLE_USER_SCRIPT_SANDBOXING = NO`，因此不需要额外处理开启 sandbox 后的 debug dylib 输入项。
- Firebase Console 侧为现有 bundle id `io.github.v2compose.iosApp` 注册 iOS App，并把 `GoogleService-Info.plist` 放到固定 iOS 宿主路径。
- 因为采用单构建线，这个 plist 对 iOS 构建是必需品；仓库侧补充 ignore 规则与文档说明，但默认不提供 Firebase-off 兜底。

### 4. 文档与仓库约束

- 将本计划归档到 `docs/plans/codex/`。
- README 的构建说明补齐 iOS 部分：
  - 需要在 Firebase Console 注册 iOS App
  - 本地提供 `GoogleService-Info.plist`
  - 通过 XcodeGen 生成工程后再构建
- 增加 iOS 侧对 `GoogleService-Info.plist` 的 `.gitignore` 规则，和 Android `google-services.json` 的处理方式保持一致的“本地自备、仓库不跟踪”原则。

## Public APIs / Interfaces / Types

- 不新增 `shared` 公共 API，不改 `IAnalytics` 或 Koin shared graph。
- 新增的仅是 iOS 原生内部 bootstrap 辅助类型/函数，用于隔离 Firebase 初始化与 debug collection 控制；它不作为跨平台接口暴露。
- iOS 工程层新增 SPM package 声明、run script phase 和配置文件约束，这些属于宿主工程接口变更，不影响 Android/KMP 对外接口。

## Test Plan

- 工程生成与编译
  - `xcodegen generate --spec iosApp/project.yml` 成功
  - `./gradlew :shared:compileKotlinIosSimulatorArm64` 成功
  - `./gradlew :androidApp:assembleFossDebug` 继续成功
  - iOS Debug 模拟器构建成功，SPM 依赖解析正常
- iOS 启动与运行
  - App 首次启动不因 Firebase 初始化崩溃
  - `FirebaseApp.configure()` 仅执行一次，不受 SwiftUI 重建影响
  - Debug 下 Analytics collection 关闭；Release 保持默认开启
- Crashlytics 验证
  - 强制触发一次测试崩溃后，Crashlytics 控制台可见首条 iOS 崩溃
  - dSYM 自动上传成功，没有 “Missing dSYM” 或 run script 输入文件缺失问题
  - Analytics 已启用时，Crashlytics breadcrumb logs 可见
- 回归
  - Android `foss/google` 现有构建逻辑不被影响
  - iOS 现有后台任务注册、Compose 宿主启动、共享首页加载保持正常

## Assumptions

- 官方当前基线按 `2026-04-23` 可用文档执行：Apple 平台 Firebase 新接入要求 `iOS 15+`，并推荐 `Swift Package Manager`。
- iOS 本轮不做 `foss/google` 双变体；所有 iOS 构建默认依赖 Firebase SDK 与本地 `GoogleService-Info.plist`。
- `GoogleService-Info.plist` 仍视为仓库外的本地配置文件，不提交到版本库；缺失时 iOS 构建或运行失败属于预期。
- 这次目标是“iOS 原生官方 SDK 正式接通并可验证上报”，不是顺手重构共享埋点体系。
