# Codex Login Redirect Loop Plan

## 背景

- 打开登录页时，请求 `/signin` 返回 `302 Location: /signin`。
- Android 网络层会把该重定向广播为 `RedirectEvent`，全局导航层再打开一次登录页，导致无限循环。

## 实施方案

1. 增加认证流归一化逻辑：
   - `/signin/**` 归一化到 `/signin`
   - `/2fa/**` 归一化到 `/2fa`
2. Android `RedirectInterceptor` 过滤“请求和重定向同属一个认证流”的事件，避免自跳转触发全局导航。
3. `V2AppState` 增加认证流重复导航去重，作为第二层保护。
4. 登录页加载参数失败时，将这类自重定向异常翻译为明确文案，而不是仅显示通用加载失败。

## 验证

- 增加共享单元测试覆盖认证流归一化和重复导航判定。
- 执行 `:shared:allTests`、Android 构建和 iOS Simulator Kotlin 编译。
