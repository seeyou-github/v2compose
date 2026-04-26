package io.github.v2compose.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.v2compose.network.V2exApi
import io.github.v2compose.util.KLogger

private const val TAG = "TopicPagingSource"

class TopicPagingSource(
    private val topicId: String,
    private val initialPage: Int?,
    private val reversed: Boolean,
    private val v2exService: V2exApi,
) : PagingSource<Int, Any>() {

    companion object {
        const val FirstPageIndex = 1
        private const val StartPageReversed = 999
    }

    private var startPage = initialPage ?: if (reversed) StartPageReversed else FirstPageIndex

    override fun getRefreshKey(state: PagingState<Int, Any>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Any> {
        return try {
            var page = params.key ?: startPage
            val topicInfo = v2exService.topicDetails(topicId = topicId, page = page)
            KLogger.d(TAG, "load, result, topicInfo = $topicInfo")
            val totalPageCount = topicInfo.totalPage()
            if (page == StartPageReversed) {
                startPage = totalPageCount
                page = startPage
            }
            val data = topicInfo.replies.toMutableList<Any>().apply {
                if (page == FirstPageIndex || (reversed && page == totalPageCount)) {
                    add(0, topicInfo)
                }
                if (reversed) {
                    reverse()
                }
            }
            val largerPage = if (page < totalPageCount) page + 1 else null
            val smallerPage = if (page <= FirstPageIndex) null else page - 1
            val prev = if (reversed) largerPage else smallerPage
            val next = if (reversed) smallerPage else largerPage
            LoadResult.Page(
                data = data,
                prevKey = prev,
                nextKey = next,
            )
        } catch (e: Exception) {
            KLogger.e(TAG, "load topic failed", e)
            LoadResult.Error(e)
        }
    }
}
