package io.github.cooaer.htmltext

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import kotlin.math.ceil
import kotlin.math.roundToInt

data class ExternalImageDecodeSize(
    val width: Int,
    val height: Int,
)

object ExternalImageSizePolicy {
    private const val HTML_PROBE_MAX_LONG_SIDE_PX = 1440
    private const val GALLERY_PREVIEW_SCALE_MULTIPLIER = 2
    private const val GALLERY_PREVIEW_MAX_LONG_SIDE_PX = 2560

    fun htmlProbeDecodeSize(): ExternalImageDecodeSize {
        return ExternalImageDecodeSize(
            width = HTML_PROBE_MAX_LONG_SIDE_PX,
            height = HTML_PROBE_MAX_LONG_SIDE_PX,
        )
    }

    fun inlineDecodeSize(
        width: Dp,
        height: Dp,
        density: Density,
    ): ExternalImageDecodeSize {
        val widthPx = with(density) { ceil(width.toPx()).toInt() }
        val heightPx = with(density) { ceil(height.toPx()).toInt() }
        return boundedDecodeSize(
            widthPx = widthPx,
            heightPx = heightPx,
            maxLongSidePx = HTML_PROBE_MAX_LONG_SIDE_PX,
        )
    }

    fun galleryPreviewDecodeSize(
        viewportWidthPx: Int,
        viewportHeightPx: Int,
    ): ExternalImageDecodeSize {
        return boundedDecodeSize(
            widthPx = viewportWidthPx.coerceAtLeast(1) * GALLERY_PREVIEW_SCALE_MULTIPLIER,
            heightPx = viewportHeightPx.coerceAtLeast(1) * GALLERY_PREVIEW_SCALE_MULTIPLIER,
            maxLongSidePx = GALLERY_PREVIEW_MAX_LONG_SIDE_PX,
        )
    }
}

internal fun boundedDecodeSize(
    widthPx: Int,
    heightPx: Int,
    maxLongSidePx: Int,
): ExternalImageDecodeSize {
    val safeWidth = widthPx.coerceAtLeast(1)
    val safeHeight = heightPx.coerceAtLeast(1)
    val safeMaxLongSide = maxLongSidePx.coerceAtLeast(1)
    val currentLongSide = maxOf(safeWidth, safeHeight)

    if (currentLongSide <= safeMaxLongSide) {
        return ExternalImageDecodeSize(width = safeWidth, height = safeHeight)
    }

    val scale = safeMaxLongSide.toDouble() / currentLongSide.toDouble()
    return ExternalImageDecodeSize(
        width = maxOf(1, (safeWidth * scale).roundToInt()),
        height = maxOf(1, (safeHeight * scale).roundToInt()),
    )
}
