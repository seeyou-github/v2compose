package io.github.v2compose.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.v2compose.shared.bean.AppSettings
import io.github.v2compose.shared.bean.ProxyInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private const val TAG = "AppPreferences"

class AppPreferences(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val KeyTopicRepliesReversed = booleanPreferencesKey("topic_replies_reversed")
        private val KeyOpenInInternalBrowser = booleanPreferencesKey("open_in_internal_browser")
        private val KeyDarkThemeEnabled = booleanPreferencesKey("dark_theme_enabled")
        private val KeyTopicTitleOverview = booleanPreferencesKey("topic_title_overview")
        private val KeyIgnoredReleaseName = stringPreferencesKey("ignored_release_name")
        private val KeyAutoCheckIn = booleanPreferencesKey("auto_check_in")
        private val KeySearchKeywords = stringPreferencesKey("search_keywords")
        private val KeyHighlightOpReply = booleanPreferencesKey("highlight_op_reply")
        private val KeyReplyWithFloor = booleanPreferencesKey("reply_with_floor")
        private val KeyHideLoginRelatedUi = booleanPreferencesKey("hide_login_related_ui")
        private val KeyHideTopicUserInfo = booleanPreferencesKey("hide_topic_user_info")
        private val KeyDisableAvatarImages = booleanPreferencesKey("disable_avatar_images")

        private val KeyAppearanceDarkPresetIndex = intPreferencesKey("appearance_dark_preset_index")
        private val KeyAppearanceLightPresetIndex = intPreferencesKey("appearance_light_preset_index")
        private val KeyAppearanceDarkOverridesJson = stringPreferencesKey("appearance_dark_overrides_json")
        private val KeyAppearanceLightOverridesJson = stringPreferencesKey("appearance_light_overrides_json")
        private val KeyPrimaryTextSize = intPreferencesKey("primary_text_size")
        private val KeySecondaryTextSize = intPreferencesKey("secondary_text_size")
        private val KeyTopicListTitleTextSize = intPreferencesKey("topic_list_title_text_size")
        private val KeyTopicBodyTextSize = intPreferencesKey("topic_body_text_size")
        private val KeyTopicReplyTextSize = intPreferencesKey("topic_reply_text_size")

        private val KeyProxyInfo = stringPreferencesKey("proxy_info")
    }

    val appSettings: Flow<AppSettings> = dataStore.data.map {
        AppSettings(
            topicRepliesReversed = it[KeyTopicRepliesReversed] ?: true,
            openInInternalBrowser = it[KeyOpenInInternalBrowser] ?: true,
            darkThemeEnabled = it[KeyDarkThemeEnabled] ?: true,
            topicTitleOverview = it[KeyTopicTitleOverview] ?: true,
            ignoredReleaseName = it[KeyIgnoredReleaseName],
            autoCheckIn = it[KeyAutoCheckIn] ?: false,
            searchKeywords = it[KeySearchKeywords]?.split(",") ?: listOf(),
            highlightOpReply = it[KeyHighlightOpReply] ?: false,
            replyWithFloor = it[KeyReplyWithFloor] ?: true,
            hideLoginRelatedUi = it[KeyHideLoginRelatedUi] ?: true,
            hideTopicUserInfo = it[KeyHideTopicUserInfo] ?: true,
            disableAvatarImages = it[KeyDisableAvatarImages] ?: true,
            appearanceDarkPresetIndex = it[KeyAppearanceDarkPresetIndex] ?: 0,
            appearanceLightPresetIndex = it[KeyAppearanceLightPresetIndex] ?: 0,
            appearanceDarkOverridesJson = it[KeyAppearanceDarkOverridesJson] ?: "",
            appearanceLightOverridesJson = it[KeyAppearanceLightOverridesJson] ?: "",
            primaryTextSize = it[KeyPrimaryTextSize] ?: 16,
            secondaryTextSize = it[KeySecondaryTextSize] ?: 12,
            topicListTitleTextSize = it[KeyTopicListTitleTextSize] ?: 14,
            topicBodyTextSize = it[KeyTopicBodyTextSize] ?: 14,
            topicReplyTextSize = it[KeyTopicReplyTextSize] ?: 14,
        )
    }.distinctUntilChanged()

    val proxyInfo: Flow<ProxyInfo> = dataStore.data.map {
        it[KeyProxyInfo]?.let { value -> ProxyInfo.fromJson(value) } ?: ProxyInfo.Default
    }

    suspend fun toggleTopicRepliesOrder() {
        dataStore.edit {
            it[KeyTopicRepliesReversed] = !(it[KeyTopicRepliesReversed] ?: true)
        }
    }

    suspend fun openInInternalBrowser(value: Boolean) {
        dataStore.edit {
            it[KeyOpenInInternalBrowser] = value
        }
    }

    suspend fun darkThemeEnabled(value: Boolean) {
        dataStore.edit {
            it[KeyDarkThemeEnabled] = value
        }
    }

    suspend fun topicTitleOverview(value: Boolean) {
        dataStore.edit {
            it[KeyTopicTitleOverview] = value
        }
    }

    suspend fun ignoredReleaseName(value: String) {
        dataStore.edit {
            it[KeyIgnoredReleaseName] = value
        }
    }

    suspend fun autoCheckIn(value: Boolean) {
        dataStore.edit {
            it[KeyAutoCheckIn] = value
        }
    }

    suspend fun replyWithFloor(value: Boolean) {
        dataStore.edit {
            it[KeyReplyWithFloor] = value
        }
    }

    suspend fun searchKeywords(value: List<String>) {
        dataStore.edit {
            it[KeySearchKeywords] = value.joinToString(",")
        }
    }

    suspend fun highlightOpReply(value: Boolean) {
        dataStore.edit {
            it[KeyHighlightOpReply] = value
        }
    }

    suspend fun proxyInfo(proxy: ProxyInfo) {
        dataStore.edit {
            it[KeyProxyInfo] = proxy.toJson()
        }
    }

    suspend fun hideLoginRelatedUi(value: Boolean) {
        dataStore.edit {
            it[KeyHideLoginRelatedUi] = value
        }
    }

    suspend fun hideTopicUserInfo(value: Boolean) {
        dataStore.edit {
            it[KeyHideTopicUserInfo] = value
        }
    }

    suspend fun disableAvatarImages(value: Boolean) {
        dataStore.edit {
            it[KeyDisableAvatarImages] = value
        }
    }

    suspend fun appearanceDarkPresetIndex(value: Int) {
        dataStore.edit { it[KeyAppearanceDarkPresetIndex] = value }
    }

    suspend fun appearanceLightPresetIndex(value: Int) {
        dataStore.edit { it[KeyAppearanceLightPresetIndex] = value }
    }

    suspend fun appearanceDarkOverridesJson(value: String) {
        dataStore.edit { it[KeyAppearanceDarkOverridesJson] = value }
    }

    suspend fun appearanceLightOverridesJson(value: String) {
        dataStore.edit { it[KeyAppearanceLightOverridesJson] = value }
    }

    suspend fun primaryTextSize(value: Int) {
        dataStore.edit { it[KeyPrimaryTextSize] = value }
    }

    suspend fun secondaryTextSize(value: Int) {
        dataStore.edit { it[KeySecondaryTextSize] = value }
    }

    suspend fun topicBodyTextSize(value: Int) {
        dataStore.edit { it[KeyTopicBodyTextSize] = value }
    }

    suspend fun topicReplyTextSize(value: Int) {
        dataStore.edit { it[KeyTopicReplyTextSize] = value }
    }

    suspend fun topicListTitleTextSize(value: Int) {
        dataStore.edit { it[KeyTopicListTitleTextSize] = value }
    }
}
