package io.github.v2compose.shared.bean

data class AppSettings(
    val topicRepliesReversed: Boolean = true,
    val openInInternalBrowser: Boolean = true,
    val darkThemeEnabled: Boolean = true,
    val topicTitleOverview: Boolean = true,
    val ignoredReleaseName: String? = null,
    val autoCheckIn: Boolean = false,
    val searchKeywords: List<String> = listOf(),
    val highlightOpReply: Boolean = false,
    val replyWithFloor: Boolean = true,
    val hideLoginRelatedUi: Boolean = true,
    val hideTopicUserInfo: Boolean = true,
    val disableAvatarImages: Boolean = true,
    val appearanceDarkPresetIndex: Int = 0,
    val appearanceLightPresetIndex: Int = 0,
    val appearanceDarkOverridesJson: String = "",
    val appearanceLightOverridesJson: String = "",
    val primaryTextSize: Int = 16,
    val secondaryTextSize: Int = 12,
    // Only affects topic titles in list items (home/recent/mine etc). Keep separate from global typography.
    val topicListTitleTextSize: Int = 14,
    val topicBodyTextSize: Int = 14,
    val topicReplyTextSize: Int = 14,
) {
    companion object {
        val Default = AppSettings()
    }
}
