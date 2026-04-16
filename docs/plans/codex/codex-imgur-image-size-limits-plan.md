# Imgur 图片尺寸双重限制实施计划

## Summary
- 在 Imgur 图片链路上同时限制显示尺寸和解码尺寸，避免正文与预览场景直接解码超大原图。
- 采用“视口优先”策略：正文按排版尺寸和安全上限解码；全屏预览按当前视口尺寸放大后再受硬上限约束。

## Key Changes
- 在 `htmlText` 公共层新增共享尺寸策略，供 `FixHtmlUseCase`、`HtmlText`、`GalleryImage` 复用。
- `FixHtmlUseCase` 不再使用 `Size.ORIGINAL` 探测图片宽高，统一走安全上限解码。
- `HtmlText` 的成功态与自动加载态都显式传入 decode size，不再只靠 `Modifier.size(...)`。
- `GalleryImage` 按当前视口像素尺寸计算预览解码尺寸，并使用硬上限约束峰值内存。

## Verification
- 为共享尺寸策略补充单元测试，覆盖正文探测、正文显示、预览视口、异常输入等场景。
- 保持 Imgur URL 解析与 HTML 改写相关测试继续通过。
- 执行 `./gradlew :shared:allTests`、`./gradlew :app:assembleFossDebug`、`./gradlew :shared:compileKotlinIosSimulatorArm64` 验证回归。
