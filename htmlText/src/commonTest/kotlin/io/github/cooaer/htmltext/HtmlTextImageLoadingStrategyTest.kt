package io.github.cooaer.htmltext

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HtmlTextImageLoadingStrategyTest {

    @Test
    fun suppressesAutoLoadWhenExternalImageLoaderExists() {
        assertFalse(
            shouldAutoLoadInlineImage(
                loadState = "",
                hasExternalImageLoader = true,
            ),
        )
    }

    @Test
    fun keepsAutoLoadWhenNoExternalImageLoaderExists() {
        assertTrue(
            shouldAutoLoadInlineImage(
                loadState = "",
                hasExternalImageLoader = false,
            ),
        )
    }

    @Test
    fun doesNotAutoLoadWhenImageAlreadyHasState() {
        assertFalse(
            shouldAutoLoadInlineImage(
                loadState = "loading",
                hasExternalImageLoader = true,
            ),
        )
    }
}
