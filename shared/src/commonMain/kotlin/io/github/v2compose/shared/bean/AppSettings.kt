package io.github.v2compose.shared.bean

data class AppSettings(
    val topicRepliesReversed: Boolean = true,
    val openInInternalBrowser: Boolean = true,
    val darkThemeEnabled: Boolean = true,
    val topicTitleOverview: Boolean = true,
    val ignoredReleaseName: String? = null,
    val autoCheckIn: Boolean = false,
    val searchKeywords: List<String> = listOf(),
    // Customizable Home tab categories (order/visibility/custom nodes). Empty -> use defaults.
    val homeTabConfigsJson: String = "",
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
    val topicListTitleTextSize: Int = 22,
    // Line height for topic titles in home list (sp). Kept explicit since title uses a custom fontSize.
    val homeListTitleLineHeight: Int = 33,
    // Layout spacing (dp)
    val homeListItemVerticalPadding: Int = 5,
    val topBarMinHeight: Int = 5,
    val topicBodyTextSize: Int = 20,
    val topicReplyTextSize: Int = 19,
) {
    companion object {
        val Default = AppSettings()
    }
}
