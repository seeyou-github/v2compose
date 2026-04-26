# Fruit-kt 构造器解析化与 network/bean 不可变模型迁移

## Summary
- 将 `fruit-kt` 的 KSP 生成器切换为“主构造函数注入”模式，只支持 `data class + val` 的新模型写法。
- 为 Fruit 新增 `RawResponseHolder` 契约，仅在需要二次解析原始 HTML 的模型上保留 `rawResponse`。
- 将 `shared/src/commonMain/kotlin/io/github/v2compose/network/bean/` 中本次涉及的 HTML/JSON 模型统一迁为不可变数据模型，移除 `BaseInfo`、`IBase` 和集合继承包装。

## Implementation
- `fruit-kt`
  - 在 `fruit/src/commonMain/kotlin/io/github/fruit/Fruit.kt` 新增 `RawResponseHolder`。
  - 重写 `fruit-ksp/src/main/kotlin/io/github/fruit/ksp/FruitProcessor.kt`：
    - 只读取 `@Pulp` 类型的主构造函数属性。
    - 只允许 `@Pick` 出现在主构造函数属性上。
    - 生成 `TypeName(prop = ...)` 形式的 Adapter，不再创建无参实例并回填属性。
    - 对集合继承、缺失主构造函数、可变主构造属性等旧模式直接在 KSP 阶段报错。
  - 更新 `fruit-ksp-sample` 为 `data class` 样例，并覆盖列表、嵌套对象、`rawResponse` 注入场景。
- `V2compose`
  - 将 Fruit 解析模型统一改成 `@Pulp data class` + `@property:Pick val ...`。
  - 将 JSON 模型统一改成 `@Serializable data class`；`NodesInfo` 使用自定义序列化器，将根数组映射为 `items`。
  - 删除 `BaseInfo.kt`、`IBase.kt`。
  - `NodesNavInfo` 改为显式 `items: List<Item>`，调用点同步改为访问 `items`。
  - `NewsInfo` 实现 `RawResponseHolder`，`AppStateStore` / `UpdateAccountUseCase` 改为读取 `rawResponse`。
  - 测试与播种逻辑改为构造函数初始化，不再依赖 `apply` 写入可变属性。

## Validation
- `./gradlew :shared:allTests`
- `./gradlew :fruit-ksp-sample:test`（在 `/Users/cooaer/Developer/myself/fruit-kt`）
- `./gradlew :shared:compileKotlinIosSimulatorArm64`
- `./gradlew :androidApp:assembleFossDebug`

## Notes
- Android 构建过程中 `fruit-kt` 的 Kotlin daemon 增量缓存出现关闭异常，但 Gradle 自动回退到非 daemon 编译后构建成功；没有发现由本次改造引入的源码级错误。
- 当前工作树中已有用户对 `shared/src/commonMain/kotlin/io/github/v2compose/ui/topic/TopicScreen.kt` 的未提交修改，本次提交不会包含该文件。
