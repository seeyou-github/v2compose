package io.github.v2compose

import coil3.ImageLoader
import coil3.SingletonImageLoader
import org.koin.mp.KoinPlatformTools

internal fun initIosRuntime() {
    val context = KoinPlatformTools.defaultContext()
    if (context.getOrNull() == null) {
        io.github.v2compose.di.initKoin()
    }
    SingletonImageLoader.setSafe { _: coil3.PlatformContext ->
        KoinPlatformTools.defaultContext().get().get<ImageLoader>()
    }
}
