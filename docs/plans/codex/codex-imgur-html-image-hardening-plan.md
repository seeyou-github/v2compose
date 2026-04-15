# Imgur 图片链路修复计划

## Summary
- 根因按当前证据认定为：HTML 图片链路把 `imgur.com/<id>` 这类页面 URL 当成图片源直接交给 Coil/平台下载，触发 Imgur 的页面域名与 CDN 域名重定向规则，导致正文预加载、大图预览、保存图片三条链路都可能失败。
- 本次按“整体补强”实施，但范围只覆盖 `HtmlContent` 相关图片链路与图片预览/保存，不改头像等普通 `AsyncImage` 用法，也不改 V2EX 主站请求客户端。
- 修复目标是先把 Imgur 页面 URL 解析成稳定的 `i.imgur.com/<id>.<ext>` 直链，再让正文渲染、点击预览、保存图片都统一使用同一个已解析 URL。

## Implementation Changes
- 在 `docs/plans/codex/` 归档本计划，文件名固定为 `codex-imgur-html-image-hardening-plan.md`，作为后续实现依据。
- 新增 `ExternalImageUrlResolver`（commonMain，Koin 注入）：
  - 接口固定为 `suspend fun resolve(rawUrl: String): String`。
  - 先做绝对 URL 归一化，保留现有 `fullUrl` 逻辑。
  - 对以下 URL 直接原样返回：非 Imgur 域名、已经带图片扩展名的直链、`i.imgur.com` 直链。
  - 对 `imgur.com` / `m.imgur.com` 的单资源页面链接（路径仅 1 段、无扩展名、忽略 query/fragment）做一次网络探测，并缓存 `raw absolute url -> resolved direct url`。
  - 探测请求使用 `NetworkClientProvider.imageHttpClient()`，允许跟随跳转；若最终请求 URL 落在 `i.imgur.com` 且路径扩展名属于图片类型（`png/jpg/jpeg/webp/gif/avif`），就把该最终 URL 作为 canonical URL。
  - 若最终落到 `gifv/mp4/webm` 或无法确认是图片，视为“不支持的媒体类型”，不做伪造扩展名，直接回退原始 URL。
  - 若探测失败、出现跳转环、或最终仍不是可确认图片 URL，返回原始 URL并记录结构化日志。
- 新增 `ExternalImageRequestHeaders` 辅助：
  - 接口固定为 `fun forUrl(url: String, probe: Boolean = false): Map<String, String>`。
  - 仅对 `imgur.com` / `i.imgur.com` 生效，其他域名返回空头集合。
  - 实际图片请求统一带：
    - `User-Agent`: 新增一个固定“标准浏览器” UA 常量，不再使用当前带 `Android + iPhone` 混合特征的 UA。
    - `Accept`: `image/avif,image/webp,image/apng,image/*,*/*;q=0.8`
    - `Referer`: `https://imgur.com/`
  - 探测请求在上述基础上允许 `text/html`，避免 Imgur 页面入口直接拒绝。
- 修改 HTML 图片预加载链路（`FixHtmlUseCase`）：
  - 在对每个 `<img>` 构建 `ImageRequest` 前先调用 resolver。
  - 一旦拿到 canonical URL，就直接改写 DOM 里的 `src` 属性，再继续现有的尺寸探测与 `loadState` 逻辑。
  - `ImageRequest.Builder` 不再只传裸 `src`，而是统一附加 `ExternalImageRequestHeaders` 返回的头。
  - 这样 `HtmlText` 后续点击图片时会拿到已改写的直链，正文显示与点击预览共享同一 URL。
- 修改图片预览与保存链路：
  - `PopupImage` 在展示 `GalleryImage` 前先异步 resolve 一次入参 URL；未解析完成时显示轻量加载态，不直接把原始 Imgur 页面 URL 交给 `AsyncImage`。
  - `GalleryImage` 使用与正文一致的请求头策略构建图片请求，避免预览链路退回旧行为。
  - Android 保存图片继续依赖 Coil 磁盘缓存，但缓存 key 必须来自 canonical URL；预览已经使用 canonical URL 后，这里无需额外分叉。
  - iOS 保存图片时，对 `imageHttpClient().get(imageUrl)` 也附加同一套 Imgur 头部策略，保证“预览能看、保存失败”的分叉被消掉。
- 增加日志：
  - 记录 `rawUrl / resolvedUrl / finalRequestUrl / status / location / fallbackReason`。
  - 只对 Imgur 探测与回退打日志，避免普通图片噪音。

## Public APIs / Types
- 新增 `ExternalImageUrlResolver.resolve(rawUrl: String): String`
- 新增 `ExternalImageRequestHeaders.forUrl(url: String, probe: Boolean = false): Map<String, String>`
- 不修改导航、仓库接口、Topic/User/Notification 的公开参数结构。

## Test Plan
- 新增 commonTest：
  - 非 Imgur URL 原样透传。
  - `imgur.com/<id>`、`m.imgur.com/<id>` 被识别为可探测页面 URL。
  - 已带扩展名的 `i.imgur.com/...` 不触发探测。
  - 探测结果若最终 URL 是 `i.imgur.com/<id>.png`，返回该直链。
  - 探测结果若最终 URL 是 `gifv/mp4/webm` 或无法确认图片，回退原始 URL。
  - 同一原始 URL 多次解析命中缓存，不重复探测。
- 为 `FixHtmlUseCase` 提取可测试的 URL 改写辅助逻辑，并覆盖：
  - HTML 中 Imgur 页面图在第一次成功解析后，输出 HTML 的 `img[src]` 已改成直链。
  - 非 Imgur 图片与现有 `loadState` 行为不变。
- 手工验证：
  - Topic `content` 和 `supplement` 中包含 `imgur.com/<id>` 的图片可正常展示。
  - 点击正文图片能正常弹出预览。
  - Android 与 iOS 的“保存图片”都成功。
  - User 页面与通知页面中复用 `HtmlContent` 的图片行为一致。
- 构建与验证命令：
  - `./gradlew :shared:allTests`
  - `./gradlew :androidApp:assembleFossDebug`
  - `./gradlew :shared:compileKotlinIosSimulatorArm64`

## Assumptions
- 本次只处理“单资源 Imgur 页面链接”到“图片直链”的解析；`/a/`、`/gallery/`、视频型资源不纳入此次修复。
- 如果无法确认真实图片直链，默认回退原始 URL并打日志，不做猜扩展名的启发式补全。
- 不修改全局 V2EX API 请求 UA，也不碰头像等非 HTML 图片组件，避免扩大回归面。
