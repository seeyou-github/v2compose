package io.github.v2compose.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.v2compose.network.V2exApi
import io.github.v2compose.util.KLogger

private const val TAG = "NodePagingSource"

class NodePagingSource(private val nodeName: String, private val v2exService: V2exApi) :
    PagingSource<Int, Any>() {

    companion object {
        const val FirstPageIndex = 1
    }

    private var pageCount: Int = 0

    override fun getRefreshKey(state: PagingState<Int, Any>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Any> {
        return try {
            val page = params.key ?: FirstPageIndex
            val nodeInfo = v2exService.nodesInfo(node = nodeName, page = page)
            KLogger.d(TAG, "load, result, nodeTopicInfo = $nodeInfo")
            if (page == FirstPageIndex) {
                pageCount =
                    if (nodeInfo.items.isEmpty()) 0 else nodeInfo.total() / nodeInfo.items.size
            }
            val data = nodeInfo.items.toMutableList<Any>().apply {
                if (page == FirstPageIndex) add(0, nodeInfo)
            }
            val prev = if (page == FirstPageIndex) null else page - 1
            val next = if (page < pageCount) page + 1 else null
            LoadResult.Page(
                data = data,
                prevKey = prev,
                nextKey = next,
            )
        } catch (e: Exception) {
            KLogger.e(TAG, "load node failed", e)
            LoadResult.Error(e)
        }
    }
}
