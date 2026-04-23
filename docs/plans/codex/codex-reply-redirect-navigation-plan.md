# 修复回帖重定向并统一导航解析

## 背景

- 发帖/回帖后的 `302 Location` 可能是 `/t/1207974?p=1#reply36`。
- 现有 `resolveRedirectLocation()` 只识别首页和登录流，导致主题页重定向落到“未支持页面”。
- `resolveRedirectLocation()` 与 `resolveOpenUri()` 还各自维护了一套近似的路径判定逻辑，维护成本偏高。

## 目标

1. 让 `/t/{topicId}?p=...#reply{floor}` 这类回帖重定向直接进入主题页并定位到对应楼层。
2. 抽出共享的导航语义分类，统一 `resolveOpenUri()` 与 `resolveRedirectLocation()` 的解析逻辑。
3. 保持既有边界不变：
   - `openUri` 对未知内部路径仍进入 WebView。
   - `redirectLocation` 对未知内部路径仍进入 unsupported。
   - Topic 应用内路由协议仍维持 `/t/{topicId}#reply{floor}`，不引入 `p` 参数。

## 实施方案

- 在 `AppNavigation.kt` 中增加私有导航目标模型，统一分类：
  - 根路径
  - 认证路径
  - 主题页
  - 节点页
  - 用户页
  - 未知内部路径
  - 外部 / system URI
- `resolveOpenUri()` 改为消费共享分类结果：
  - topic / node / member 继续走应用内页面
  - 其它内部路径继续转成 WebView 路由
  - 外部与 `mailto` / `sms` / `tel` 继续交给外部处理
- `resolveRedirectLocation()` 改为消费同一分类结果：
  - 根路径清空栈回首页
  - 登录流继续归一化到 `/signin` 或 `/2fa`
  - topic / node / member 直接应用内导航
  - 未知内部路径继续转 unsupported

## 验证

- 为 `AppNavigationTest` 补充 topic/node/member redirect 场景。
- 增加 `resolveOpenUri("/signin")` 仍走 WebView 的回归断言。
- 执行：
  - `./gradlew :shared:allTests`
  - `./gradlew :app:assembleFossDebug`
  - `./gradlew :shared:compileKotlinIosSimulatorArm64`
