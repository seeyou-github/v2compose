package io.github.v2compose

import android.app.Application
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.SingletonImageLoader
import io.github.v2compose.core.NotificationCenter
import io.github.v2compose.di.appModule
import io.github.v2compose.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class App : Application(), Configuration.Provider, KoinComponent {

    val imageLoader: ImageLoader by inject()

    companion object {
        private const val TAG = "APP"
        lateinit var instance: App
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        initKoin(
            appDeclaration = {
                androidLogger()
                androidContext(this@App)
                workManagerFactory()
            },
            platformModules = listOf(appModule)
        )

        init()
    }

    private fun init() {
        SingletonImageLoader.setSafe { imageLoader }
        NotificationCenter.init(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .build() // Koin's workManagerFactory injects workers automatically

}
