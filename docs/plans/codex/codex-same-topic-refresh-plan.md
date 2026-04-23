# 同 Topic 导航复用当前页并静默刷新

## 背景

- 当前从 `openUri` / redirect 再次导航到同一个 Topic 时，会新建新的 `TopicScreen` route。
- 这种行为会污染 back stack，也会把用户当前阅读位置重置掉。
- 现有 `TopicScreen` 已支持页面内纯 `#replyN` 锚点定位，但不适合把“同 topic 完整 link 导航”继续等同成跳楼层。

## 目标

1. 当前已经在某个 Topic 页面时，再导航到同一个 Topic 不再新建 route。
2. 同 topic 完整 link 导航时，不再根据 `/t/{id}#replyN` 触发楼层跳转。
3. 改为刷新当前 Topic 数据，但尽量保持当前位置不滚动。
4. 页面内纯 `#replyN` 锚点定位行为保持不变。

## 实施

- 在 `TopicNavigation.kt` 中补充 topic route 解析与“同 topic 复用” helper，并定义当前 topic entry 的刷新 token key。
- 在 `V2AppState.kt` 中拦截 `AppNavigationAction.Navigate`：
  - 若当前 destination 已是 `TopicScreen`，且目标 route 的 `topicId` 与当前一致，则不再 `navigate(...)`
  - 改为向当前 entry 的 `SavedStateHandle` 写入新的 refresh token
- 在 `TopicScreen.kt` 中监听 refresh token：
  - token 变化时调用 `topicItems.refresh()`
  - 不改 `LazyListState`，不改当前楼层目标

## 验证

- 为 topic route 解析与复用判断新增纯 helper 单测。
- 回归确认页面内纯 `#replyN` 逻辑不变。
- 执行：
  - `./gradlew :shared:allTests`
  - `./gradlew :androidApp:assembleFossDebug`
  - `./gradlew :shared:compileKotlinIosSimulatorArm64`
