package io.github.cooaer.htmltext

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class ExternalImageSizePolicyTest {

    @Test
    fun usesFixedSafeBoundsForHtmlProbe() {
        assertEquals(
            ExternalImageDecodeSize(width = 1440, height = 1440),
            ExternalImageSizePolicy.htmlProbeDecodeSize(),
        )
    }

    @Test
    fun matchesInlineDecodeSizeToDisplaySizeWhenWithinLimit() {
        val density = Density(density = 2f)

        assertEquals(
            ExternalImageDecodeSize(width = 320, height = 160),
            ExternalImageSizePolicy.inlineDecodeSize(
                width = 160.dp,
                height = 80.dp,
                density = density,
            ),
        )
    }

    @Test
    fun capsInlineDecodeSizeAtSafeLongSide() {
        val density = Density(density = 2f)

        assertEquals(
            ExternalImageDecodeSize(width = 1440, height = 480),
            ExternalImageSizePolicy.inlineDecodeSize(
                width = 1200.dp,
                height = 400.dp,
                density = density,
            ),
        )
    }

    @Test
    fun scalesGalleryPreviewFromViewportAndAppliesHardLimit() {
        assertEquals(
            ExternalImageDecodeSize(width = 1189, height = 2560),
            ExternalImageSizePolicy.galleryPreviewDecodeSize(
                viewportWidthPx = 1290,
                viewportHeightPx = 2778,
            ),
        )
    }

    @Test
    fun clampsInvalidDecodeInputsToMinimumSize() {
        assertEquals(
            ExternalImageDecodeSize(width = 1, height = 1),
            boundedDecodeSize(
                widthPx = 0,
                heightPx = -32,
                maxLongSidePx = 1440,
            ),
        )
    }
}
