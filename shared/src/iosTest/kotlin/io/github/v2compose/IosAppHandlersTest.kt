package io.github.v2compose

import platform.Foundation.NSURL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IosAppHandlersTest {
    @Test
    fun openUrlDoesNotOpenWhenApplicationRejectsUrl() {
        val logs = mutableListOf<String>()
        val opener = RecordingIosUrlOpener(canOpen = false)

        openUrl(
            url = "https://www.v2ex.com/t/123",
            urlOpener = opener,
            logger = logs::add,
        )

        assertTrue(opener.openedUrls.isEmpty())
        assertEquals(
            listOf("openUrl skipped because UIApplication cannot open: https://www.v2ex.com/t/123"),
            logs,
        )
    }

    @Test
    fun openUrlUsesUrlOpenerForSupportedUrl() {
        val logs = mutableListOf<String>()
        val opener = RecordingIosUrlOpener(canOpen = true)

        openUrl(
            url = "https://www.v2ex.com/t/123",
            urlOpener = opener,
            logger = logs::add,
        )

        assertEquals(listOf("https://www.v2ex.com/t/123"), opener.openedUrls)
        assertTrue(logs.isEmpty())
    }
}

private class RecordingIosUrlOpener(
    private val canOpen: Boolean,
) : IosUrlOpener {
    val openedUrls = mutableListOf<String>()

    override fun canOpen(url: NSURL): Boolean = canOpen

    override fun open(url: NSURL, completionHandler: (Boolean) -> Unit) {
        openedUrls += url.absoluteString ?: ""
        completionHandler(true)
    }
}
