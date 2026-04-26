package io.github.v2compose.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.github.v2compose.core.PlatformContext
import java.io.File

actual fun createAccountDataStore(context: PlatformContext): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create {
        File(context.filesDir, "datastore/account.preferences_pb")
    }
}

actual fun createAppDataStore(context: PlatformContext): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create {
        File(context.filesDir, "datastore/settings.preferences_pb")
    }
}
