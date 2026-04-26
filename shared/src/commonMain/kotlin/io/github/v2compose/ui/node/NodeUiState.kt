package io.github.v2compose.ui.node

import androidx.compose.runtime.Stable
import io.github.v2compose.network.bean.NodeInfo

@Stable
sealed interface NodeUiState {
    data class Success(val nodeInfo: NodeInfo) : NodeUiState
    data object Loading : NodeUiState
    data class Error(val error: Throwable?) : NodeUiState
}
