package io.github.v2compose.ui.topic

import kotlin.test.Test
import kotlin.test.assertEquals

class TopicHtmlContentStateTest {

    @Test
    fun topicBodyStateKeepsOriginalSourceContentWhenRenderedHtmlChanges() {
        val state = topicBodyHtmlContentState(
            renderedContent = """<img src="https://example.com/a.png" loadState="error">""",
            originalContent = """<img src="https://example.com/a.png">""",
        )

        assertEquals(
            """<img src="https://example.com/a.png" loadState="error">""",
            state.content,
        )
        assertEquals(
            """<img src="https://example.com/a.png">""",
            state.sourceContent,
        )
    }
}
