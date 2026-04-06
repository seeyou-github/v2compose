package io.github.v2compose

import android.app.Application
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import io.github.v2compose.core.NotificationCenter
import io.github.v2compose.core.analytics.IAnalytics
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import java.lang.reflect.Field
import java.lang.reflect.Modifier

class App : Application(), ImageLoaderFactory, Configuration.Provider, KoinComponent {

    val imageLoader: ImageLoader by inject()
    val analytics: IAnalytics by inject()

    companion object {
        private const val TAG = "APP"
        lateinit var instance: App
    }

    override fun onCreate() {
        beforeOnCreate()
        super.onCreate()
        instance = this
        
        startKoin {
            androidLogger()
            androidContext(this@App)
            workManagerFactory()
            modules(io.github.v2compose.di.allModules)
        }
        
        init()
    }

    private fun beforeOnCreate() {
//        resetScrollableTabRowMinimumTabWidth()
    }

    private fun init() {
        initLogger()
        if (BuildConfig.DEBUG) analytics.stopTracking()
        NotificationCenter.init(this)
    }

    private fun initLogger() {
        val formatStrategy: FormatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(false) // (Optional) Whether to show thread info or not. Default true
            .methodCount(0) // (Optional) How many method line to show. Default 2
            .methodOffset(7) // (Optional) Hides internal method calls up to offset. Default 5
            .tag("V2compose.Log") // (Optional) Global tag for every log. Default PRETTY_LOGGER
            .build()
        Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })
    }

    override fun newImageLoader(): ImageLoader = imageLoader

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build() // Koin's workManagerFactory injects workers automatically

    private fun resetScrollableTabRowMinimumTabWidth() {
        try {
            val cls = Class.forName("androidx.compose.material3.TabRowKt")
            val field = cls.getDeclaredField("ScrollableTabRowMinimumTabWidth")
            field.isAccessible = true
            val modifiersField = Field::class.java.getDeclaredField("accessFlags")
            modifiersField.isAccessible = true
            modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
            field.set(null, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}