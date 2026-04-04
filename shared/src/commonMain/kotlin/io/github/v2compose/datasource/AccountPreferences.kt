package io.github.v2compose.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.v2compose.shared.bean.Account
import io.github.v2compose.shared.bean.AccountBalance
import io.github.v2compose.shared.bean.DraftTopic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val TAG = "AccountPreferences"

class AccountPreferences(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val KeyAccount = stringPreferencesKey("account")
        private val KeyDraftTopic = stringPreferencesKey("draft_topic")
        private val KeyDraftSupplement = stringPreferencesKey("draft_supplement")

        private val KeyUnreadNotificationsCount = intPreferencesKey("unread_notifications_count")
        private val KeyLastCheckInTime = longPreferencesKey("last_check_in_time")
    }

    val account: Flow<Account> = dataStore.data.map { preferences ->
        preferences[KeyAccount].let {
            if (it.isNullOrEmpty()) Account.Empty else Account.fromJson(it)
        }
    }.distinctUntilChanged()

    val draftTopic: Flow<DraftTopic> = dataStore.data.map { preferences ->
        preferences[KeyDraftTopic].let {
            if (it.isNullOrEmpty()) DraftTopic.Empty else DraftTopic.fromJson(it)
        }
    }

    val unreadNotifications = dataStore.data.map {
        it[KeyUnreadNotificationsCount] ?: 0
    }

    val lastCheckInTime = dataStore.data.map {
        it[KeyLastCheckInTime] ?: 0L
    }

    val draftSupplement = dataStore.data.map {
        it[KeyDraftSupplement] ?: ""
    }

    suspend fun account(value: Account) {
        dataStore.edit {
            it[KeyAccount] = value.toJson()
        }
    }

    suspend fun updateAccount(
        userName: String? = null,
        userAvatar: String? = null,
        description: String? = null,
        nodes: Int? = null,
        topics: Int? = null,
        following: Int? = null,
        balance: AccountBalance? = null,
    ) {
        val current = account.first()
        account(
            current.copy(
                userName = userName ?: current.userName,
                userAvatar = userAvatar ?: current.userAvatar,
                description = description ?: current.description,
                nodes = nodes ?: current.nodes,
                topics = topics ?: current.topics,
                following = following ?: current.following,
                balance = balance ?: current.balance,
            )
        )
    }

    suspend fun draftTopic(value: DraftTopic) {
        dataStore.edit {
            it[KeyDraftTopic] = value.toJson()
        }
    }

    suspend fun unreadNotifications(value: Int) {
        dataStore.edit {
            it[KeyUnreadNotificationsCount] = value
        }
    }

    suspend fun lastCheckInTime(value: Long) {
        dataStore.edit {
            it[KeyLastCheckInTime] = value
        }
    }

    suspend fun draftSupplement(value: String) {
        dataStore.edit {
            it[KeyDraftSupplement] = value
        }
    }

    suspend fun clear() {
        dataStore.edit {
            it.clear()
        }
    }
}
