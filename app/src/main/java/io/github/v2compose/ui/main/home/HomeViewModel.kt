package io.github.v2compose.ui.main.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val tabNames =
        arrayOf(
            "全部",
            "最热",
            "最近",
            "技术",
            "创意",
            "好玩",
            "Apple",
            "酷工作",
            "交易",
            "城市",
            "问与答",
            "R2",
            "节点",
            "关注"
        )
    private val tabValues = arrayOf(
        "all",
        "hot",
        "recent",
        "tech",
        "creative",
        "play",
        "apple",
        "jobs",
        "deals",
        "city",
        "qna",
        "r2",
        "nodes",
        "members"
    )
    val newsTabInfos =
        tabNames.mapIndexed { index, title -> NewsTabInfo(title, tabValues[index]) }
}

data class NewsTabInfo(val name: String, val value: String) {
    companion object {
        const val recent = "recent"
    }
}