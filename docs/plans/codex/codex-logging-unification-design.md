# Codex Logging Unification Design

## 背景

当前项目的日志输出存在三套并行通道：

1. 应用层日志通过 `KLogger` 输出
2. Ktor 网络日志通过 `Logger.DEFAULT` 输出
3. Android 的 OkHttp 网络日志通过 `HttpLoggingInterceptor` 直接写入 Android 日志系统

此外，`htmlText` 模块在 iOS 侧还存在直接 `println` 的输出。

这带来几个具体问题：

- Android 与 iOS 控制台里日志格式不一致
- 网络日志与业务日志不在同一入口，排查链路时要跨通道看日志
- iOS 平台上，错误的底层日志实现方式容易引入额外风险
- BODY 级别日志默认开启，输出量过大，淹没有效信号

本次目标不是“替换几个 API 调用”，而是把项目内日志收敛成一个跨平台、可控、可扩展的统一方案。

## 目标

统一日志体系后，应满足以下要求：

- 所有项目内日志统一通过 `KLogger` 输出
- Ktor、OkHttp、业务层、共享层都使用同一套日志入口
- Android 与 iOS 输出格式一致
- 网络日志默认输出摘要信息，而不是无条件输出 BODY
- 后续如需增加日志级别开关、脱敏、过滤，只需要修改一处基础设施

## 非目标

本轮不做以下事情：

- 不引入完整的结构化日志平台或 JSON log
- 不做远程日志上报
- 不设计复杂的运行时日志配置中心
- 不重构所有历史日志文本内容，只统一入口和策略

## 方案选型

### 方案 A：仅替换调用点

把零散的 `Logger.DEFAULT`、`println` 改成 `KLogger`，其余行为保持不变。

优点：

- 改动小
- 快速见效

缺点：

- Ktor 与 OkHttp 仍然各自维护日志语义
- 网络日志格式和粒度不统一
- 无法系统性控制“默认摘要 / 可选详细”的策略

### 方案 B：统一到 KLogger，并为网络层增加适配器

以 `KLogger` 作为唯一公共日志入口，网络层新增一个轻量适配层，把 Ktor 与 OkHttp 的日志统一收敛后再输出。

优点：

- 真正做到单入口
- 平台表现一致
- 方便后续增加过滤、脱敏和开关
- 变更范围可控

缺点：

- 比简单替换多一层基础设施代码

### 方案 C：直接上结构化日志系统

为所有日志设计事件模型、分类、字段、序列化格式。

优点：

- 长期扩展性最好

缺点：

- 对当前需求明显过重
- 会把“统一日志”做成另一个项目

### 推荐

采用方案 B。

这能在不过度设计的前提下，真正把项目日志收敛为一套统一方案，并为未来演进留出空间。

## 设计

### 1. 保持 KLogger 为唯一公共入口

保留现有：

- `shared/src/commonMain/kotlin/io/github/v2compose/util/KLogger.kt`
- `shared/src/androidMain/kotlin/io/github/v2compose/util/KLogger.kt`
- `shared/src/iosMain/kotlin/io/github/v2compose/util/KLogger.kt`

原则：

- 业务层和共享层继续只依赖 `KLogger`
- 上层代码不得再直接依赖 `Logger.DEFAULT`、`println`、`NSLog`、`Log`
- 平台差异仍然封装在 `actual object KLogger` 内

### 2. 增加网络日志适配层

在 `commonMain` 引入一个轻量网络日志适配器，例如：

- `NetworkLogLevel`
- `NetworkKLogger`

职责：

- 接收 Ktor / OkHttp 的原始日志或事件
- 决定输出到 `KLogger.v/d/i/w/e`
- 对日志内容做归一化

该层只做日志格式与级别策略，不承担网络行为逻辑。

### 3. 统一 Ktor 日志出口

`shared/src/commonMain/kotlin/io/github/v2compose/network/V2Client.kt`

当前 Ktor `Logging` 插件使用的是 `Logger.DEFAULT`。

修改后：

- 改为使用自定义 `Logger`
- 该 `Logger` 内部只调用 `NetworkKLogger` / `KLogger`

这样 Ktor 日志会直接进入统一日志体系，而不是走外部默认 logger。

### 4. 统一 Android OkHttp 日志出口

`shared/src/androidMain/kotlin/io/github/v2compose/network/OkHttpFactory.kt`

当前 `HttpLoggingInterceptor` 通过 `L.v(msg)` 输出。

修改后：

- 由 `HttpLoggingInterceptor` 委托到统一的网络日志适配层
- 不再让 Android 网络日志走独立通道

结果：

- Android 的 OkHttp 日志与 Ktor 日志、业务日志格式统一

### 5. 清理 iOS / 模块内的直写日志

至少处理：

- `htmlText/src/iosMain/kotlin/io/github/cooaer/htmltext/PlatformActuals.kt`

把这里的 `println` 改成统一日志入口，避免库模块继续绕开主日志体系。

如果后续发现其他模块仍有直接输出，也按同一原则收口。

### 6. 默认日志策略：摘要优先

统一后的默认策略：

- 请求开始：`METHOD URL`
- 响应完成：`STATUS URL elapsed`
- 错误：`METHOD URL + status/error`
- 关键业务事件：直接保留业务语义

默认不打印：

- 响应 BODY
- 大量 header 明细
- 无边界的大对象 dump

详细模式下才允许输出 headers/body。

### 7. 可选详细模式

为后续保留一个简单开关能力：

- `Summary`
- `Verbose`

本轮不要求做复杂 UI 配置，但代码结构上要允许后续切换。

初始实现可以直接在共享网络层提供一个简单常量或配置源。

## 文件边界

预计会修改的文件：

- `shared/src/commonMain/kotlin/io/github/v2compose/util/KLogger.kt`
- `shared/src/androidMain/kotlin/io/github/v2compose/util/KLogger.kt`
- `shared/src/iosMain/kotlin/io/github/v2compose/util/KLogger.kt`
- `shared/src/commonMain/kotlin/io/github/v2compose/network/V2Client.kt`
- `shared/src/androidMain/kotlin/io/github/v2compose/network/OkHttpFactory.kt`
- `htmlText/src/iosMain/kotlin/io/github/cooaer/htmltext/PlatformActuals.kt`

预计新增的文件：

- `shared/src/commonMain/kotlin/io/github/v2compose/util/NetworkKLogger.kt`

如实现中发现职责过重，可把网络日志级别与格式拆分为两个小文件，但不应扩散成过多层次。

## 测试与验证

### 编译验证

必须验证：

- `./gradlew :shared:allTests`
- `./gradlew :androidApp:assembleFossDebug`
- `./gradlew :shared:compileKotlinIosSimulatorArm64`

### 手工验证

需要检查：

- Android Studio 运行 Android 时，业务日志和网络日志都能看到
- Android Studio / Xcode 运行 iOS 时，业务日志和网络日志都能看到
- 网络请求默认输出为摘要，不再无条件刷 BODY
- 两步登录、首页加载等关键路径的日志链路可连续阅读

## 风险

### 风险 1：日志量骤降，排查时信息不够

缓解：

- 保留详细模式能力
- 对错误日志保留足够上下文

### 风险 2：Android 与 iOS 输出格式仍存在平台差异

缓解：

- 统一在 `commonMain` 完成日志文本格式化
- `actual KLogger` 仅负责最后落地

### 风险 3：网络适配层把原始日志切得太碎

缓解：

- 摘要策略只做“保留关键字段”，不做过度裁剪

## 实施顺序

1. 引入统一的网络日志适配层
2. Ktor `Logging` 改接 `KLogger`
3. Android `HttpLoggingInterceptor` 改接 `KLogger`
4. 清理 iOS / 模块内 `println`
5. 调整默认日志粒度为摘要优先
6. 做双端编译和人工验证

## 结论

本次统一日志不应停留在“替换几个 API 调用”层面，而应把项目内日志正式收敛到 `KLogger` 这一唯一入口。

推荐方案是：

- `KLogger` 作为唯一公共日志 API
- 网络层新增轻量适配器统一 Ktor / OkHttp 输出
- 默认采用摘要日志策略
- 保留详细模式扩展点

这能在当前项目复杂度下，以较小风险获得跨平台一致、可维护、可扩展的日志体系。
