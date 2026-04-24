# iOS Debug 包名与环境区分方案

日期：2026-04-24

## 概述

- 保持单一 `iosApp` target 和现有 `Debug/Release` configuration，不新增 iOS flavor、target 或 scheme。
- 将 `iosApp/Configuration/Config.xcconfig` 拆分为 `Base.xcconfig`、`Debug.xcconfig`、`Release.xcconfig`。
- `Debug` 使用独立 Bundle ID `io.github.v2compose.iosApp.debug`，`Release` 保持 `io.github.v2compose.iosApp`。
- Debug 与 Release 均保留 Firebase，但使用各自独立的本地 plist。
- 桌面名称保持 `V2X`，通过单独的 Debug 图标区分开发环境与正式环境。

## 实施要点

### 构建配置

- `iosApp/project.yml` 的 `configFiles` 分别指向 `Configuration/Debug.xcconfig` 与 `Configuration/Release.xcconfig`。
- `ASSETCATALOG_COMPILER_APPICON_NAME` 改为使用 `$(APP_ICON_NAME)`。
- 新增 `FIREBASE_PLIST_SOURCE` 构建变量，由 configuration 指定本地 plist 源文件。
- 移除固定资源声明 `iosApp/GoogleService-Info.plist`，改为在 build phase 中复制对应 plist 到产物 bundle，并统一命名为 `GoogleService-Info.plist`。
- Firebase plist 复制脚本放在 Crashlytics dSYM 上传脚本之前。

### 配置文件

- `Base.xcconfig` 维护共享配置：显示名、产品名、版本号、Team ID、最低系统版本。
- `Debug.xcconfig`：
  - `APP_BUNDLE_IDENTIFIER = io.github.v2compose.iosApp.debug`
  - `APP_ICON_NAME = AppIconDebug`
  - `FIREBASE_PLIST_SOURCE = iosApp/GoogleService-Info-Debug.plist`
- `Release.xcconfig`：
  - `APP_BUNDLE_IDENTIFIER = io.github.v2compose.iosApp`
  - `APP_ICON_NAME = AppIcon`
  - `FIREBASE_PLIST_SOURCE = iosApp/GoogleService-Info.plist`

### Firebase 与资源

- Release 继续使用本地未跟踪文件 `iosApp/iosApp/GoogleService-Info.plist`。
- Debug 新增本地未跟踪文件 `iosApp/iosApp/GoogleService-Info-Debug.plist`。
- `.gitignore` 增加两个 iOS Firebase plist 路径。
- `FirebaseBootstrap.swift` 继续只读取 bundle 内的 `GoogleService-Info.plist`，但错误提示明确两套本地源文件要求。

### 图标区分

- 新增 `AppIconDebug.appiconset`。
- Debug 图标由当前正式图标派生，增加明显的 `DEV` 角标，不改显示名。

## 验证要求

- `xcodegen generate --spec iosApp/project.yml`
- `./gradlew :shared:compileKotlinIosSimulatorArm64`
- `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator -configuration Debug build`
- `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator -configuration Release build`
- `./gradlew :androidApp:assembleFossDebug`

## 备注

- 若本地尚未准备独立的 Debug Firebase App，可临时补齐 `GoogleService-Info-Debug.plist` 后再进行 iOS Debug 构建验证。
