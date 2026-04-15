# Logging Unification Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 把项目内业务日志、网络日志和模块内零散输出统一收敛到 `KLogger`，并默认使用可读的摘要级网络日志。

**Architecture:** 先抽出一个新的 `:logging` KMP 模块承载 `KLogger` 与网络日志适配层，解决 `:shared` 依赖 `:htmlText` 导致的循环依赖问题。然后把 Ktor、Android OkHttp 包装层和 `htmlText` 的直接输出改接 `KLogger`，并把网络日志策略从默认 BODY 改为默认摘要、可扩展到详细模式。

**Tech Stack:** Kotlin Multiplatform, Gradle Kotlin DSL, Ktor 3.4.2, OkHttp 5.3.2, Android Logcat, iOS NSLog

---

## File Map

- Create: `logging/build.gradle.kts`
- Create: `logging/src/commonMain/kotlin/io/github/v2compose/util/KLogger.kt`
- Create: `logging/src/commonMain/kotlin/io/github/v2compose/util/NetworkKLogger.kt`
- Create: `logging/src/commonTest/kotlin/io/github/v2compose/util/NetworkKLoggerTest.kt`
- Create: `logging/src/androidMain/kotlin/io/github/v2compose/util/KLogger.kt`
- Create: `logging/src/iosMain/kotlin/io/github/v2compose/util/KLogger.kt`
- Modify: `settings.gradle.kts`
- Modify: `shared/build.gradle.kts`
- Modify: `htmlText/build.gradle.kts`
- Delete: `shared/src/commonMain/kotlin/io/github/v2compose/util/KLogger.kt`
- Delete: `shared/src/androidMain/kotlin/io/github/v2compose/util/KLogger.kt`
- Delete: `shared/src/iosMain/kotlin/io/github/v2compose/util/KLogger.kt`
- Modify: `shared/src/commonMain/kotlin/io/github/v2compose/network/V2Client.kt`
- Modify: `shared/src/commonMain/kotlin/io/github/v2compose/V2AppState.kt`
- Modify: `shared/src/androidMain/kotlin/io/github/v2compose/network/OkHttpFactory.kt`
- Modify: `htmlText/src/iosMain/kotlin/io/github/cooaer/htmltext/PlatformActuals.kt`

## Task 1: 抽出独立的 `:logging` 模块

**Files:**
- Create: `logging/build.gradle.kts`
- Create: `logging/src/commonMain/kotlin/io/github/v2compose/util/KLogger.kt`
- Create: `logging/src/androidMain/kotlin/io/github/v2compose/util/KLogger.kt`
- Create: `logging/src/iosMain/kotlin/io/github/v2compose/util/KLogger.kt`
- Modify: `settings.gradle.kts`
- Modify: `shared/build.gradle.kts`
- Modify: `htmlText/build.gradle.kts`
- Delete: `shared/src/commonMain/kotlin/io/github/v2compose/util/KLogger.kt`
- Delete: `shared/src/androidMain/kotlin/io/github/v2compose/util/KLogger.kt`
- Delete: `shared/src/iosMain/kotlin/io/github/v2compose/util/KLogger.kt`

- [ ] **Step 1: Add the new module in Gradle settings**

```kotlin
// settings.gradle.kts
include(":androidApp")
include(":htmlText")
include(":logging")
include(":shared")
```

- [ ] **Step 2: Create the `:logging` KMP module**

```kotlin
// logging/build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    android {
        namespace = "io.github.v2compose.logging"
        compileSdk = 36
        minSdk = 26

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    iosArm64()
    iosSimulatorArm64()
}
```

- [ ] **Step 3: Move `KLogger` into the new module without changing package name**

```kotlin
// logging/src/commonMain/kotlin/io/github/v2compose/util/KLogger.kt
package io.github.v2compose.util

expect object KLogger {
    fun v(tag: String, msg: String)
    fun d(tag: String, msg: String)
    fun i(tag: String, msg: String)
    fun w(tag: String, msg: String)
    fun e(tag: String, msg: String, throwable: Throwable? = null)
}
```

```kotlin
// logging/src/androidMain/kotlin/io/github/v2compose/util/KLogger.kt
package io.github.v2compose.util

import android.util.Log

actual object KLogger {
    actual fun v(tag: String, msg: String) = Log.v(tag, msg)
    actual fun d(tag: String, msg: String) = Log.d(tag, msg)
    actual fun i(tag: String, msg: String) = Log.i(tag, msg)
    actual fun w(tag: String, msg: String) = Log.w(tag, msg)
    actual fun e(tag: String, msg: String, throwable: Throwable?) = Log.e(tag, msg, throwable)
}
```

```kotlin
// logging/src/iosMain/kotlin/io/github/v2compose/util/KLogger.kt
package io.github.v2compose.util

import platform.Foundation.NSLog

actual object KLogger {
    actual fun v(tag: String, msg: String) = NSLog("V/$tag: $msg")
    actual fun d(tag: String, msg: String) = NSLog("D/$tag: $msg")
    actual fun i(tag: String, msg: String) = NSLog("I/$tag: $msg")
    actual fun w(tag: String, msg: String) = NSLog("W/$tag: $msg")
    actual fun e(tag: String, msg: String, throwable: Throwable?) =
        NSLog("E/$tag: $msg\n${throwable?.stackTraceToString() ?: ""}")
}
```

- [ ] **Step 4: Wire dependencies and remove old copies**

```kotlin
// shared/build.gradle.kts
commonMain.dependencies {
    implementation(project(":logging"))
    implementation(project(":htmlText"))
}
```

```kotlin
// htmlText/build.gradle.kts
sourceSets {
    commonMain.dependencies {
        implementation(project(":logging"))
    }
}
```

- [ ] **Step 5: Compile to verify the dependency cycle is gone**

Run: `./gradlew :logging:compileKotlinIosSimulatorArm64 :shared:compileKotlinIosSimulatorArm64 :htmlText:compileKotlinIosSimulatorArm64`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Commit**

```bash
git add settings.gradle.kts logging/build.gradle.kts \
  logging/src/commonMain/kotlin/io/github/v2compose/util/KLogger.kt \
  logging/src/androidMain/kotlin/io/github/v2compose/util/KLogger.kt \
  logging/src/iosMain/kotlin/io/github/v2compose/util/KLogger.kt \
  shared/build.gradle.kts htmlText/build.gradle.kts \
  shared/src/commonMain/kotlin/io/github/v2compose/util/KLogger.kt \
  shared/src/androidMain/kotlin/io/github/v2compose/util/KLogger.kt \
  shared/src/iosMain/kotlin/io/github/v2compose/util/KLogger.kt
git commit -m "refactor: extract shared logging module"
```

## Task 2: 先写失败测试，锁定网络日志摘要格式

**Files:**
- Create: `logging/src/commonMain/kotlin/io/github/v2compose/util/NetworkKLogger.kt`
- Create: `logging/src/commonTest/kotlin/io/github/v2compose/util/NetworkKLoggerTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package io.github.v2compose.util

import kotlin.test.Test
import kotlin.test.assertEquals

class NetworkKLoggerTest {

    @Test
    fun formatRequestStart_usesCompactSummary() {
        assertEquals(
            "HTTP --> GET https://www.v2ex.com/signin",
            NetworkKLogger.formatRequestStart("GET", "https://www.v2ex.com/signin"),
        )
    }

    @Test
    fun formatResponseEnd_includesStatusAndElapsedTime() {
        assertEquals(
            "HTTP <-- 302 https://www.v2ex.com/signin (1063ms)",
            NetworkKLogger.formatResponseEnd(
                status = 302,
                url = "https://www.v2ex.com/signin",
                elapsedMs = 1063,
            ),
        )
    }

    @Test
    fun formatFailure_keepsMethodUrlAndMessage() {
        assertEquals(
            "HTTP xx POST https://www.v2ex.com/signin :: timeout",
            NetworkKLogger.formatFailure(
                method = "POST",
                url = "https://www.v2ex.com/signin",
                message = "timeout",
            ),
        )
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :logging:allTests`

Expected: FAIL with unresolved `NetworkKLogger`

- [ ] **Step 3: Write the minimal formatter implementation**

```kotlin
package io.github.v2compose.util

object NetworkKLogger {
    private const val TAG = "Network"

    fun formatRequestStart(method: String, url: String): String =
        "HTTP --> $method $url"

    fun formatResponseEnd(status: Int, url: String, elapsedMs: Long): String =
        "HTTP <-- $status $url (${elapsedMs}ms)"

    fun formatFailure(method: String, url: String, message: String): String =
        "HTTP xx $method $url :: $message"

    fun requestStart(method: String, url: String) {
        KLogger.d(TAG, formatRequestStart(method, url))
    }

    fun responseEnd(status: Int, url: String, elapsedMs: Long) {
        KLogger.d(TAG, formatResponseEnd(status, url, elapsedMs))
    }

    fun failure(method: String, url: String, message: String, throwable: Throwable? = null) {
        KLogger.e(TAG, formatFailure(method, url, message), throwable)
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :logging:allTests`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add logging/src/commonMain/kotlin/io/github/v2compose/util/NetworkKLogger.kt \
  logging/src/commonTest/kotlin/io/github/v2compose/util/NetworkKLoggerTest.kt
git commit -m "test: lock network log summary format"
```

## Task 3: 让 shared 网络层和导航日志统一走 KLogger

**Files:**
- Modify: `shared/src/commonMain/kotlin/io/github/v2compose/network/V2Client.kt`
- Modify: `shared/src/commonMain/kotlin/io/github/v2compose/V2AppState.kt`

- [ ] **Step 1: Replace direct Ktor default logger usage in `V2Client.kt`**

```kotlin
install(Logging) {
    logger = object : Logger {
        override fun log(message: String) {
            KLogger.v("Network", message)
        }
    }
    level = LogLevel.INFO
    format = LoggingFormat.OkHttp
}
```

- [ ] **Step 2: Route redirect diagnostics through `KLogger` instead of `Logger.DEFAULT`**

```kotlin
KLogger.d(
    "V2Client",
    "redirect observed: request=$requestUrl, location=$redirectLocation, status=${response.status.value}",
)

KLogger.d("V2Client", "post RedirectEvent($it)")
```

- [ ] **Step 3: Route navigation diagnostics through `KLogger`**

```kotlin
KLogger.d("V2AppState", "consume RedirectEvent(${event.location})")
KLogger.d(
    "V2AppState",
    "navigate action: current=${navHostController.currentDestination?.route}, target=${action.route}, clearToRoot=${action.clearBackStackToRoot}",
)
```

- [ ] **Step 4: Run shared tests and iOS compile**

Run: `./gradlew :shared:allTests :shared:compileKotlinIosSimulatorArm64`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/io/github/v2compose/network/V2Client.kt \
  shared/src/commonMain/kotlin/io/github/v2compose/V2AppState.kt
git commit -m "refactor: route shared network logs through klogger"
```

## Task 4: 删除 Android 套娃网络日志并统一 htmlText 输出

**Files:**
- Modify: `shared/src/androidMain/kotlin/io/github/v2compose/network/OkHttpFactory.kt`
- Modify: `htmlText/src/iosMain/kotlin/io/github/cooaer/htmltext/PlatformActuals.kt`

- [ ] **Step 1: Remove `HttpLoggingInterceptor` from Android OkHttp**

```kotlin
val builder = OkHttpClient.Builder()
    .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
    .cache(cache)
    .cookieJar(cookieJar)
    .retryOnConnectionFailure(true)
    .addInterceptor(ConfigInterceptor())
    .followRedirects(false)
    .followSslRedirects(false)
    .proxySelector(proxySelector)
```

同样删除 `createImageHttpClient()` 里的 `HttpLoggingInterceptor`。

- [ ] **Step 2: Replace `println` in `htmlText` with `KLogger`**

```kotlin
package io.github.cooaer.htmltext

import io.github.v2compose.util.KLogger

actual fun logDebug(tag: String, message: String) {
    KLogger.d(tag, message)
}
```

- [ ] **Step 3: Run Android build and cross-module compile**

Run: `./gradlew :androidApp:assembleFossDebug :htmlText:compileKotlinIosSimulatorArm64`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add shared/src/androidMain/kotlin/io/github/v2compose/network/OkHttpFactory.kt \
  htmlText/src/iosMain/kotlin/io/github/cooaer/htmltext/PlatformActuals.kt
git commit -m "refactor: unify android and htmltext logs through klogger"
```

## Task 5: 全量验证与人工检查

**Files:**
- Modify: `docs/plans/codex/codex-logging-unification-plan.md`

- [ ] **Step 1: Run module and project verification**

Run: `./gradlew :logging:allTests :shared:allTests :androidApp:assembleFossDebug :shared:compileKotlinIosSimulatorArm64`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: Manually verify Android logs**

Run the Android app and confirm:

```text
D/Network: HTTP --> GET https://www.v2ex.com/?tab=all
D/Network: HTTP <-- 302 https://www.v2ex.com/?tab=all (1063ms)
D/V2Client: redirect observed: request=..., location=/2fa, status=302
```

- [ ] **Step 3: Manually verify iOS logs**

Run the iOS app and confirm:

```text
D/Network: HTTP --> GET https://www.v2ex.com/?tab=all
D/Network: HTTP <-- 302 https://www.v2ex.com/?tab=all (1063ms)
D/V2AppState: consume RedirectEvent(/2fa)
```

- [ ] **Step 4: Final commit**

```bash
git add docs/plans/codex/codex-logging-unification-plan.md
git commit -m "docs: finalize logging unification execution plan"
```
