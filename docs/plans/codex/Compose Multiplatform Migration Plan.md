## Summary

  - 目标是把项目从“Android App + 若干 KMP 模块”演进成“共享业务与共享 UI + Android/iOS 双宿主”。
  - 当前事实：
      - shared 与 htmlText 已是 KMP 模块，且 iOS target 当前能编译通过。
      - Android 仍以 app/src/main/java/io/github/v2compose/MainActivity.kt 和 app/src/main/java/io/
        github/v2compose/V2App.kt 为主入口。
      - shared 已承载共享导航、共享 ViewModel、数据层、资源、主题及平台 handler 注入，`initKoin`
        已形成可用共享模块组合。
      - app 已基本收敛为 Android 宿主与 Android-only 初始化层，不再维护大块重复共享 UI。
      - htmlText 的 iOS 端已有 expect/actual 骨架，但仍存在功能占位，典型如 YouTube 播放器。
  - 迁移策略：保持 Android 可发布，分阶段把“共享应用壳”建立起来，首个 iOS 版本只覆盖核心浏览能力。

## Implementation Doc

  - Phase 4: `docs/plans/codex/phase-4.md`

## Key Changes

### Phase 0: 基线收敛

  - 统一模块职责：
      - app 只保留 Android 宿主、Android 平台服务和 Android 独占能力。
      - shared 负责共享业务、共享状态、共享导航、共享 Compose UI。
      - htmlText 继续作为独立 KMP UI 组件，但要把占位能力与真正可交付能力区分清楚。
  - 清理迁移债务：
      - 解决 fruit-kt 的 Kotlin hierarchy template 告警，避免后续 iOS source set 扩展继续堆警告。
      - 补齐共享层的模块清单，形成真正可用的 sharedModule/platformModule 组合，而不是只保留空的
        initKoin 外壳。
      - 明确 Android-only 能力目录：通知、WorkManager、WebView 代理、分享、浏览器、Firebase、登录回调
        等。

### Phase 1: 建立共享应用壳

  - 在 shared/commonMain 新增共享根入口，职责等价于当前 Android 的 V2App + V2AppState +
    V2AppNavGraph。
  - 将下列内容迁移为共享实现：
      - 应用级状态容器：snackbar、导航状态、全局事件分发。
      - 主题与资源接入：统一只走 Compose Multiplatform 资源和共享 theme。
      - 导航图：以 JetBrains Navigation Compose 为准，不再继续扩大 Android androidx.navigation 版本的
        使用面。
      - ViewModel 基类与 UI state：优先迁移“核心浏览版”涉及页面。
  - Android 宿主改为最薄适配层：
      - MainActivity 只负责窗口配置、平台 context、调用共享根 Composable。
      - Android Application 只负责启动 Koin、注入 Android 平台模块、初始化 Android 专属服务。
  - 新增 iOS 宿主工程：
      - 使用标准 Compose Multiplatform iOS 宿主结构，提供 UIViewController 入口并接入共享 Koin 初始
        化。
      - 宿主只做生命周期、平台 context、系统桥接，不承载业务逻辑。

### Phase 2: 共享核心浏览能力

  - 按“先读后写、先简单后复杂”的顺序迁移页面：
      - 第一波：首页、节点页、主题详情、用户页、搜索页。
      - 第二波：登录、我的页面中的基础资料与列表浏览。
  - 每迁一个页面，同时迁它的：
      - Composable
      - ViewModel / state / intent
      - 路由定义
      - Koin 注入声明
  - 数据层原则：
      - 已在 shared 的 repository/usecase 不回退到 app。
      - Android 专属实现通过 expect/actual 或平台模块注入，不把 Context、Intent、WorkManager 继续渗透
        回共享 UI。
  - htmlText 处理策略：
      - 保留共享 HTML 渲染主逻辑。
      - 对 iOS 暂不具备等价能力的功能做显式降级策略，而不是隐式 placeholder。
      - 至少明确 3 类能力：完全共享、平台差异适配、iOS 暂不支持。

### Phase 3: 平台能力适配与功能分层

  - 明确平台能力接口，优先抽这些服务而不是把平台 API 散落在页面里：
      - 浏览器打开链接
      - 分享
      - Cookie 管理
      - 日志
      - 图片缓存目录
      - 登录跳转/回调
      - 外部视频/网页展示
  - Android-only 功能暂留 Android：
      - WorkManager 自动签到
      - 通知中心
      - WebView 代理能力
      - Firebase Analytics / Crashlytics
  - iOS v1 范围内不做强等价的能力：
      - 自动签到
      - 后台任务
      - Android 风格内部浏览器/代理链路
      - 复杂 WebView 交互
  - Phase 3 结束时，要形成“核心浏览跨平台、增强能力按平台差异提供”的稳定边界。

### Phase 4: 收尾与收敛

  - Android 旧实现维持宿主薄壳：
      - `app` 继续保留 Android 宿主、Android-only 初始化与平台独占能力。
      - 不再把 `V2App()` 这类宿主包装层误判为“待删除旧 UI”。
  - 删除仅因双轨迁移存在的过渡注入和桥接代码：
      - 例如将平台能力注册收敛回各平台 `platformModule`，避免独立过渡模块长期保留。
  - 文档补齐：
      - 模块职责图
      - 平台能力接口清单
      - iOS 首版功能矩阵
      - 新页面迁移模板
      - 详细落地说明见 `docs/plans/codex/phase-4.md`
  - 发布准备：
      - Android 继续完整构建。
      - iOS 至少具备可运行 Demo 和核心浏览回归清单。

## Public APIs / Interfaces / Types

  - 新增共享应用入口，例如 SharedApp(platformContext, platformServices) 一类根 Composable/API，供
    Android 与 iOS 宿主统一调用。
  - shared 中新增平台服务接口集合，替代直接依赖 Android API；建议按能力拆分而不是做一个巨大的
    PlatformServices。
  - shared 中新增真正可用的共享 Koin 模块导出，区分：
      - sharedBusinessModule
      - sharedUiModule
      - platformModule
  - 现有 PlatformContext 保留，但不要继续让更多共享逻辑直接依赖它；新增能力接口优先于继续扩张
    context。
  - htmlText 中需要把当前 iOS 占位实现标注为降级接口语义，避免调用方误判“功能已支持”。

## Test Plan

  - 构建验证：
      - :app Android 构建持续可过。
      - :shared、:htmlText 的 android 与 iosSimulatorArm64 编译持续可过。
      - iOS 宿主工程能启动到共享首页。
  - 共享层单元测试：
      - repository/use case
      - 共享状态转换
      - 导航参数编码/解码
      - 平台能力接口的 fake 实现
  - UI 验证：
      - 主题详情中的 htmlText 渲染、链接点击、图片显示、@用户 和楼层跳转。
      - 核心浏览版页面在 Android/iOS 的状态流转一致。
  - 回归重点：
      - 登录态持久化
      - Cookie 与 DataStore/Preferences 行为
      - 分页列表加载
      - 深色模式与资源访问
  - 验收标准：
      - Android 不因迁移丢现有核心功能。
      - iOS 首版可完成浏览、搜索、查看主题、查看用户、登录。
      - 所有 Android-only 能力在共享层都有清晰边界和降级说明。

## Assumptions

  - 采用渐进双轨迁移，不做一次性大切换。
  - iOS 首版只追求“核心浏览版”，不追求与 Android 完全功能齐平。
  - 继续沿用现有技术方向：Koin、JetBrains Navigation Compose、Compose Multiplatform、Ktor、Coil 3。
  - 不在本轮迁移中替换整体架构到 Decompose/Redux/其他导航栈，除非后续出现现有栈无法覆盖的明确阻塞。
  - app 将长期保留为 Android 宿主模块，但不再承担共享业务与共享 UI 的主实现。
