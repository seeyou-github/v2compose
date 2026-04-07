package io.github.v2compose.shared.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class V2EventManager {
    private val _events = MutableSharedFlow<Any>(extraBufferCapacity = 64)
    val events = _events.asSharedFlow()

    suspend fun post(event: Any) {
        _events.emit(event)
    }

    fun tryPost(event: Any) {
        _events.tryEmit(event)
    }
}
