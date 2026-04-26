package io.github.v2compose.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.github.v2compose.core.PlatformContext
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun createAccountDataStore(context: PlatformContext): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        )
        requireNotNull(documentDirectory) { "Failed to get iOS document directory" }
        (documentDirectory.path + "/datastore/account.preferences_pb").toPath()
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun createAppDataStore(context: PlatformContext): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        )
        requireNotNull(documentDirectory) { "Failed to get iOS document directory" }
        (documentDirectory.path + "/datastore/settings.preferences_pb").toPath()
    }
}
