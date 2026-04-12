package io.github.v2compose.di

import io.github.v2compose.PlatformCapabilities
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformCapabilitiesModule: Module = module {
    single { PlatformCapabilities.Ios }
}
