package io.github.v2compose.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import java.io.File

/**
 * Android 全局 Context 引用，由 Koin 模块初始化
 */
lateinit var androidContext: Context

/**
 * 使用指定的 Context 创建 Account DataStore
 */
fun createAccountDataStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create {
        File(context.filesDir, "datastore/account.preferences_pb")
    }
}

/**
 * 使用指定的 Context 创建 App DataStore
 */
fun createAppDataStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create {
        File(context.filesDir, "datastore/settings.preferences_pb")
    }
}

/**
 * 使用全局 androidContext 创建 Account DataStore 的实际实现
 */
actual fun createAccountDataStore(): DataStore<Preferences> {
    return createAccountDataStore(androidContext)
}

/**
 * 使用全局 androidContext 创建 App DataStore 的实际实现
 */
actual fun createAppDataStore(): DataStore<Preferences> {
    return createAppDataStore(androidContext)
}
