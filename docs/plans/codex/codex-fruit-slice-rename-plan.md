# Fruit 术语收口重命名：`Pulp -> Slice` / `PickAdapter -> SliceAdapter`

## Summary
- 在 `fruit-kt` 与 `V2compose` 中一次性切换 `Pulp -> Slice` 与 `PickAdapter -> SliceAdapter` 术语，不保留旧名字兼容层。
- 保持 `@Pick` 继续只承担属性级提取语义，统一“类级作用域”与“运行时适配器”命名，避免 `@Slice` 仍搭配 `PickAdapter` 的半新半旧状态。

## Key Changes
- 注解与 KSP
  - 将 `io.github.fruit.annotations.Pulp` 重命名为 `Slice`，同步更新 KSP 扫描目标、错误文案与示例代码。
  - 生成类后缀由 `*PickAdapter` 切换为 `*SliceAdapter`，注册入口改为 `registerGeneratedSliceAdapters()`。
  - 清理 `fruit-ksp/bin/main` 下已跟踪但未接入构建的镜像 `FruitProcessor.kt`，避免与正式实现分叉。

- 运行时 API
  - 将 `PickAdapter` / `PickAdapterFactory` / `BasicPickAdapters` 统一重命名为 `SliceAdapter` / `SliceAdapterFactory` / `BasicSliceAdapters`。
  - `Fruit.registerAdapter()` 与 `Fruit.getAdapter()` 分别重命名为 `registerSliceAdapter()` 与 `getSliceAdapter()`。
  - `fruit-kt` 内的测试样例、文档与公开示例全部改用 `Slice` / `SliceAdapter` 术语。

- V2compose 接入
  - 将共享层所有 `@Pulp` 模型与嵌套模型统一改为 `@Slice`。
  - Android/iOS Fruit 初始化改用 `registerGeneratedSliceAdapters()`。
  - Android ProGuard 规则改为保留 `@Slice` 标注类和 `@Pick` 字段。

## Validation
- `fruit-kt`
  - `./gradlew build`
- `V2compose`
  - `./gradlew :shared:allTests`
  - `./gradlew :shared:compileKotlinIosSimulatorArm64`
  - `./gradlew :androidApp:assembleFossDebug`

## Assumptions
- 本次接受源码级 break change，不提供 deprecated alias 或双命名过渡层。
- 历史计划文档不回写，只新增当前 rename 的归档计划文档。
