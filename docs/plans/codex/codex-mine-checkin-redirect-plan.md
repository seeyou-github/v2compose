# Mine 页签到重定向治理

## Summary
- 保持 Mine 页手动签到为原地完成流程，把 `/mission/daily/redeem -> 302 /mission/daily` 视为业务成功重定向，不再触发全局导航。
- 收紧全局重定向机制：只让认证相关重定向进入事件流，避免签到和用户/节点等业务 302 被 `V2AppState` 误消费。
- 手动签到后的前台体验保持为留在 Mine 页，按钮切成“已领取”，继续使用 Snackbar 展示连续登录天数。

## Key Changes
- 在 `V2Client` 中收窄全局重定向判定：
  - 任意请求如果被重定向到 `/signin*` 或 `/2fa*`，继续发全局事件，保留登录失效自动跳转。
  - 如果请求本身就是认证流程（`/signin*`、`/2fa*`），则允许把成功后的跳转目标继续发出去，例如 `/` 或 `next` 指向的内部页面。
  - `/mission/daily`、`/member/...`、`/go/...` 等业务成功重定向不再进入全局事件流。
- 将全局事件语义显式收敛为认证跳转事件，并同步更新 `V2AppState` 的消费逻辑，不在 UI 层做针对签到的特殊拦截。
- 保持 `CheckInUseCase` 作为签到业务 302 的唯一处理者：
  - `redeem` 请求若 302 到 `/mission/daily`，立刻再次拉取 `dailyInfo()`。
  - 以回读后的 `hadCheckedIn()` 和连续登录文案作为最终结果。
  - 非 `/mission/daily` 的重定向或普通异常按失败路径处理，不误判成功。

## Tests
- 扩展 `V2ClientRedirectTest`，覆盖：
  - `/signin -> /2fa` 继续发全局事件。
  - `/signin?next=... -> /` 或内部 `next` 目标继续发全局事件。
  - 非登录请求 `-> /signin?next=...` 继续发全局事件。
  - `/mission/daily/redeem -> /mission/daily` 不再发全局事件。
  - 用户动作回跳用户页等业务重定向不再发全局事件。
- 新增 `CheckInUseCaseTest`，覆盖：
  - `redeem` 302 到 `/mission/daily` 且二次 `dailyInfo` 显示已签到时返回成功。
  - 非 daily 重定向与普通异常不返回成功。
  - `dailyInfo` 回读后仍未签到时不误判成功。

## Assumptions
- 手动签到成功时停留在 Mine 页，不主动打开 mission 页面。
- 前台业务请求若因登录失效被重定向到登录页，应用仍自动导航到登录流程。
