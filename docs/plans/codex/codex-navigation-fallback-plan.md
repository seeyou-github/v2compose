# Codex Navigation Fallback Plan

## 背景

- 运行时出现 `IllegalArgumentException`，原因是导航系统收到未注册路由 `/signin/cooldown`。
- 当前 `V2AppState` 会将解析结果直接传给 `NavController.navigate()`，缺少最后一道兜底。

## 方案

1. 在重定向解析层归一化登录流子路径：
   - `/signin/**` 统一映射到 `/signin`
   - `/2fa/**` 统一映射到 `/2fa`
2. 为无法映射到已注册页面的内部路径增加共享“未支持页面”：
   - 展示原始路径
   - 提供返回首页与外部浏览器打开能力
3. 在 `V2AppState` 中捕获 `IllegalArgumentException`：
   - 任何遗漏的未注册 route 都自动跳转到错误页，而不是崩溃

## 预期结果

- `/signin/cooldown` 不再导致应用崩溃。
- 后续若再出现未注册内部路径，也会统一进入错误页，便于继续使用与排查。
