package io.github.v2compose.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

/**
 * 跨平台 Koin 初始化入口
 */
fun initKoin(
    appDeclaration: KoinAppDeclaration = {},
    platformModule: Module
) {
    startKoin {
        appDeclaration()
        modules(
            platformModule,
            // sharedModule, // 待后续将共享模块逻辑移入此处
        )
    }
}
