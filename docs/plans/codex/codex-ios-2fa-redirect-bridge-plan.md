# iOS 2FA Redirect Bridge Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让 `/signin` 提交后的 `302 Location: /2fa` 在 Android 和 iOS 都统一转成 `RedirectEvent`，从而稳定跳转到两步验证页。

**Architecture:** 把“是否应当把重定向转成认证导航事件”的判定下沉到共享 Ktor client。共享层负责解析 `ResponseException + request.url + Location`，Android 删除仅此一份的 OkHttp 拦截器分支，iOS 通过同一套共享逻辑自动获得行为。

**Tech Stack:** Kotlin Multiplatform, Ktor 3.4.2, OkHttp, Darwin engine, kotlinx.coroutines-test, commonTest

---

## File Map

- Modify: `shared/src/commonMain/kotlin/io/github/v2compose/network/V2Client.kt`
  - 安装共享的响应异常处理器，并在合适时发出 `RedirectEvent`。
- Modify: `shared/src/androidMain/kotlin/io/github/v2compose/network/OkHttpFactory.kt`
  - 删除只存在于 Android 的认证重定向事件桥接，避免双发事件。
- Modify: `shared/src/androidMain/kotlin/io/github/v2compose/di/PlatformModule.kt`
  - 调整 Android `HttpClient` 构建参数，让共享层持有 `V2EventManager`。
- Modify: `shared/src/iosMain/kotlin/io/github/v2compose/network/IosNetworkClientRegistry.kt`
  - 注入 `V2EventManager`，仅给主 V2 client 开启认证重定向事件桥接。
- Modify: `shared/src/iosMain/kotlin/io/github/v2compose/di/PlatformModule.kt`
  - 为 `IosNetworkClientRegistry` 补齐 `V2EventManager` 依赖。
- Test: `shared/src/commonTest/kotlin/io/github/v2compose/network/V2ClientRedirectTest.kt`
  - 覆盖 2FA 重定向、同认证流自跳转过滤、非 V2EX host 过滤。

## Task 1: 写失败测试锁定共享判定逻辑

**Files:**
- Create: `shared/src/commonTest/kotlin/io/github/v2compose/network/V2ClientRedirectTest.kt`
- Test: `shared/src/commonTest/kotlin/io/github/v2compose/network/V2ClientRedirectTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
class V2ClientRedirectTest {

    @Test
    fun `returns 2fa location for signin redirect`() {
        assertEquals(
            "/2fa",
            resolveAuthRedirectEventLocation(
                requestUrl = "https://www.v2ex.com/signin",
                redirectLocation = "/2fa",
            ),
        )
    }

    @Test
    fun `ignores redirect to same auth flow`() {
        assertNull(
            resolveAuthRedirectEventLocation(
                requestUrl = "https://www.v2ex.com/signin",
                redirectLocation = "/signin?next=%2Fsettings",
            ),
        )
    }

    @Test
    fun `ignores non v2ex requests`() {
        assertNull(
            resolveAuthRedirectEventLocation(
                requestUrl = "https://example.com/signin",
                redirectLocation = "/2fa",
            ),
        )
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :shared:allTests --tests "io.github.v2compose.network.V2ClientRedirectTest"`
Expected: FAIL, because `resolveAuthRedirectEventLocation` 尚未定义。

## Task 2: 把认证重定向桥接下沉到共享 Ktor client

**Files:**
- Modify: `shared/src/commonMain/kotlin/io/github/v2compose/network/V2Client.kt`
- Modify: `shared/src/androidMain/kotlin/io/github/v2compose/network/OkHttpFactory.kt`
- Modify: `shared/src/androidMain/kotlin/io/github/v2compose/di/PlatformModule.kt`
- Modify: `shared/src/iosMain/kotlin/io/github/v2compose/network/IosNetworkClientRegistry.kt`
- Modify: `shared/src/iosMain/kotlin/io/github/v2compose/di/PlatformModule.kt`
- Test: `shared/src/commonTest/kotlin/io/github/v2compose/network/V2ClientRedirectTest.kt`

- [ ] **Step 1: Write minimal shared implementation**

```kotlin
internal fun resolveAuthRedirectEventLocation(
    requestUrl: String,
    redirectLocation: String?,
): String? {
    if (redirectLocation.isNullOrBlank()) return null
    val requestHost = runCatching { Url(requestUrl).host }.getOrNull() ?: return null
    if (!requestHost.endsWith(Constants.host)) return null
    if (isSameAuthFlow(requestUrl, redirectLocation)) return null
    return redirectLocation
}

private suspend fun handleRedirectException(
    cause: Throwable,
    request: HttpRequest,
    eventManager: V2EventManager?,
) {
    if (eventManager == null || cause !is ResponseException) return
    if (cause.response.status.value !in 300..399) return
    resolveAuthRedirectEventLocation(
        requestUrl = request.url.toString(),
        redirectLocation = cause.response.headers[HttpHeaders.Location],
    )?.let { eventManager.tryPost(RedirectEvent(it)) }
}
```

- [ ] **Step 2: Install the shared handler in `createV2HttpClient`**

```kotlin
fun createV2HttpClient(
    engine: HttpClientEngine? = null,
    fruit: Fruit = Fruit.createDefault(),
    eventManager: V2EventManager? = null,
): HttpClient = HttpClient(engine ?: createHttpClientEngine()) {
    expectSuccess = true

    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, request ->
            handleRedirectException(cause, request, eventManager)
        }
    }
}
```

- [ ] **Step 3: Remove Android-only redirect interception**

```kotlin
OkHttpClient.Builder()
    .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
    .cache(cache)
    .cookieJar(cookieJar)
    .retryOnConnectionFailure(true)
    .addInterceptor(ConfigInterceptor())
    .followRedirects(false)
    .followSslRedirects(false)
    .proxySelector(proxySelector)
```

- [ ] **Step 4: Wire `V2EventManager` into platform builders**

```kotlin
createAndroidV2HttpClient(
    okHttpClient = get(named("CommonOkHttpClient")),
    fruit = get(),
    eventManager = get(),
)
```

```kotlin
class IosNetworkClientRegistry(
    private val fruit: Fruit,
    private val urlCache: NSURLCache,
    appPreferences: AppPreferences,
    private val eventManager: V2EventManager,
) {
    private fun buildV2HttpClient(proxyInfo: ProxyInfo): HttpClient =
        createV2HttpClient(
            engine = createIosHttpClientEngine(...),
            fruit = fruit,
            eventManager = eventManager,
        )
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :shared:allTests --tests "io.github.v2compose.network.V2ClientRedirectTest"`
Expected: PASS

## Task 3: 全量回归验证并提交

**Files:**
- Modify: `docs/plans/codex/codex-ios-2fa-redirect-bridge-plan.md`

- [ ] **Step 1: Run shared tests**

Run: `./gradlew :shared:allTests`
Expected: PASS

- [ ] **Step 2: Run Android build**

Run: `./gradlew :app:assembleFossDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Run iOS simulator Kotlin compile**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/io/github/v2compose/network/V2Client.kt \
  shared/src/commonTest/kotlin/io/github/v2compose/network/V2ClientRedirectTest.kt \
  shared/src/androidMain/kotlin/io/github/v2compose/network/OkHttpFactory.kt \
  shared/src/androidMain/kotlin/io/github/v2compose/di/PlatformModule.kt \
  shared/src/iosMain/kotlin/io/github/v2compose/network/IosNetworkClientRegistry.kt \
  shared/src/iosMain/kotlin/io/github/v2compose/di/PlatformModule.kt \
  docs/plans/codex/codex-ios-2fa-redirect-bridge-plan.md
git commit -m "fix: bridge auth redirects in shared client"
```
