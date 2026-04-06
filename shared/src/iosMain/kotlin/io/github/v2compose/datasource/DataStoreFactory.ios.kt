package io.github.v2compose.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import okio.Path.Companion.toPath

/**
 * 创建 Account DataStore 的 iOS 实际实现
 * 使用 NSDocumentDirectory 作为存储路径
 */
@OptIn(ExperimentalForeignApi::class)
actual fun createAccountDataStore(): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        )
        requireNotNull(documentDirectory) { "Failed to get iOS document directory" }

        val filePath = documentDirectory.path + "/datastore/account.preferences_pb"
        filePath.toPath()
    }
}

/**
 * 创建 App DataStore 的 iOS 实际实现
 * 使用 NSDocumentDirectory 作为存储路径
 */
@OptIn(ExperimentalForeignApi::class)
actual fun createAppDataStore(): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        )
        requireNotNull(documentDirectory) { "Failed to get iOS document directory" }

        val filePath = documentDirectory.path + "/datastore/settings.preferences_pb"
        filePath.toPath()
    }
}
