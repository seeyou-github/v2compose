package io.github.v2compose.ui.supplement

import androidx.lifecycle.SavedStateHandle

class AddSupplementArgs(val topicId: String) {
    constructor(savedStateHandle: SavedStateHandle) : this(
        checkNotNull(
            savedStateHandle.get<String>(argsTopicId)
        )
    )
}
