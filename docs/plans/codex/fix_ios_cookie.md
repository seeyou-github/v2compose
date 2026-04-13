# 修复 iOS 登录 Cookie/CSRF 问题

## Summary

在 iOS 侧为 Darwin `HttpClient` 显式接入共享 cookie storage，确保 `GET /signin`、验证码图片请求、
`POST /signin` 处于同一会话；本轮默认只修复“原生表单登录失败”，不扩展到 `WKWebView` 全量 cookie 双向同步。

## Key Changes

- 在 iOS 网络层统一配置 Darwin session 使用 `NSHTTPCookieStorage.sharedHTTPCookieStorage`，并确保 V2
  主 client 与图片 client 共享这套 storage。
- 保持 Android 现有 `WebkitCookieManager` 方案不变，iOS 仅补齐缺失的 cookie 会话能力，不改公共登录
  API。
- 保留 `IosCookieManager.clearCookies()`，但让它真正对应到正在使用的共享 storage；必要时补一层简单的注释，说明它负责的是
  Darwin session 的共享 cookie 清理。
- 不在本轮引入额外的 repo-tracked 持久化格式；是否跨冷启动保活，先遵循服务端 cookie
  的过期属性，不额外发明本地序列化策略。

## Test Plan

- iOS 真机或模拟器验证：打开登录页后加载验证码，输入正确账号密码和验证码，确认不再出现“验证码错误 /
  CSRF 失效”。
- 再次验证失败重试路径：点击刷新验证码后重新提交，确认 `once` 与验证码仍然有效。
- 回归校验登出：执行登出后再次请求需要登录的页面，确认共享 cookie 已清空。
- 构建验证：编译 `:shared` 的 iOS Simulator target，并编译当前 Android 壳模块，确认双端不因网络层改动而回归。

## Assumptions

- 当前问题范围限定为 Compose 原生登录页，不把 Google 登录或通用 `WKWebView` 的 cookie 同步纳入本轮。
- 如果后续发现 iOS WebView 与 Ktor 会话也需要互通，再单独补 `NSHTTPCookieStorage` 与
  `WKWebsiteDataStore.httpCookieStore` 的桥接层。
