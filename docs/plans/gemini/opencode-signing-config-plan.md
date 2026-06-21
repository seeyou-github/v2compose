# V2compose 签名硬编码配置修改计划

## 背景与目的
应用户要求，将 Android 的构建签名硬编码为使用当前项目根目录（实际上是在 `androidApp/` 下，或者按用户表述在项目中的 `androidApp/Test.jks`）。
具体的签名配置如下：
- 签名文件路径：`androidApp/Test.jks`（原本文件名为 `TestKey.jks`，需要重命名为 `Test.jks`）
- key_alias: `Test`
- keystore_password: `123456`
- key_password: `123456`

所有的 debug 和 release 版本都需要使用此签名，并且修改 `.github/workflows` 中涉及签名的相关部分。

## 实施方案

1. **重命名密钥文件**: 
   将 `androidApp/TestKey.jks` 重命名为 `androidApp/Test.jks`。

2. **修改 `androidApp/build.gradle.kts`**:
   - 移除 `releaseSigningEnvVars` 和 `isReleaseBuildTask` 相关的环境变量检查抛错逻辑，因为我们不再需要外部传入环境变量。
   - 在 `signingConfigs` 块中，统一配置 `release` 与 `debug` 签名或直接自定义一个配置供 debug 和 release 共同引用。由于 Gradle 默认自带一个名为 `debug` 的 signingConfig，我们建议在 `signingConfigs` 中，显式设置或更新 `release` 签名，或者也可以用自定义 `config`。
     考虑到 Gradle 特性，我们这样设计：
     ```kotlin
     signingConfigs {
         create("release") {
             storeFile = file("Test.jks") // 由于 context 是在 androidApp 模块下，file("Test.jks") 默认查找 androidApp/Test.jks 
             storePassword = "123456"
             keyAlias = "Test"
             keyPassword = "123456"
         }
         getByName("debug") {
             storeFile = file("Test.jks")
             storePassword = "123456"
             keyAlias = "Test"
             keyPassword = "123456"
         }
     }
     ```
   - 确保 `buildTypes { debug { signingConfig = signingConfigs.getByName("debug") } release { signingConfig = signingConfigs.getByName("release") } }` 正常配置。

3. **修改 `.github/workflows/release.yml`**:
   - 移除从 Base64 恢复 keystore 文件的步骤：不需要解密 `ANDROID_RELEASE_KEYSTORE_BASE64`。由于 `Test.jks` 已经保存在仓库中（如果是开源的/公开的，且用户要求直接用此硬编码签名），我们直接用代码库自带的 `androidApp/Test.jks`。
   - 移除编译 APK 时传入的 `ANDROID_RELEASE_KEYSTORE_PATH` 等环境变量，因为 `build.gradle.kts` 已硬编码。

4. **本地验证构建**:
   - 本地运行 `./gradlew :androidApp:assembleFossDebug` 和 `./gradlew :androidApp:assembleFossRelease` 验证配置正确性。

5. **执行代码提交**:
   - `git commit` 保存进度。
