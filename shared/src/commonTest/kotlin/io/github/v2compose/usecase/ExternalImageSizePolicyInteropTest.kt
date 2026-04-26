package io.github.v2compose.usecase

import io.github.cooaer.htmltext.ExternalImageDecodeSize
import io.github.cooaer.htmltext.ExternalImageSizePolicy
import kotlin.test.Test
import kotlin.test.assertEquals

class ExternalImageSizePolicyInteropTest {

    @Test
    fun sharedModuleUsesBoundedHtmlProbeDecodeSize() {
        assertEquals(
            ExternalImageDecodeSize(width = 1440, height = 1440),
            ExternalImageSizePolicy.htmlProbeDecodeSize(),
        )
    }

    @Test
    fun sharedModuleUsesBoundedGalleryPreviewDecodeSize() {
        assertEquals(
            ExternalImageDecodeSize(width = 1189, height = 2560),
            ExternalImageSizePolicy.galleryPreviewDecodeSize(
                viewportWidthPx = 1290,
                viewportHeightPx = 2778,
            ),
        )
    }
}
