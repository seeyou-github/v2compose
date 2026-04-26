# Codex Compose Audit Fixes Plan

## 背景

本次修复针对 Compose 审查中确认的 4 个问题：

1. 发帖页在组合期间同步阻塞读取草稿。
2. 回复成功后再次发送可能复用上一条正文。
3. 相同错误内容的 HTML 弹窗只能显示一次。
4. 主题页标题显隐阈值判断错误。

## 实施方案

### 1. 写帖页草稿加载改为异步状态流

- 在 `WriteTopicViewModel` 中将草稿改为 `StateFlow` 暴露，去掉 `runBlocking`。
- UI 通过 `collectAsStateWithLifecycle()` 订阅草稿加载状态。
- 草稿加载完成前允许先展示路由默认节点，避免首帧阻塞。
- 仅在用户尚未编辑时，用已加载草稿回填本地输入状态，避免异步回流覆盖正在输入的内容。

### 2. 回复输入框父子状态保持一致

- 点击“回复某层”时，同时更新可见初始文本和真正用于提交的父层文本。
- 回复成功后同时清空 `replyInputInitialText` 与 `replyInputCurrentText`。
- 保持“折叠再展开时保留未发送草稿”的现有交互，不做额外行为扩张。

### 3. HTML 错误弹窗改为外部控制生命周期

- `HtmlAlertDialog` 改为纯展示组件，不再内部缓存“是否已关闭”。
- 各调用方按具体失败状态对象维护 `showProblem`，保证：
  - 同一次失败可以手动关闭；
  - 下一次即使返回相同 HTML 内容，也能重新弹出。

### 4. 主题页标题显隐阈值修正

- 将阈值判断改为正向滚动偏移比较，使用 `64.dp` 的正值偏移。
- 保持现有“滚过一定距离再显示标题”的设计意图，不调整其它导航栏动画。

## 验证

- 运行 `./gradlew :androidApp:assembleFossDebug`
- 运行 `./gradlew :shared:compileKotlinIosSimulatorArm64`
